package com.poted.vaultnotify.parser

import org.junit.Assert.assertNull
import org.junit.Test

class SparsujTest {

    @Test
    fun `nieznana paczka zwraca null`() {
        val wynik = sparsuj("com.example.inna.apka", "Cokolwiek", "treść", 10_000L)

        assertNull(wynik)
    }
}
