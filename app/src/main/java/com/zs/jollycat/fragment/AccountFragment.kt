package com.zs.jollycat.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.zs.jollycat.AboutActivity

import com.zs.jollycat.R
import com.zs.jollycat.CheckoutHistoryActivity
import com.zs.jollycat.LoginActivity
import com.zs.jollycat.database.DatabaseHelper

class AccountFragment : Fragment() {

    private lateinit var dbHelper : DatabaseHelper
    //private val userList = Database.userList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHelper = DatabaseHelper(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        dbHelper = DatabaseHelper(requireContext())
        val userID = arguments?.getString("userID")

        val user = dbHelper.getUserFromDatabase(userID)

        val tvUsername = view?.findViewById<TextView>(R.id.tvUsername)
        tvUsername?.text = user?.Username ?: "User not found"

        val tvPhoneNum = view?.findViewById<TextView>(R.id.tvPhoneNum)
        tvPhoneNum?.text = user?.PhoneNum ?: "Phone Number not found"

        val historyButton = view.findViewById<Button>(R.id.btn_history)
        historyButton.setOnClickListener {
            val intent = Intent(activity, CheckoutHistoryActivity::class.java)
            intent.putExtra("userID", userID)
            startActivity(intent)

        }

        val buttonAboutUs = view.findViewById<TextView>(R.id.btn_aboutUs)
        buttonAboutUs.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("userID", userID)
            startActivity(intent)
        }

        val logoutButton = view.findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            clearRememberMe()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        return view
    }

    private fun clearRememberMe() {
        val sharedPreferences = activity?.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.clear()?.apply()
    }

}