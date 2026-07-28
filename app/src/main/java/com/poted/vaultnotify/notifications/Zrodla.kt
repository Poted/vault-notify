package com.poted.vaultnotify.notifications

const val PACZKA_IKO = "pl.pkobp.iko"

const val TYTUL_CA24 = "CA24"

/**
 * Nazwa paczki aplikacji Wiadomości na Samsungu bywa inna niż na czystym
 * Androidzie zależnie od tego, czy użytkownik używa domyślnej apki Samsunga,
 * czy Google Messages jako domyślnej. Obie są tu ujęte — tryb diagnostyczny
 * (packageName w logu) pozwoli ustalić, która faktycznie dostarcza SMS-y CA24
 * na tym urządzeniu, i zawęzić listę, jeśli okaże się potrzebne.
 */
val PACZKI_WIADOMOSCI = setOf(
    "com.samsung.android.messaging",
    "com.google.android.apps.messaging"
)

fun czyMonitorowaneZrodlo(paczka: String, tytul: String): Boolean {
    if (paczka == PACZKA_IKO) return true
    if (paczka in PACZKI_WIADOMOSCI && tytul == TYTUL_CA24) return true
    return false
}
