package com.example.android.vertech.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.vertech.R
import kotlinx.android.synthetic.main.activity_getting_started.*

class GettingStarted : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getting_started)
        getstartedcircle.setOnClickListener {
            startActivity(Intent(this@GettingStarted, MainActivity::class.java))
            finish()
        }
        getstartedTextview.setOnClickListener {
            startActivity(Intent(this@GettingStarted, MainActivity::class.java))
            finish()
        }
    }
}