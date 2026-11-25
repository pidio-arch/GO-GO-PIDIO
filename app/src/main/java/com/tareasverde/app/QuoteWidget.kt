package com.tareasverde.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews

/**
 * Widget transparente con la frase (sin fondo, como el reloj). Se puede poner en la pantalla
 * de inicio y, en los celulares que permiten widgets en la pantalla de bloqueo, también ahí.
 * Cambia solo con el mismo intervalo que el fondo de bloqueo.
 */
class QuoteWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { render(context, appWidgetManager, it) }
        scheduleAlarm(context)
    }

    override fun onEnabled(context: Context) {
        scheduleAlarm(context)
    }

    override fun onDisabled(context: Context) {
        cancelAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ROTATE) {
            if (AppPrefs.rotateMinutes(context) == 0) {
                QuoteRotator.rotate(context)
            } else {
                QuoteRotator.rotateIfDue(context, grace = 0.8)
            }
            refreshAll(context)
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        const val ACTION_ROTATE = "com.tareasverde.app.ROTATE_QUOTE"

        /** Vuelve a dibujar todos los widgets puestos (tras cambiar frase o aspecto). */
        fun refreshAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, QuoteWidget::class.java))
            ids.forEach { render(ctx, mgr, it) }
        }

        private fun render(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val q = QuoteRotator.current(ctx)
            val size = (AppPrefs.quoteSize(ctx) * 0.6f).coerceAtLeast(12f)
            val color = AppPrefs.quoteColor(ctx)
            val views = RemoteViews(ctx.packageName, R.layout.widget_quote)
            views.setTextViewText(R.id.w_quote, "“${q.text}”")
            views.setTextViewText(R.id.w_author, "— ${q.author}")
            views.setTextViewTextSize(R.id.w_quote, TypedValue.COMPLEX_UNIT_SP, size)
            views.setTextViewTextSize(R.id.w_author, TypedValue.COMPLEX_UNIT_SP, (size * 0.6f).coerceAtLeast(10f))
            views.setTextColor(R.id.w_quote, color)
            views.setTextColor(R.id.w_author, color)
            views.setOnClickPendingIntent(R.id.w_root, Notifier.openApp(ctx))
            mgr.updateAppWidget(id, views)
        }

        private fun alarmIntent(ctx: Context): PendingIntent =
            PendingIntent.getBroadcast(
                ctx, 2,
                Intent(ctx, QuoteWidget::class.java).setAction(ACTION_ROTATE),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        /** Programa el cambio periódico solo si hay algún widget puesto. */
        fun scheduleAlarm(ctx: Context) {
            val am = ctx.getSystemService(AlarmManager::class.java)
            val hasWidgets = AppWidgetManager.getInstance(ctx)
                .getAppWidgetIds(ComponentName(ctx, QuoteWidget::class.java)).isNotEmpty()
            if (!hasWidgets) {
                am.cancel(alarmIntent(ctx))
                return
            }
            val minutes = AppPrefs.rotateMinutes(ctx).let { if (it == 0) 15 else it }
            val interval = minutes * 60_000L
            am.setInexactRepeating(
                AlarmManager.RTC, System.currentTimeMillis() + interval, interval, alarmIntent(ctx),
            )
        }

        fun cancelAlarm(ctx: Context) {
            ctx.getSystemService(AlarmManager::class.java).cancel(alarmIntent(ctx))
        }
    }
}
