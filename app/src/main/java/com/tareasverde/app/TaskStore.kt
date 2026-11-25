package com.tareasverde.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Guarda las tareas como JSON en SharedPreferences (simple y sin dependencias extra). */
object TaskStore {
    private const val PREFS = "tareas_prefs"
    private const val KEY_TASKS = "tasks"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): List<Task> {
        val raw = prefs(context).getString(KEY_TASKS, null) ?: return emptyList()
        val today = LocalDate.now().toEpochDay()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                val cat = runCatching { Category.valueOf(o.getString("category")) }
                    .getOrDefault(Category.NEUTRA)
                val doneDay = if (o.isNull("doneDay")) null else o.getLong("doneDay")
                var done = o.optBoolean("done", false)
                // Las tareas diarias se destachan solas al empezar un nuevo día.
                if (cat == Category.DIARIA && done && doneDay != today) done = false
                Task(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    category = cat,
                    dueEpochDay = if (o.isNull("due")) null else o.getLong("due"),
                    done = done,
                    doneEpochDay = doneDay,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach { t ->
            arr.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("category", t.category.name)
                    put("due", t.dueEpochDay ?: JSONObject.NULL)
                    put("done", t.done)
                    put("doneDay", t.doneEpochDay ?: JSONObject.NULL)
                }
            )
        }
        prefs(context).edit().putString(KEY_TASKS, arr.toString()).apply()
    }
}

object AppPrefs {
    private fun p(c: Context) = c.getSharedPreferences("tareas_settings", Context.MODE_PRIVATE)

    fun reminderHour(c: Context): Int = p(c).getInt("hour", 9)
    fun setReminderHour(c: Context, hour: Int) = p(c).edit().putInt("hour", hour).apply()

    fun onboardingDone(c: Context): Boolean = p(c).getBoolean("onboarding_done", false)
    fun setOnboardingDone(c: Context, value: Boolean) =
        p(c).edit().putBoolean("onboarding_done", value).apply()

    fun unlockQuotes(c: Context): Boolean = p(c).getBoolean("unlock_quotes", true)
    fun setUnlockQuotes(c: Context, value: Boolean) =
        p(c).edit().putBoolean("unlock_quotes", value).apply()

    // --- Aspecto de la frase en la pantalla de bloqueo ---
    fun quoteSize(c: Context): Int = p(c).getInt("q_size", 30)
    fun setQuoteSize(c: Context, v: Int) = p(c).edit().putInt("q_size", v).apply()

    fun quoteColor(c: Context): Int = p(c).getInt("q_color", 0xFFFFFFFF.toInt())
    fun setQuoteColor(c: Context, v: Int) = p(c).edit().putInt("q_color", v).apply()

    fun quoteShadow(c: Context): Boolean = p(c).getBoolean("q_shadow", true)
    fun setQuoteShadow(c: Context, v: Boolean) = p(c).edit().putBoolean("q_shadow", v).apply()

    /** 0 = arriba, 1 = centro, 2 = abajo */
    fun quotePosition(c: Context): Int = p(c).getInt("q_pos", 1)
    fun setQuotePosition(c: Context, v: Int) = p(c).edit().putInt("q_pos", v).apply()

    fun quoteSeconds(c: Context): Int = p(c).getInt("q_secs", 10)
    fun setQuoteSeconds(c: Context, v: Int) = p(c).edit().putInt("q_secs", v).apply()

    // --- Actualizaciones: repositorio de GitHub con formato "usuario/repo" ---
    fun updateRepo(c: Context): String = p(c).getString("update_repo", "") ?: ""
    fun setUpdateRepo(c: Context, v: String) = p(c).edit().putString("update_repo", v.trim()).apply()

    // --- Fondo de bloqueo y cambio de frase ---
    /** Altura de la frase en pantalla, en % (10 = arriba, 90 = abajo). */
    fun quoteY(c: Context): Int = p(c).getInt("q_y", 40)
    fun setQuoteY(c: Context, v: Int) = p(c).edit().putInt("q_y", v).apply()

    /** Cada cuántos minutos cambia la frase. 0 = cada vez que se prende la pantalla. */
    fun rotateMinutes(c: Context): Int = p(c).getInt("rotate_min", 60)
    fun setRotateMinutes(c: Context, v: Int) = p(c).edit().putInt("rotate_min", v).apply()

    /** 0 = todas, 1 = motivacionales, 2 = reflexivas, 3 = religiosas */
    fun quoteFilter(c: Context): Int = p(c).getInt("q_filter", 0)
    fun setQuoteFilter(c: Context, v: Int) = p(c).edit().putInt("q_filter", v).apply()

    /** Fondo del wallpaper: 0 = negro, 1 = degradado verde, 2 = foto elegida. */
    fun bgMode(c: Context): Int = p(c).getInt("bg_mode", 1)
    fun setBgMode(c: Context, v: Int) = p(c).edit().putInt("bg_mode", v).apply()

    fun bgVersion(c: Context): Int = p(c).getInt("bg_version", 0)
    fun setBgVersion(c: Context, v: Int) = p(c).edit().putInt("bg_version", v).apply()
}
