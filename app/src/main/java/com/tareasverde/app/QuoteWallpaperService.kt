package com.tareasverde.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.SurfaceHolder

/**
 * Fondo de pantalla animado que dibuja la frase directamente en el fondo. Si se elige como
 * fondo de la pantalla de bloqueo, la frase queda fija ahí (como el reloj), no tapa nada y
 * cambia sola según el intervalo configurado en Ajustes.
 */
class QuoteWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = QuoteEngine()

    inner class QuoteEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var w = 0
        private var h = 0
        private var bg: Bitmap? = null
        private var bgVersion = -1

        private val tick = object : Runnable {
            override fun run() {
                if (!visible) return
                QuoteRotator.rotateIfDue(this@QuoteWallpaperService)
                draw()
                handler.postDelayed(this, 60_000L)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            w = width
            h = height
            bgVersion = -1
            draw()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            handler.removeCallbacks(tick)
            if (visible) {
                QuoteRotator.rotateIfDue(this@QuoteWallpaperService, onScreenOn = true)
                draw()
                handler.postDelayed(tick, 60_000L)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(tick)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(tick)
            super.onDestroy()
        }

        private fun draw() {
            if (w == 0 || h == 0) return
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) paintScene(canvas, this@QuoteWallpaperService)
            } catch (e: Exception) {
                // La superficie puede no estar lista todavía: se vuelve a intentar en el próximo ciclo.
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        // nada que hacer
                    }
                }
            }
        }

        private fun paintScene(c: Canvas, ctx: Context) {
            // ── Fondo ──
            when (AppPrefs.bgMode(ctx)) {
                2 -> {
                    c.drawColor(Color.BLACK)
                    val version = AppPrefs.bgVersion(ctx)
                    if (bg == null || bgVersion != version) {
                        bg = WallpaperBg.load(ctx, w, h)
                        bgVersion = version
                    }
                    val b = bg
                    if (b != null) {
                        val scale = maxOf(w.toFloat() / b.width, h.toFloat() / b.height)
                        val m = Matrix().apply {
                            postScale(scale, scale)
                            postTranslate((w - b.width * scale) / 2f, (h - b.height * scale) / 2f)
                        }
                        c.drawBitmap(b, m, Paint(Paint.FILTER_BITMAP_FLAG))
                    }
                }
                1 -> {
                    c.drawColor(Color.BLACK)
                    val p = Paint().apply {
                        shader = RadialGradient(
                            w / 2f, h * 0.62f, h * 0.7f,
                            intArrayOf(0xFF0F5A33.toInt(), 0xFF04150C.toInt(), Color.BLACK),
                            floatArrayOf(0f, 0.55f, 1f),
                            Shader.TileMode.CLAMP,
                        )
                    }
                    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
                }
                else -> c.drawColor(Color.BLACK)
            }

            // ── Frase ──
            val q = QuoteRotator.current(ctx)
            val dm = resources.displayMetrics
            val sizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, AppPrefs.quoteSize(ctx).toFloat(), dm,
            )
            val textColor = AppPrefs.quoteColor(ctx)
            val useShadow = AppPrefs.quoteShadow(ctx)
            val margin = (28 * dm.density).toInt()
            val textWidth = (w - 2 * margin).coerceAtLeast(100)

            fun paintFor(px: Float): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = px
                if (useShadow) setShadowLayer(10f, 0f, 2f, Color.BLACK)
            }

            fun layoutFor(text: String, p: TextPaint): StaticLayout =
                StaticLayout.Builder.obtain(text, 0, text.length, p, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.15f)
                    .build()

            val quoteLayout = layoutFor("“${q.text}”", paintFor(sizePx))
            val authorLayout = layoutFor(
                "— ${q.author}",
                paintFor((sizePx * 0.55f).coerceAtLeast(12f * dm.density)),
            )

            val gap = 12f * dm.density
            val blockHeight = quoteLayout.height + gap + authorLayout.height
            var top = h * AppPrefs.quoteY(ctx) / 100f - blockHeight / 2f
            top = top.coerceIn(0f, (h - blockHeight).coerceAtLeast(0f))

            c.save()
            c.translate(margin.toFloat(), top)
            quoteLayout.draw(c)
            c.restore()

            c.save()
            c.translate(margin.toFloat(), top + quoteLayout.height + gap)
            authorLayout.draw(c)
            c.restore()
        }
    }
}
