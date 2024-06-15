package com.zs.jollycat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.zs.jollycat.R
import com.zs.jollycat.database.DatabaseHelper

class ForgotPasswordFragment: DialogFragment() {

    private lateinit var dbHelPer : DatabaseHelper

    private lateinit var submitButton: Button
    private lateinit var usernameEditText : EditText
    private lateinit var phoneNumEditText: EditText


    private lateinit var passwordUser : TextView
    private lateinit var passwordUserTitle : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        dbHelPer = DatabaseHelper(requireContext())
        usernameEditText = view.findViewById(R.id.et_forgot_password_username)
        phoneNumEditText = view.findViewById(R.id.et_forgot_password_phoneNum)
        submitButton = view.findViewById(R.id.btn_forgot_password_submit)

        passwordUser = view.findViewById(R.id.tv_forgot_password)
        passwordUserTitle = view.findViewById(R.id.tv_forgot_password_subtitle)
        passwordUserTitle.visibility = View.GONE
        passwordUser.visibility = View.GONE

        submitButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val phone = phoneNumEditText.text.toString()

            if (username.isEmpty() || phone.isEmpty()){
                Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            } else {
                val user = dbHelPer.getUserFromDatabaseByUserPhone(username, phone)
                if (user != null){
                    passwordUserTitle.visibility = View.VISIBLE
                    passwordUser.visibility = View.VISIBLE
                    passwordUserTitle.text = "$username password: "
                    passwordUser.text = user.Password

                    Toast.makeText(context, "User found!", Toast.LENGTH_SHORT).show()
                } else  {
                    Toast.makeText(context, "User not found found!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val closeButton = view.findViewById<TextView>(R.id.btn_closeFragment)
        closeButton.setOnClickListener{
            dismiss()
        }
    return view
    }
}