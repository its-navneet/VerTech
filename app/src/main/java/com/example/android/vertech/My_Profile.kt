package com.example.android.vertech

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.latest_message_row.*

class My_Profile : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var mGoogleSignInClient: GoogleSignInClient
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!)?.get().addOnSuccessListener {
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

                profile_username.text="$username"
                biotext.text="$bio"
                email.text="$emailText"
                profile_graduation.text="$graduation"
                profile_domain.text="$domain"

            } else {
                Toast.makeText(this, "Failed", LENGTH_SHORT)
            }
        }.addOnFailureListener() {
            Log.d(RegisterActivity.TAG, "failed")
        }

        profile_username.setOnClickListener(){
            startActivity(Intent(this@My_Profile,Details::class.java))
            finish()
        }

        edit_profile.setOnClickListener(){
            startActivity(Intent(this@My_Profile,EditProfile::class.java))
        }

        back.setOnClickListener(){
            startActivity(Intent(this@My_Profile,Home::class.java))
            finish()
        }

        logout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@My_Profile,RegisterActivity::class.java))
        }
    }
}