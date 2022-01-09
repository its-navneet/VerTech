package com.example.android.vertech

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_email.*
import kotlinx.android.synthetic.main.fragment_email.view.*
import kotlinx.android.synthetic.main.fragment_name.view.*

class Email : Fragment() {
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!)?.get().addOnSuccessListener {
            if (it.exists()) {
                val username = it.child("name").value

                email.text="$username"

            } else {
            }
        }.addOnFailureListener() {
            Log.d(RegisterActivity.TAG, "failed")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_email, container, false)

        view.name_btn.setOnClickListener(){
            Navigation.findNavController(view).navigate(R.id.action_email3_to_name2)
        }
        return view
    }

}