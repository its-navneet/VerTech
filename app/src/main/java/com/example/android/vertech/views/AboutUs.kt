package com.example.android.vertech.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.vertech.R

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        supportActionBar?.hide()
    }
}