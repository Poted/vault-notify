package com.poted.vaultnotify.parser

import com.poted.vaultnotify.data.Status
import com.poted.vaultnotify.notifications.PACZKA_IKO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ParserIkoTest {

    @Test
    fun `dosłowny tekst z IKO daje poprawną kwotę`() {
        val tekst = "Kwota: 15,48 PLN.\nMiejsce: NETTO 4580 SCO K.2, BOCHNIA..."

        val wynik = sparsuj(PACZKA_IKO, "Autoryzacja transakcji kartą", tekst, 1_000L)

        assertNotNull(wynik)
        assertEquals(15.48, wynik!!.kwota)
        assertEquals("IKO", wynik.zrodlo)
        assertEquals(Status.NOWY, wynik.status)
        assertEquals(tekst, wynik.surowyTekst)
    }

    @Test
    fun `kwota z separatorem tysięcy i spacją nierozdzielającą`() {
        val tekst = "Kwota: 1 234,56 PLN.\nMiejsce: JAKIS SKLEP, WARSZAWA..."

        val wynik = sparsuj(PACZKA_IKO, "Autoryzacja transakcji kartą", tekst, 2_000L)

        assertNotNull(wynik)
        assertEquals(1234.56, wynik!!.kwota)
    }

    @Test
    fun `kwota z separatorem tysięcy i zwykłą spacją`() {
        val tekst = "Kwota: 1 234,56 PLN.\nMiejsce: JAKIS SKLEP, WARSZAWA..."

        val wynik = sparsuj(PACZKA_IKO, "Autoryzacja transakcji kartą", tekst, 2_500L)

        assertNotNull(wynik)
        assertEquals(1234.56, wynik!!.kwota)
    }

    @Test
    fun `powiadomienie IKO niebędące transakcją zwraca null`() {
        val tekst = "Sprawdź nową ofertę oszczędnościową dostępną w aplikacji IKO."

        val wynik = sparsuj(PACZKA_IKO, "Nowość w IKO", tekst, 3_000L)

        assertNull(wynik)
    }

    @Test
    fun `nieznany format treści daje wpis z kwotą null, nie null i nie wyjątek`() {
        val tekst = "Autoryzowano płatność. Szczegóły w aplikacji."

        val wynik = sparsuj(PACZKA_IKO, "Autoryzacja transakcji kartą", tekst, 4_000L)

        assertNotNull(wynik)
        assertNull(wynik!!.kwota)
        assertEquals(tekst, wynik.surowyTekst)
        assertEquals("IKO", wynik.zrodlo)
    }
}
