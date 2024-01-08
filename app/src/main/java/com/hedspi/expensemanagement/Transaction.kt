package com.hedspi.expensemanagement

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey (autoGenerate = true) val id: Int,
    val label: String,
    val amount: Double,
    val description: String?=null,
    val transactionDate: String
) : Serializable {

}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val username: String,
    val passwordHash: String,
    val email: String,
) : Serializable {

}

