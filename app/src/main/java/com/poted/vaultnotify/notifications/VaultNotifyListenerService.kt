package com.poted.vaultnotify.notifications

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.poted.vaultnotify.data.AppDatabase
import com.poted.vaultnotify.parser.sparsuj
import com.poted.vaultnotify.ustawienia.Preferencje
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VaultNotifyListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var preferencje: Preferencje

    @Volatile
    private var diagnostykaWlaczona = false

    override fun onCreate() {
        super.onCreate()
        preferencje = Preferencje(applicationContext)
        scope.launch {
            preferencje.diagnostykaWlaczona.collect { diagnostykaWlaczona = it }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener podłączony")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Listener odłączony przez system — żądam ponownego podłączenia")
        requestRebind(ComponentName(this, VaultNotifyListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val paczka = sbn.packageName
        val extras = sbn.notification.extras
        val tytul = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val tekst = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val liniePodgladu = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.map { it.toString() }
            ?.filter { it.isNotBlank() }
        val czas = sbn.postTime

        if (diagnostykaWlaczona) {
            Log.d(
                TAG,
                "paczka=$paczka tytul=\"$tytul\" text=\"$tekst\" bigText=\"$bigText\" " +
                    "textLines=$liniePodgladu czas=$czas"
            )
        }

        if (!czyMonitorowaneZrodlo(paczka, tytul)) return

        scope.launch { preferencje.zarejestrujOdebranePowiadomienie(czas) }

        val kandydaci = kandydaciTekstu(tekst, bigText, liniePodgladu)
        if (kandydaci.isEmpty()) return

        scope.launch {
            val dao = AppDatabase.pobierz(applicationContext).wydatekDao()
            kandydaci.forEach { kandydat ->
                val wydatek = sparsuj(paczka, tytul, kandydat, czas) ?: return@forEach
                dao.wstaw(wydatek)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "VaultNotifyListener"

        /**
         * EXTRA_TEXT_LINES ma pierwszeństwo — w powiadomieniach zgrupowanych
         * (kilka SMS-ów zwiniętych w jedno) każda linia to osobna wiadomość i musi
         * zostać sparsowana niezależnie, inaczej "Blokada" i rozliczenie tej samej
         * transakcji zlałyby się w jeden tekst. EXTRA_BIG_TEXT jest kolejny w
         * kolejności, bo IKO ucina treść w widoku zwiniętym (EXTRA_TEXT).
         */
        fun kandydaciTekstu(tekst: String?, bigText: String?, liniePodgladu: List<String>?): List<String> {
            if (!liniePodgladu.isNullOrEmpty()) return liniePodgladu
            if (!bigText.isNullOrBlank()) return listOf(bigText)
            if (!tekst.isNullOrBlank()) return listOf(tekst)
            return emptyList()
        }
    }
}
