package com.hedspi.expensemanagement

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.hedspi.expensemanagement.databinding.ActivityStaticBinding


class StaticActivity : AppCompatActivity() {
    private lateinit var binding : ActivityStaticBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bottomNavigationView = binding.bottomNavView

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                com.hedspi.expensemanagement.R.id.item_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                com.hedspi.expensemanagement.R.id.item_static -> {
                    startActivity(Intent(this, StaticActivity::class.java))
                    true
                }
                com.hedspi.expensemanagement.R.id.item_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        val adapter = ViewPagerAdapter(supportFragmentManager)


        // Add your fragments to the adapter
        adapter.addFragment(DayFragment(), "Theo ngày")
        adapter.addFragment(WeekFragment(), "Theo tuần")
        adapter.addFragment(MonthFragment(), "Theo tháng")

        binding.viewPager.adapter = adapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

//    private fun showFragment(fragment: Fragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(com.hedspi.expensemanagement.R.id.frameLayout, fragment)
//        transaction.commit()
//    }
}