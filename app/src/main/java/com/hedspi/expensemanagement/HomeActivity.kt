package com.hedspi.expensemanagement

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.snackbar.Snackbar
import com.hedspi.expensemanagement.databinding.ActivityHomeBinding
import com.hedspi.expensemanagement.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var deletedTransaction : Transaction
    private lateinit var transactions: List<Transaction>
    private  lateinit var oldTransactions: List<Transaction>
    private lateinit var  transactionAdapter: TransactionAdapter
    private  lateinit var  linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        transactions = arrayListOf()
        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        val bottomNavigationView = binding.bottomNavView

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.item_static -> {
                    startActivity(Intent(this, StaticActivity::class.java))
                    true
                }
                R.id.item_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

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

        db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions")
            .addMigrations(migration_3_4)
            .build()

        val recyclerView = binding.recyclerview
        recyclerView.adapter = transactionAdapter
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

//        fetchAll()

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

        val addBtn = binding.addBtn

        addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAll() {
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }

    @SuppressLint("SetTextI18n")
    private fun updateDashboard(){
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmount = transactions.filter{ it.amount > 0 }.map{ it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        val balance = binding.balance
        val budget = binding.budget
        val expense = binding.expense

        balance.text = "$totalAmount vnd"
        budget.text = "$budgetAmount vnd"
        expense.text = "$expenseAmount vnd"
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun showSnackbar() {
        val view : View = binding.coordinator
        val snackbar : Snackbar = Snackbar.make(view, "Transaction deleted!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter {it.id != transaction.id}
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }
}