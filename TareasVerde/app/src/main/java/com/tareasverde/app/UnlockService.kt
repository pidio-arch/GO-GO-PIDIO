package com.tareasverde.app

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Servicio en primer plano que escucha el encendido de la pantalla (ACTION_SCREEN_ON) y muestra
 * la frase sobre la pantalla de bloqueo. Android no permite registrar este evento en el manifest,
 * por eso hace falta un servicio activo con su aviso permanente.
 */
class UnlockService : Service() {

    private var lastShown = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val now = System.currentTimeMillis()
            // Evita repetir la frase si la pantalla se prende varias veces seguidas.
            if (now - lastShown > 20_000L) {
                lastShown = now
                QuoteDisplay.show(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Notifier.createChannels(this)
        ContextCompat.registerReceiver(
            this, receiver, IntentFilter(Intent.ACTION_SCREEN_ON),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val n = NotificationCompat.Builder(this, Notifier.CH_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("GO GO PIDIO · frases activas")
            .setContentText("Verás una frase en la pantalla de bloqueo al prender el celular")
            .setOngoing(true)
            .setContentIntent(Notifier.openApp(this))
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(Notifier.ID_SERVICE, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(Notifier.ID_SERVICE, n)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, UnlockService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, UnlockService::class.java))
        }
    }
}
