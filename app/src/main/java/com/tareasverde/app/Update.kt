package com.tareasverde.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Busca versiones nuevas en los "Releases" de GitHub del repositorio configurado en Ajustes
 * (formato usuario/repo). Cada release trae un version.json y el APK; ver README.
 */
object Update {
    data class Info(
        val versionCode: Long,
        val versionName: String,
        val apkUrl: String,
        val notes: String,
    )

    sealed class Result {
        data class Available(val info: Info) : Result()
        object UpToDate : Result()
        object NotConfigured : Result()
        data class Error(val message: String) : Result()
    }

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val repoPattern = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")

    fun isValidRepo(repo: String) = repoPattern.matches(repo.trim())

    fun currentVersionCode(ctx: Context): Long {
        val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    fun currentVersionName(ctx: Context): String =
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"

    fun check(ctx: Context, onResult: (Result) -> Unit) {
        val app = ctx.applicationContext
        val repo = AppPrefs.updateRepo(app)
        if (!isValidRepo(repo)) {
            onResult(Result.NotConfigured)
            return
        }
        io.execute {
            val result: Result = try {
                val url = URL("https://github.com/$repo/releases/latest/download/version.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    instanceFollowRedirects = true
                }
                if (conn.responseCode != 200) {
                    Result.Error("el repositorio no tiene versiones publicadas todavía (código ${conn.responseCode})")
                } else {
                    val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    val info = Info(
                        versionCode = json.getLong("versionCode"),
                        versionName = json.optString("versionName", "?"),
                        apkUrl = json.getString("apkUrl"),
                        notes = json.optString("notes", ""),
                    )
                    if (info.versionCode > currentVersionCode(app)) Result.Available(info) else Result.UpToDate
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "sin conexión")
            }
            main.post { onResult(result) }
        }
    }

    /** Descarga el APK a la carpeta de caché. onDone recibe null si falló. */
    fun download(ctx: Context, info: Info, onProgress: (Int) -> Unit, onDone: (File?) -> Unit) {
        val app = ctx.applicationContext
        io.execute {
            var file: File? = null
            try {
                val dir = File(app.cacheDir, "updates").apply { mkdirs() }
                val target = File(dir, "gogo-pidio.apk")
                val conn = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                }
                if (conn.responseCode == 200) {
                    val total = conn.contentLengthLong
                    var done = 0L
                    var lastPct = -1
                    conn.inputStream.use { input ->
                        target.outputStream().use { out ->
                            val buf = ByteArray(32 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                if (total > 0) {
                                    val pct = (done * 100 / total).toInt()
                                    if (pct != lastPct) {
                                        lastPct = pct
                                        main.post { onProgress(pct) }
                                    }
                                }
                            }
                        }
                    }
                    file = target
                }
            } catch (e: Exception) {
                file = null
            }
            main.post { onDone(file) }
        }
    }

    /**
     * Abre el instalador de Android con el APK descargado. Devuelve false si primero hay que
     * permitir "instalar apps desconocidas" para GO GO PIDIO (en ese caso abre esa pantalla).
     */
    fun install(ctx: Context, file: File): Boolean {
        if (!ctx.packageManager.canRequestPackageInstalls()) {
            Perms.open(ctx, Perms.installUnknownAppsIntent(ctx))
            return false
        }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { ctx.startActivity(intent) }.isSuccess
    }
}
