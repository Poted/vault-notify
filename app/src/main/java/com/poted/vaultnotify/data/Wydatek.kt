package com.poted.vaultnotify.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest
import java.util.Locale

@Entity(indices = [Index(value = ["klucz"], unique = true)])
data class Wydatek(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val klucz: String,
    val czas: Long,
    val kwota: Double?,
    val zrodlo: String,
    val surowyTekst: String,
    val status: Status = Status.NOWY
) {
    companion object {
        private const val MILISEKUND_NA_MINUTE = 60_000L

        /**
         * Klucz musi zawierać [czas], nie sam dzień — dwie identyczne kwoty
         * z tego samego źródła i dnia, ale o różnej minucie, to dwie różne transakcje.
         */
        fun utworz(kwota: Double?, zrodlo: String, czas: Long, surowyTekst: String): Wydatek {
            return Wydatek(
                klucz = generujKlucz(kwota, zrodlo, czas),
                czas = czas,
                kwota = kwota,
                zrodlo = zrodlo,
                surowyTekst = surowyTekst
            )
        }

        fun generujKlucz(kwota: Double?, zrodlo: String, czas: Long): String {
            val czasZaokraglony = czas - (czas % MILISEKUND_NA_MINUTE)
            val kwotaKanoniczna = kwota?.let { String.format(Locale.ROOT, "%.2f", it) } ?: "null"
            val surowyKlucz = "$zrodlo|$czasZaokraglony|$kwotaKanoniczna"
            val skrot = MessageDigest.getInstance("SHA-256").digest(surowyKlucz.toByteArray(Charsets.UTF_8))
            return skrot.joinToString(separator = "") { "%02x".format(it) }
        }
    }
}
