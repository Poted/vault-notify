package com.poted.vaultnotify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WydatekDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun wstaw(wydatek: Wydatek): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun wstawWszystkie(wydatki: List<Wydatek>): List<Long>

    @Query("SELECT * FROM Wydatek WHERE status = :status ORDER BY czas ASC")
    suspend fun pobierzWedlugStatusu(status: Status): List<Wydatek>

    @Query("SELECT COUNT(*) FROM Wydatek WHERE status = :status")
    suspend fun policzWedlugStatusu(status: Status): Int

    @Query("UPDATE Wydatek SET status = :status WHERE id IN (:idki)")
    suspend fun ustawStatus(idki: List<Long>, status: Status)

    @Query("DELETE FROM Wydatek WHERE status = :status AND czas < :przed")
    suspend fun usunStarszeNiz(status: Status, przed: Long)
}
