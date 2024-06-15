package com.zs.jollycat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.RequestQueue
import com.zs.jollycat.database.DatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper : DatabaseHelper
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animationView = findViewById<LottieAnimationView>(R.id.animationView)
        Handler(Looper.getMainLooper()).postDelayed({
            animationView.cancelAnimation() // Stop the animation
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }


}