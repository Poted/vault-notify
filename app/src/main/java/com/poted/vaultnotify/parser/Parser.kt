package com.poted.vaultnotify.parser

import com.poted.vaultnotify.data.Wydatek
import com.poted.vaultnotify.notifications.PACZKA_IKO
import com.poted.vaultnotify.notifications.PACZKI_WIADOMOSCI

/**
 * Wspolny interfejs parsera. Zwraca null, gdy powiadomienie nie jest
 * transakcja. Gdy powiadomienie JEST transakcja, ale kwoty nie udalo sie
 * wyciagnac, zwraca Wydatek z kwota = null i pelnym surowym tekstem -
 * to jedyny sposob, zeby zauwazyc zmiane formatu powiadomien banku.
 */
fun sparsuj(paczka: String, tytul: String, tekst: String, czas: Long): Wydatek? =
    when (paczka) {
        PACZKA_IKO -> parseIko(tytul, tekst, czas)
        in PACZKI_WIADOMOSCI -> parseCa24(tytul, tekst, czas)
        else -> null
    }
