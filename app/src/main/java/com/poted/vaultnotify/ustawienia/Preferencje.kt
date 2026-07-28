package com.poted.vaultnotify.ustawienia

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ustawienia")

class Preferencje(private val context: Context) {

    private object Klucze {
        val DIAGNOSTYKA = booleanPreferencesKey("diagnostyka_wlaczona")
        val LICZNIK_POWIADOMIEN = intPreferencesKey("licznik_powiadomien")
        val OSTATNIE_POWIADOMIENIE_CZAS = longPreferencesKey("ostatnie_powiadomienie_czas")
        val OSTATNIA_SYNCHRONIZACJA_CZAS = longPreferencesKey("ostatnia_synchronizacja_czas")
    }

    val diagnostykaWlaczona: Flow<Boolean> =
        context.dataStore.data.map { it[Klucze.DIAGNOSTYKA] ?: false }

    val licznikPowiadomien: Flow<Int> =
        context.dataStore.data.map { it[Klucze.LICZNIK_POWIADOMIEN] ?: 0 }

    val ostatniePowiadomienieCzas: Flow<Long?> =
        context.dataStore.data.map { it[Klucze.OSTATNIE_POWIADOMIENIE_CZAS] }

    val ostatniaSynchronizacjaCzas: Flow<Long?> =
        context.dataStore.data.map { it[Klucze.OSTATNIA_SYNCHRONIZACJA_CZAS] }

    suspend fun ustawDiagnostyke(wlaczona: Boolean) {
        context.dataStore.edit { it[Klucze.DIAGNOSTYKA] = wlaczona }
    }

    suspend fun ustawOstatniaSynchronizacje(czas: Long) {
        context.dataStore.edit { it[Klucze.OSTATNIA_SYNCHRONIZACJA_CZAS] = czas }
    }

    suspend fun zarejestrujOdebranePowiadomienie(czas: Long) {
        context.dataStore.edit { prefs ->
            val obecny = prefs[Klucze.LICZNIK_POWIADOMIEN] ?: 0
            prefs[Klucze.LICZNIK_POWIADOMIEN] = obecny + 1
            prefs[Klucze.OSTATNIE_POWIADOMIENIE_CZAS] = czas
        }
    }
}
