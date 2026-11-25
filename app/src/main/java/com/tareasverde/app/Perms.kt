package com.tareasverde.app

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager

/** Estado de los permisos que la app necesita y los intents para pedirlos. */
object Perms {
    fun notifications(ctx: Context): Boolean = Notifier.canNotify(ctx)

    fun batteryUnrestricted(ctx: Context): Boolean =
        ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName)

    fun exactAlarms(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 31 || ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun pkgUri(ctx: Context) = Uri.parse("package:${ctx.packageName}")

    fun batteryIntent(ctx: Context) =
        Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkgUri(ctx))

    fun batteryFallbackIntent() =
        Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    fun exactAlarmIntent(ctx: Context) =
        Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, pkgUri(ctx))

    fun notificationSettingsIntent(ctx: Context) =
        Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, ctx.packageName)

    fun installUnknownAppsIntent(ctx: Context) =
        Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, pkgUri(ctx))

    /** Abre una pantalla de ajustes; devuelve false si el celular no la tiene. */
    fun open(ctx: Context, intent: Intent): Boolean =
        runCatching { ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
}
