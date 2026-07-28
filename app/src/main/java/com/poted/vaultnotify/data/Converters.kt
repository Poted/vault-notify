package com.poted.vaultnotify.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun statusDoString(status: Status): String = status.name

    @TypeConverter
    fun stringDoStatus(wartosc: String): Status = Status.valueOf(wartosc)
}
