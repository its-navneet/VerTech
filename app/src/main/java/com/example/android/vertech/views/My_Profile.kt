package com.example.android.vertech.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_my_profile.*

class My_Profile : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        fetchData()
        edit_profile.setOnClickListener {
            val intent = Intent(this@My_Profile, EditProfile::class.java)
            startActivity(intent)
            finish()
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this@My_Profile, MainActivity::class.java))
            finish()
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@My_Profile, RegisterActivity::class.java))
            finish()
        }
    }

    private fun fetchData() {
        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value
                Glide.with(profilepic.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(profilepic)

                val username = it.child("name").value
                val bio = it.child("bio").value
                val graduation = it.child("graduation").value
                val emailText = it.child("email").value
                val domain = it.child("domain").value

                profile_username.text = "$username"
                biotext.text = "$bio"
                email.text = "$emailText"
                profile_graduation.text = "$graduation"
                profile_domain.text = "$domain"

            } else {
                Toast.makeText(this, "Failed", LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d(RegisterActivity.TAG, "failed")
        }
    }
}