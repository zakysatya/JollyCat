package com.zs.jollycat

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.model.Users
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper : DatabaseHelper
    //private val userList = Database.userList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val btnToRegister = findViewById<Button>(R.id.btn_register)
        btnToRegister.setOnClickListener {
            val usernameEditText = findViewById<EditText>(R.id.etUsername)
            val passwordEditText = findViewById<EditText>(R.id.etPassword)
            val phoneNumEditText = findViewById<EditText>(R.id.etPhoneNum)

            val username = usernameEditText.text
            val password = passwordEditText.text
            val phoneNum = phoneNumEditText.text

            //valdiasi suername
            if (username.isEmpty() || password.isEmpty() || phoneNum.isEmpty()){
                Toast.makeText(this,"Username, Password, Phone Number must filled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (userExists(username.toString())) {
            //else if ( userList.any { it.Username == username.toString() }) {
                Toast.makeText(this, "Username already registered", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (username.length <8 || username.length > 99 ) {
                Toast.makeText(this,"Username’s length of at least 8 characters", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (!username.matches(Regex("^[a-zA-Z0-9]*$"))) {
                Toast.makeText(this, "Username must be alphanumeric", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //validasi Password
            if (password.length < 5 || password.length > 63) {
                Toast.makeText(this, "Password must be at least 5 characters", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password must contain at least 1 letter, 1 number, and 1 symbol", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Validasi phone number
            if (phoneNum.length !in 8..20) {
                Toast.makeText(this, "Phone number must be between 8-20 characters", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validasi phone num starts with either “0” or “+”
            else if (!phoneNum.startsWith("0") && !phoneNum.startsWith("+")) {
                Toast.makeText(this, "Phone number must start with either '0' or '+'", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // valdiasi phone number only consists of digits after the first character
            else if (!phoneNum.substring(1).matches(Regex("\\d+"))) {
                Toast.makeText(this, "Phone number can only contain digits after the first character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //generate a UUID for the user
            val userID = UUID.randomUUID().toString()

            val newUser = Users(userID, username.toString(), password.toString(), phoneNum.toString())

            //userList.add(newUser)
            addUserToDatabase(newUser)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Registration")
            builder.setMessage("Registration success!\n" +"\nYour Username: " + username + "\nYour Password: " + password)

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext, "Registration Success!", Toast.LENGTH_SHORT).show()
                //pindah activity ke home
                val intent = Intent(this, LoginActivity::class.java)
                //intent.putExtra("userList", userList)
                startActivity(intent)
                finish()
            }
            builder.show()
        }

        val btnTologin = findViewById<Button>(R.id.btn_toLogin)
        btnTologin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            //intent.putExtra("userList", userList)
            startActivity(intent)
            finish()
        }

    }

    private fun isPasswordValid(password: Editable): Boolean {
        if (password.length < 5) {
            return false
        }

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        return hasLetter && hasDigit && hasSymbol
    }

    private fun userExists(username : String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users WHERE Username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun addUserToDatabase(user: Users){
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val sql = "INSERT INTO Users (UserID, Username, PhoneNumber, Password) VALUES (?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, user.UserID)
        statement.bindString(2, user.Username)
        statement.bindString(3, user.PhoneNum)
        statement.bindString(4, user.Password)
        statement.executeInsert()
    }

}