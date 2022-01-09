package com.example.android.vertech

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_getting_started.*

class GettingStarted : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getting_started)
        getstartedcircle.setOnClickListener(){
            startActivity(Intent(this@GettingStarted,Home::class.java))
            finish()
        }
        getstartedTextview.setOnClickListener(){
            startActivity(Intent(this@GettingStarted,Home::class.java))
            finish()
        }
    }
}