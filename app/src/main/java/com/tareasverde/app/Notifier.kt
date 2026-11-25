package com.tareasverde.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate

object Notifier {
    const val CH_REMINDERS = "recordatorios"
    const val ID_REMINDER = 1001

    private const val GREEN = 0xFF00E676

    fun createChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_REMINDERS, "Recordatorios de tareas", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    fun openApp(ctx: Context): PendingIntent {
        val intent = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun canNotify(ctx: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }

    /**
     * Resumen diario: tareas sin tachar que tienen fecha límite (o son "Diaria").
     * Devuelve true si se mostró una notificación.
     */
    @SuppressLint("MissingPermission")
    fun showReminder(ctx: Context): Boolean {
        if (!canNotify(ctx)) return false
        val today = LocalDate.now().toEpochDay()
        val pending = TaskStore.load(ctx)
            .filter { !it.done && (it.dueEpochDay != null || it.category == Category.DIARIA) }
            .sortedBy { it.dueEpochDay ?: Long.MAX_VALUE }
        if (pending.isEmpty()) return false

        val lines = pending.take(6).map { t ->
            val extra = if (t.dueEpochDay != null) dueLabel(t.dueEpochDay, today) else "diaria"
            "• ${t.title} — $extra"
        }
        val style = NotificationCompat.InboxStyle().also { s -> lines.forEach { s.addLine(it) } }
        if (pending.size > 6) style.setSummaryText("y ${pending.size - 6} más")

        val title = if (pending.size == 1) "Tenés 1 tarea pendiente" else "Tenés ${pending.size} tareas pendientes"
        val n = NotificationCompat.Builder(ctx, CH_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(lines.first())
            .setStyle(style)
            .setColor(GREEN.toInt())
            .setContentIntent(openApp(ctx))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(ID_REMINDER, n)
        return true
    }
}
