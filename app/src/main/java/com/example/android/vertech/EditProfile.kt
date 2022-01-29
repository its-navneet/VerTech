package com.example.android.vertech

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.bioUserDetails
import kotlinx.android.synthetic.main.activity_register.loading_view_register
import kotlinx.android.synthetic.main.activity_register.name_edittext_register
import kotlinx.android.synthetic.main.activity_register.selectphoto_button_register
import kotlinx.android.synthetic.main.activity_register.selectphoto_imageview_register
import java.io.ByteArrayOutputStream
import java.util.*
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlin.collections.HashMap


class EditProfile : AppCompatActivity() {
    private var selectedPhotoUri: Uri? = null
    companion object {
        val TAG = EditProfile::class.java.simpleName
    }
    val user = User()
    var domain: String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        update_btn.setOnClickListener {
            performUpdate()
        }

        selectphoto_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        // access the items of the list
        val domains = resources.getStringArray(R.array.domains_res)
        // access the domain spinner
        val spinner_domain = findViewById<Spinner>(R.id.domain_spinner)
        if (spinner_domain != null) {
            val adapter_domain = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, domains
            )
            spinner_domain.adapter = adapter_domain

            spinner_domain.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    domain = domains[position].toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }

        }
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
                selectedPhotoUri = data.data ?: return
                Log.d(TAG, "Photo was selected")
                // Get and resize profile image
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                contentResolver.query(selectedPhotoUri!!, filePathColumn, null, null, null)?.use {
                    it.moveToFirst()
                    val columnIndex = it.getColumnIndex(filePathColumn[0])
                    val picturePath = it.getString(columnIndex)
                    // If picture chosen from camera rotate by 270 degrees else
                    Picasso.get().load(selectedPhotoUri).into(selectphoto_imageview_update)
                }
            }
        }

        private fun performUpdate() {
            val name = name_editProfile.text.toString()
            val bio = bio_editProfile.text.toString()
            if (name.isEmpty() || domain.isEmpty() || bio.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return
            }
            uploadImageToFirebaseStorage()
            loading_view_register.visibility = View.VISIBLE
        }

        private fun uploadImageToFirebaseStorage() {
            // compressing image
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
            val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

            if (selectedPhotoUri == null) {
                saveUserToFirebaseDatabase(user.profileImageUrl.toString())
            } else {
                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
                ref.putBytes(reducedImage)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                        @Suppress("NestedLambdaShadowedImplicitParameter")
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d(TAG, "File Location: $it")
                            saveUserToFirebaseDatabase(it.toString())
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Failed to upload image to storage: ${it.message}")
                        loading_view_register.visibility = View.GONE
                        already_have_account_text_view.visibility = View.VISIBLE
                    }
            }
        }

        private fun saveUserToFirebaseDatabase(profileImageUrl: String?) {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users")
            val userUpdates = mapOf<String,String>("name" to name_editProfile.text.toString(),
                "domain" to domain , "bio" to bio_editProfile.text.toString() ,
                "profileImageUrl" to profileImageUrl.toString())
            ref.child(uid.toString()).updateChildren(userUpdates).addOnSuccessListener {
                name_editProfile.text.clear()
                domain=""
                bio_editProfile.text.clear()
                Toast.makeText(this,"Updated Successfully!!",Toast.LENGTH_SHORT).show()
                loading_view_register.visibility = View.GONE

            }.addOnFailureListener{
                Toast.makeText(this,"Failed to update!!",Toast.LENGTH_SHORT).show()
                loading_view_register.visibility = View.GONE
            }
        }

}