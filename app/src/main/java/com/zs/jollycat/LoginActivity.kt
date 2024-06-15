package com.zs.jollycat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.fragment.ForgotPasswordFragment
import com.zs.jollycat.model.Users

class LoginActivity : AppCompatActivity(){

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    //private val userList = Database.userList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val rememberMe = findViewById<CheckBox>(R.id.cbRememberMe)
        val usernameEditText = findViewById<EditText>(R.id.etUsername)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)

        val btnForgotPassword = findViewById<Button>(R.id.btn_forgetPassword)
        btnForgotPassword.setOnClickListener {
            val forgotPasswordFragment = ForgotPasswordFragment()
            forgotPasswordFragment.show(supportFragmentManager, "forgotPassword")
        }

        // Load username and password if "Remember Me" was checked
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPassword = sharedPreferences.getString("password", null)
        val isRemembered = sharedPreferences.getBoolean("rememberMe", false)
        if (isRemembered && savedUsername != null && savedPassword != null) {
            usernameEditText.setText(savedUsername)
            passwordEditText.setText(savedPassword)
            rememberMe.isChecked = true
            login(savedUsername, savedPassword)
        }


        btnLogin.setOnClickListener{
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Username and Password must filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isUsernameExists(username)) {
                Toast.makeText(this, "Username not found", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isPasswordCorrect(username, password)) {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (rememberMe.isChecked) {
                saveLoginDetails(username, password)
            } else {
                clearLoginDetails()
            }
            login(username, password)

            //if (userList.isEmpty()) {
            //    Toast.makeText(this, "Username not found", Toast.LENGTH_LONG).show()
            //    return@setOnClickListener
            //} else {
            //    val user = userList.find { it.Username == username.toString() && it.Password == password.toString() }
            //    val userID  = user?.UserID
            //    if (user == null) {
            //        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show()
            //        return@setOnClickListener
            //    }
        }

        // pindah ke register activity
        val btnToRegister = findViewById<Button>(R.id.btn_toRegister)
        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(username: String, password: String) {
        val user = getUserFromDatabase(username)
        if (user != null) {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("userID", user.UserID)
            startActivity(intent)
            finish()
        }
    }

    private fun saveLoginDetails(username: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.putBoolean("rememberMe", true)
        editor.apply()
    }

    private fun clearLoginDetails() {
        val editor = sharedPreferences.edit()
        editor.remove("username")
        editor.remove("password")
        editor.remove("rememberMe")
        editor.apply()
    }

    private fun getUserFromDatabase(username: String): Users? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users WHERE Username = ?", arrayOf(username))

        if (cursor.moveToFirst()) {
            val userID = cursor.getString(cursor.getColumnIndexOrThrow("UserID"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("Username"))
            val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("PhoneNumber"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("Password"))
            cursor.close()
            return Users(userID, username, password, phoneNumber)
        }

        cursor.close()
        return null
    }

    private fun isUsernameExists(username: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM Users WHERE Username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun isPasswordCorrect(username: String, password: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM Users WHERE Username = ? AND Password = ?", arrayOf(username, password))
        val correct = cursor.count > 0
        cursor.close()
        return correct
    }
}