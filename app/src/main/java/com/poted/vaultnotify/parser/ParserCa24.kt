package com.poted.vaultnotify.parser

import com.poted.vaultnotify.data.Wydatek
import com.poted.vaultnotify.notifications.TYTUL_CA24

private const val PREFIKS_BLOKADY = "Blokada"

private val WZORZEC_KWOTY = Regex("Kwota:\\s*([0-9][0-9\\s\\u00A0]*,\\d{2})\\s*PLN")

/**
 * Przez paczke Wiadomosci przechodza WSZYSTKIE SMS-y uzytkownika, dlatego
 * filtr po tytule "CA24" musi byc pierwszym warunkiem, przed jakimkolwiek
 * parsowaniem tresci. Sposrod wiadomosci CA24 przetwarzamy wylacznie te,
 * ktore zaczynaja sie od "Blokada" - rozliczenie tej samej transakcji
 * przychodzi pozniej jako osobny SMS i ma zostac zignorowane.
 *
 * W tekscie sa dwie kwoty (transakcji i salda dostepnego) - regex jest
 * zakotwiczony na literalnym "Kwota:", ktore nie wystepuje w etykiecie
 * "Saldo dostepne:", wiec nie da sie zlapac salda przez pomylke.
 */
internal fun parseCa24(tytul: String, tekst: String, czas: Long): Wydatek? {
    if (tytul != TYTUL_CA24) return null
    if (!tekst.trimStart().startsWith(PREFIKS_BLOKADY)) return null

    val kwota = WZORZEC_KWOTY.find(tekst)?.groupValues?.get(1)?.let(::sparsujKwotePlN)
    return Wydatek.utworz(kwota = kwota, zrodlo = "CA24", czas = czas, surowyTekst = tekst)
}
