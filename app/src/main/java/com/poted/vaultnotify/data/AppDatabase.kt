package com.poted.vaultnotify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Wydatek::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wydatekDao(): WydatekDao

    companion object {
        @Volatile
        private var instancja: AppDatabase? = null

        fun pobierz(context: Context): AppDatabase =
            instancja ?: synchronized(this) {
                instancja ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vault-notify.db"
                ).build().also { instancja = it }
            }
    }
}
