package com.poted.vaultnotify

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import com.poted.vaultnotify.notifications.VaultNotifyListenerService

class VaultNotifyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        wymusPonowneZbindowanieListenera()
    }

    /**
     * One UI potrafi ubić bindowanie NotificationListenerService bez wywołania
     * onListenerDisconnected(). Przełączenie komponentu disabled -> enabled przy
     * każdym starcie aplikacji wymusza ponowne zbindowanie przez system.
     */
    private fun wymusPonowneZbindowanieListenera() {
        val komponent = ComponentName(this, VaultNotifyListenerService::class.java)
        packageManager.setComponentEnabledSetting(
            komponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            komponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
