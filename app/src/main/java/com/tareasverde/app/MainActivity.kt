package com.tareasverde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.createChannels(this)
        ReminderScheduler.schedule(this)
        QuoteWidget.scheduleAlarm(this)
        setContent {
            AppTheme { App() }
        }
    }
}
