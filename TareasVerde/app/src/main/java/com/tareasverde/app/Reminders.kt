package com.tareasverde.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Programa el próximo aviso diario a la hora elegida en Ajustes. Es una alarma de una sola
 * vez: cada vez que suena (o al abrir la app / reiniciar) se vuelve a programar la siguiente.
 * Usa alarma exacta si el permiso está concedido; si no, una alarma aproximada.
 */
object ReminderScheduler {
    fun schedule(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, AppPrefs.reminderHour(ctx))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, 0, Intent(ctx, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Misma PendingIntent => reemplaza la alarma anterior.
        if (Perms.exactAlarms(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifier.createChannels(context)
        Notifier.showReminder(context)
        ReminderScheduler.schedule(context) // deja programado el aviso de mañana
    }
}

/**
 * Tras reiniciar el celular, actualizar la app o cambiar el permiso de alarmas exactas,
 * vuelve a programar el aviso diario y el servicio de frases.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifier.createChannels(context)
        ReminderScheduler.schedule(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        if (AppPrefs.unlockQuotes(context)) UnlockService.start(context)
    }
}
