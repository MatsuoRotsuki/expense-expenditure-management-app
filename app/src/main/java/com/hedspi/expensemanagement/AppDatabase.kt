package com.hedspi.expensemanagement

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

@TypeConverters(DateConverter::class)
@Database(entities = [Transaction::class, User::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao
    abstract fun userDao() : UserDao
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}