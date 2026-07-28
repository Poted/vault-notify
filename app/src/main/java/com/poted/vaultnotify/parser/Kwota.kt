package com.poted.vaultnotify.parser

private val WZORZEC_BIALYCH_ZNAKOW = Regex("[\\s\\u00A0]")

/**
 * Zamienia polski zapis kwoty ("15,48", "1 234,56" ze zwykla spacja lub
 * spacja nierozdzielajaca jako separatorem tysiecy) na Double. Zwraca null,
 * jesli po oczyszczeniu tekst nie jest liczba - parser wyzej traktuje to
 * tak samo jak brak dopasowania regexu (kwota = null).
 */
internal fun sparsujKwotePlN(kwotaTekst: String): Double? {
    val oczyszczona = kwotaTekst
        .replace(WZORZEC_BIALYCH_ZNAKOW, "")
        .replace(",", ".")
    return oczyszczona.toDoubleOrNull()
}
