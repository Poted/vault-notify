package com.poted.vaultnotify.parser

import com.poted.vaultnotify.data.Wydatek

private const val TYTUL_AUTORYZACJI = "Autoryzacja transakcji kartą"

private val WZORZEC_KWOTY = Regex("Kwota:\\s*([0-9][0-9\\s\\u00A0]*,\\d{2})\\s*PLN")

/**
 * IKO wysyla rozne powiadomienia (autoryzacje, marketing, oferty) - tylko
 * tytul "Autoryzacja transakcji karta" oznacza transakcje kartowa. Wszystko
 * inne z tej paczki to nie transakcja.
 */
internal fun parseIko(tytul: String, tekst: String, czas: Long): Wydatek? {
    if (!tytul.trim().equals(TYTUL_AUTORYZACJI, ignoreCase = true)) return null

    val kwota = WZORZEC_KWOTY.find(tekst)?.groupValues?.get(1)?.let(::sparsujKwotePlN)
    return Wydatek.utworz(kwota = kwota, zrodlo = "IKO", czas = czas, surowyTekst = tekst)
}
