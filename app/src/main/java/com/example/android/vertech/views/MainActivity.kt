package com.example.android.vertech.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val toolbar: Toolbar
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        menu_icon.setOnClickListener { v: View ->
            showMenu(v, R.menu.toolbar_menu)
        }
        supportActionBar?.customView = toolbar

        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value
                Glide.with(myProfile)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(myProfile)
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d("Failed", "failed")
        }

        val navController = this.findNavController(R.id.fragment)
        // Find reference to bottom navigation view
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_bar_main)
        // Hook your navigation controller to bottom navigation view
        navView.setupWithNavController(navController)

        myProfile.setOnClickListener {
            startActivity(Intent(this, My_Profile::class.java))
        }
    }

    //In the showMenu function from the previous example:
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        // Show the popup menu.
        popup.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.about -> {
                    startActivity(Intent(this, AboutUs::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        popup.show()
    }
}
