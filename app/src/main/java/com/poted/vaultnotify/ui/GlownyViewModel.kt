package com.poted.vaultnotify.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.poted.vaultnotify.data.AppDatabase
import com.poted.vaultnotify.data.Status
import com.poted.vaultnotify.notifications.czyUprawnienieListeneraWlaczone
import com.poted.vaultnotify.sync.uruchomRecznaSynchronizacje
import com.poted.vaultnotify.ustawienia.Preferencje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GlownyViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencje = Preferencje(application)
    private val dao = AppDatabase.pobierz(application).wydatekDao()

    private val _uprawnienieWlaczone = MutableStateFlow(czyUprawnienieListeneraWlaczone(application))
    val uprawnienieWlaczone: StateFlow<Boolean> = _uprawnienieWlaczone

    val diagnostykaWlaczona: StateFlow<Boolean> = preferencje.diagnostykaWlaczona
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val licznikPowiadomien: StateFlow<Int> = preferencje.licznikPowiadomien
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val ostatniePowiadomienieCzas: StateFlow<Long?> = preferencje.ostatniePowiadomienieCzas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val ostatniaSynchronizacjaCzas: StateFlow<Long?> = preferencje.ostatniaSynchronizacjaCzas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val liczbaOczekujacych: StateFlow<Int> = dao.policzWedlugStatusu(Status.NOWY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Wywoływane przy każdym wejściu na ekran (nie tylko raz), bo One UI potrafi
     *  cofnąć uprawnienie w tle bez powiadomienia aplikacji. */
    fun odswiezUprawnienie() {
        _uprawnienieWlaczone.value = czyUprawnienieListeneraWlaczone(getApplication())
    }

    fun przelaczDiagnostyke(wlaczona: Boolean) {
        viewModelScope.launch { preferencje.ustawDiagnostyke(wlaczona) }
    }

    fun uruchomSynchronizacje() {
        uruchomRecznaSynchronizacje(getApplication())
    }
}
