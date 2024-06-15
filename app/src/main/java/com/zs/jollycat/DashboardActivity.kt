package com.zs.jollycat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.zs.jollycat.fragment.AccountFragment
import com.zs.jollycat.fragment.CartFragment
import com.zs.jollycat.fragment.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    //private val userList = Database.userList

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //val userList = intent.getSerializableExtra("userList") as ArrayList<Users>?: ArrayList()
        val userID = intent.getStringExtra("userID")

        val bundle = Bundle()
        bundle.putString("userID", userID)

        val accountFragment = AccountFragment()
        accountFragment.arguments = bundle

        val cartFragment = CartFragment()
        cartFragment.arguments = bundle

        val homeFragment = HomeFragment()
        homeFragment.arguments = bundle

        replaceFragment(homeFragment)

        bottomNavigationView = findViewById(R.id.bottomNavigationView) as BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.cart -> {
                    replaceFragment(cartFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.home -> {
                    replaceFragment(homeFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.account -> {
                        replaceFragment(accountFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }



    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}