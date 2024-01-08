package com.hedspi.expensemanagement

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hedspi.expensemanagement.databinding.ActivityDetailedBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetailedActivity : AppCompatActivity() {
    private lateinit var transaction : Transaction
    private lateinit var binding : ActivityDetailedBinding
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val labelInput = binding.labelInput
        val amountInput = binding.amountInput
        val labelLayout = binding.labelLayout
        val amountLayout = binding.amountLayout
        val descriptionInput = binding.descriptionInput
        val updateTransactionBtn = binding.updateTransactionBtn
        val dateInput = binding.dateInput
        val rootView = binding.rootView

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        transaction = intent.getSerializableExtra("transaction") as Transaction

        if (transaction != null) {
            labelInput.setText(transaction.label)
            amountInput.setText(DecimalFormat("0.##########").format(transaction.amount))
            descriptionInput.setText(transaction.description)
            dateInput.setText(transaction.transactionDate.toString())
        }

        rootView.setOnClickListener {
            this.window.decorView.clearFocus()

            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        labelInput.addTextChangedListener {
            updateTransactionBtn.visibility = View.VISIBLE
            if (it!!.isNotEmpty())
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            updateTransactionBtn.visibility = View.VISIBLE
            if (it!!.isNotEmpty())
                amountLayout.error = null
        }

        descriptionInput.addTextChangedListener {
            updateTransactionBtn.visibility = View.VISIBLE
        }

        dateInput.addTextChangedListener {
            updateTransactionBtn.visibility = View.VISIBLE
        }


        updateTransactionBtn.setOnClickListener {
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

            else if (date.isEmpty())
                amountLayout.error = "Please enter a valid date"

            else {
                val transaction = Transaction(transaction.id, label, amount, description, localDate)
                update(transaction)
            }

        }

        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)
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

    private fun update(transaction: Transaction) {
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
            db.transactionDao().update(transaction)
            finish()
        }
    }
}
