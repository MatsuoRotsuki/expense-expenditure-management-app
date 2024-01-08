package com.hedspi.expensemanagement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE transactionDate = :date")
    fun getTransByDate(date: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE strftime('%W', transactionDate) = :week AND strftime('%Y', transactionDate) = :year")
    fun getTransByWeek(year: String, week: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE strftime('%m', transactionDate) = :month AND strftime('%Y', transactionDate) = :year")
    fun getTransByMonth(year: String, month: String): List<Transaction>

    @Query("SELECT * FROM transactions")
    fun getAll(): List<Transaction>
    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)
}