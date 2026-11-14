package com.tareasverde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.createChannels(this)
        ReminderScheduler.schedule(this)
        if (AppPrefs.unlockQuotes(this)) UnlockService.start(this)
        setContent {
            AppTheme { App() }
        }
    }
}
