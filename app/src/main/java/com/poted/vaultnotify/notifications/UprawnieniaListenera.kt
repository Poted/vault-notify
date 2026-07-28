package com.poted.vaultnotify.notifications

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

fun czyUprawnienieListeneraWlaczone(context: Context): Boolean {
    val wlaczonePakiety = NotificationManagerCompat.getEnabledListenerPackages(context)
    return context.packageName in wlaczonePakiety
}

fun intentDoUstawienListenera(): Intent =
    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
