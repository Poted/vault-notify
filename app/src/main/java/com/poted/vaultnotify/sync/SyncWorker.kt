package com.poted.vaultnotify.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.poted.vaultnotify.BuildConfig
import com.poted.vaultnotify.data.AppDatabase
import com.poted.vaultnotify.data.Status
import com.poted.vaultnotify.data.Wydatek
import com.poted.vaultnotify.ustawienia.Preferencje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Wysyla jedno zadanie POST z tablica WSZYSTKICH niewyslanych pozycji -
 * kazde zadanie HTTP budzi modem na kilkanascie sekund niezaleznie od ilosci
 * danych, wiec batchowanie jest glownym oszczedzaniem baterii w tym projekcie.
 *
 * Status WYSLANY ustawiany jest dopiero po HTTP 200 ORAZ status: "ok" w
 * ciele odpowiedzi - Apps Script Web Apps zawsze zwraca HTTP 200, wiec sam
 * kod odpowiedzi nie wystarczy (patrz backend/README.md).
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao = AppDatabase.pobierz(applicationContext).wydatekDao()
        val doWyslania = dao.pobierzWedlugStatusu(Status.NOWY)

        if (doWyslania.isEmpty()) return@withContext Result.success()

        val sukces = wyslijPaczke(doWyslania)
        if (!sukces) return@withContext Result.retry()

        dao.ustawStatus(doWyslania.map { it.id }, Status.WYSLANY)
        Preferencje(applicationContext).ustawOstatniaSynchronizacje(System.currentTimeMillis())
        Result.success()
    }

    private fun wyslijPaczke(pozycje: List<Wydatek>): Boolean {
        val polaczenie = URL(BuildConfig.SHEETS_URL).openConnection() as HttpURLConnection
        return try {
            polaczenie.requestMethod = "POST"
            polaczenie.doOutput = true
            polaczenie.connectTimeout = 15_000
            polaczenie.readTimeout = 15_000
            polaczenie.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            OutputStreamWriter(polaczenie.outputStream, StandardCharsets.UTF_8).use {
                it.write(zbudujCialoZadania(pozycje))
            }

            val kodOdpowiedzi = polaczenie.responseCode
            val strumien = if (kodOdpowiedzi in 200..299) polaczenie.inputStream else polaczenie.errorStream
            val tresc = strumien?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                ?: return false

            kodOdpowiedzi == HttpURLConnection.HTTP_OK && JSONObject(tresc).optString("status") == "ok"
        } catch (wyjatek: Exception) {
            Log.w(TAG, "Synchronizacja nieudana", wyjatek)
            false
        } finally {
            polaczenie.disconnect()
        }
    }

    private fun zbudujCialoZadania(pozycje: List<Wydatek>): String {
        val tablicaPozycji = JSONArray()
        pozycje.forEach { wydatek ->
            val obiekt = JSONObject()
            obiekt.put("czas", wydatek.czas)
            obiekt.put("kwota", wydatek.kwota ?: JSONObject.NULL)
            obiekt.put("zrodlo", wydatek.zrodlo)
            obiekt.put("surowyTekst", wydatek.surowyTekst)
            tablicaPozycji.put(obiekt)
        }

        return JSONObject()
            .put("token", BuildConfig.SHEETS_TOKEN)
            .put("pozycje", tablicaPozycji)
            .toString()
    }

    companion object {
        private const val TAG = "VaultNotifySync"
        const val NAZWA_OKRESOWA = "synchronizacja_okresowa"
        const val NAZWA_RECZNA = "synchronizacja_reczna"
    }
}
