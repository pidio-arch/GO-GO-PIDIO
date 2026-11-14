package com.tareasverde.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Pantalla transparente que se muestra por encima de la pantalla de bloqueo, solo con la frase
 * (sin fondo), con el tamaño, color, sombra y posición elegidos en Ajustes.
 * Se cierra tocando, al apagar la pantalla, al desbloquear o sola pasados unos segundos.
 */
class QuoteActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        NotificationManagerCompat.from(this).cancel(Notifier.ID_LOCK)

        val q = Quotes.random()
        val size = AppPrefs.quoteSize(this).toFloat()
        val color = AppPrefs.quoteColor(this)
        val shadow = AppPrefs.quoteShadow(this)
        val pos = AppPrefs.quotePosition(this)
        val seconds = AppPrefs.quoteSeconds(this)
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        fun style(tv: TextView) {
            tv.setTextColor(color)
            tv.gravity = Gravity.CENTER_HORIZONTAL
            if (shadow) tv.setShadowLayer(10f, 0f, 2f, Color.BLACK)
        }

        val quote = TextView(this).apply {
            text = "“${q.text}”"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            setLineSpacing(0f, 1.15f)
            style(this)
        }
        val author = TextView(this).apply {
            text = "— ${q.author}"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, (size * 0.55f).coerceAtLeast(12f))
            style(this)
        }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(quote, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            addView(author, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) })
        }

        val gravity = when (pos) {
            0 -> Gravity.TOP
            2 -> Gravity.BOTTOM
            else -> Gravity.CENTER_VERTICAL
        }
        val root = FrameLayout(this).apply {
            // Sin fondo: totalmente transparente.
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(28), dp(96), dp(28), dp(96))
            addView(column, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity
            ))
            setOnClickListener { finish() }
        }
        setContentView(root)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        ContextCompat.registerReceiver(this, closeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        handler.postDelayed({ finish() }, seconds * 1000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        runCatching { unregisterReceiver(closeReceiver) }
        super.onDestroy()
    }
}

/** Punto único para mostrar la frase: pantalla de bloqueo si se puede, notificación si no. */
object QuoteDisplay {
    fun show(ctx: Context) {
        val app = ctx.applicationContext
        val fullScreenOk = Perms.fullScreenIntent(app) && Notifier.canNotify(app)

        // Vía recomendada por Android: notificación con "pantalla completa" sobre el bloqueo.
        if (fullScreenOk) Notifier.showLockScreenQuote(app)

        // Vía directa: funciona si el permiso "Mostrar sobre otras apps" está activo.
        runCatching {
            app.startActivity(
                Intent(app, QuoteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        // Sin ningún permiso de los dos, al menos una notificación normal.
        if (!fullScreenOk && !Perms.overlay(app)) Notifier.showQuote(app)
    }
}
