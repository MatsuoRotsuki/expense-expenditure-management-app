package com.hedspi.expensemanagement

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hedspi.expensemanagement.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddTransactionActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        val labelInput = binding.labelInput
        val amountInput = binding.amountInput
        val labelLayout = binding.labelLayout
        val amountLayout = binding.amountLayout
        val descriptionInput = binding.descriptionInput
        val dateInput = binding.dateInput

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        labelInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                amountLayout.error = null
        }

        val addTransactionBtn = binding.addTransactionBtn
        addTransactionBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descriptionInput.text.toString()
            val date = dateInput.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(date, formatter).toString()

            if (label.isEmpty())
                labelLayout.error = "Please enter a valid label"

            else if (amount == null)
                amountLayout.error = "Please enter a valid amount"

            else {
                val transaction = Transaction(0, label, amount, description, localDate)
                insert(transaction)
            }

        }

        val closeBtn = binding.closeBtn
        closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dateInput = binding.dateInput

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                var selectedDate : String

                if (month < 10 && dayOfMonth < 10)
                    selectedDate = "$year-0${month + 1}-0$dayOfMonth"
                else if (month < 10)
                    selectedDate = "$year-0${month + 1}-$dayOfMonth"
                else if (dayOfMonth < 10)
                    selectedDate = "$year-0${month + 1}-$dayOfMonth"
                else
                    selectedDate = "$year-${month + 1}-$dayOfMonth"

                dateInput.setText(selectedDate)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun insert(transaction: Transaction) {
        val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Perform migration steps here (e.g., CREATE TABLE, ALTER TABLE)
                database.execSQL("CREATE TABLE IF NOT EXISTS new_transactions (id INTEGER PRIMARY KEY NOT NULL," +
                        "label TEXT NOT NULL," +
                        "amount REAL NOT NULL, " +
                        "description TEXT, " +
                        "transactionDate TEXT NOT NULL)");

                // Migrate data from old_transactions to new_transactions
                database.execSQL("INSERT INTO new_transactions (label, amount, description, transactionDate) SELECT label, amount, description, transactionDate FROM transactions");

                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS transactions");

                // Rename the new table to the original table name
                database.execSQL("ALTER TABLE new_transactions RENAME TO transactions");
            }
        }

        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions")
            .addMigrations(migration_3_4)
            .build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }


}
