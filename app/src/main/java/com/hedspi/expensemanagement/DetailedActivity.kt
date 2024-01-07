package com.hedspi.expensemanagement

import android.content.Context
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hedspi.expensemanagement.databinding.ActivityDetailedBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        val rootView = binding.rootView

        transaction = intent.getSerializableExtra("transaction", Transaction::class.java)!!

        if (transaction != null) {
            labelInput.setText(transaction.label)
            amountInput.setText(transaction.amount.toString())
            descriptionInput.setText(transaction.description)
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


        updateTransactionBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descriptionInput.text.toString()

            if (label.isEmpty())
                labelLayout.error = "Please enter a valid label"

            else if (amount == null)
                amountLayout.error = "Please enter a valid amount"

            else {
                val transaction = Transaction(transaction.id, label, amount, description)
                update(transaction)
            }

        }

        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun update(transaction: Transaction) {
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().update(transaction)
            finish()
        }
    }
}