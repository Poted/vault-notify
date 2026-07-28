package com.poted.vaultnotify.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ParserCa24Test {

    private val paczkaWiadomosci = "com.samsung.android.messaging"

    @Test
    fun `dosłowny SMS blokady z dwiema kwotami bierze kwotę transakcji, nie saldo`() {
        val tekst = "Blokada na rachunku Konto:4XXXXXX000 Kwota:5,00PLN " +
            "Data:2026-07-28 12:26 Saldo dostepne:514,82PLN"

        val wynik = sparsuj(paczkaWiadomosci, "CA24", tekst, 5_000L)

        assertNotNull(wynik)
        assertEquals(5.00, wynik!!.kwota)
        assertEquals("CA24", wynik.zrodlo)
        assertEquals(tekst, wynik.surowyTekst)
    }

    @Test
    fun `SMS rozliczenia, nie blokady, jest ignorowany`() {
        val tekst = "Rozliczenie transakcji Konto:4XXXXXX000 Kwota:5,00PLN " +
            "Data:2026-07-28 12:30 Saldo dostepne:509,82PLN"

        val wynik = sparsuj(paczkaWiadomosci, "CA24", tekst, 6_000L)

        assertNull(wynik)
    }

    @Test
    fun `SMS z innym tytułem niż CA24 jest ignorowany mimo pasującej treści`() {
        val tekst = "Blokada na rachunku Konto:4XXXXXX000 Kwota:5,00PLN " +
            "Data:2026-07-28 12:26 Saldo dostepne:514,82PLN"

        val wynik = sparsuj(paczkaWiadomosci, "Mama", tekst, 7_000L)

        assertNull(wynik)
    }

    @Test
    fun `nieznany format blokady daje wpis z kwotą null`() {
        val tekst = "Blokada na rachunku Konto:4XXXXXX000 - szczegóły w aplikacji CA24"

        val wynik = sparsuj(paczkaWiadomosci, "CA24", tekst, 8_000L)

        assertNotNull(wynik)
        assertNull(wynik!!.kwota)
        assertEquals(tekst, wynik.surowyTekst)
        assertEquals("CA24", wynik.zrodlo)
    }

    @Test
    fun `kod logowania z tytułem CA24 nie zaczyna się od Blokada i jest ignorowany`() {
        val tekst = "Twoj kod jednorazowy do logowania: 123456. Nie udostepniaj go nikomu."

        val wynik = sparsuj(paczkaWiadomosci, "CA24", tekst, 9_000L)

        assertNull(wynik)
    }
}
