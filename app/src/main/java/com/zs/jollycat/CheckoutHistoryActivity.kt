package com.zs.jollycat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zs.jollycat.adapter.CheckoutListAdapter
import com.zs.jollycat.database.DatabaseHelper

class CheckoutHistoryActivity : AppCompatActivity() {

    private lateinit var checkoutListAdapter: CheckoutListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_history)

        val userID = intent.getStringExtra("userID") as String

        dbHelper = DatabaseHelper(this)
        val userCheckoutHistory = dbHelper.getCheckoutHistory(userID)

        recyclerView = findViewById<RecyclerView>(R.id.RRrecyclerView)
        checkoutListAdapter = CheckoutListAdapter(userCheckoutHistory, dbHelper)
        recyclerView.adapter = checkoutListAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
