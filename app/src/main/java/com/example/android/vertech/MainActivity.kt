package com.example.android.vertech

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.ismaeldivita.chipnavigation.ChipNavigationBar


class MainActivity : AppCompatActivity() {

    var chipNavigationBar: ChipNavigationBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chipNavigationBar = findViewById(R.id.bottom_nav_bar)
        chipNavigationBar?.setItemSelected(
            R.id.home,
            true
        )
        bottomMenu()
    }
    private fun bottomMenu() {
        chipNavigationBar?.setOnItemSelectedListener(object :
            ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                when (i) {
                    R.id.home -> startActivity(Intent(this@MainActivity, Home::class.java))
                    R.id.search -> startActivity(Intent(this@MainActivity, Search::class.java))
                    R.id.chats -> startActivity(Intent(this@MainActivity,LatestMessagesActivity::class.java))
                }
            }
        })
    }
}
