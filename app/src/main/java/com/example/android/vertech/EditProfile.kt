package com.example.android.vertech

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.android.vertech.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.bioUserDetails
import kotlinx.android.synthetic.main.activity_register.graduationUserdetails
import kotlinx.android.synthetic.main.activity_register.loading_view_register
import kotlinx.android.synthetic.main.activity_register.name_edittext_register
import kotlinx.android.synthetic.main.activity_register.selectphoto_button_register
import kotlinx.android.synthetic.main.activity_register.selectphoto_imageview_register
import java.util.*

class EditProfile : AppCompatActivity() {
    private var selectedPhotoUri: Uri? = null

    companion object {
        val TAG = RegisterActivity::class.java.simpleName!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout)
        supportActionBar?.elevation = 0.0f



        update_btn.setOnClickListener {
            performUpdate()
        }

        selectphoto_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
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
        val name = name_edittext_register.text.toString()
        val graduation=graduationUserdetails.text.toString()
        val bio=bioUserDetails.text.toString()

        if (name.isEmpty() || graduation.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            return
        }

        loading_view_register.visibility = View.VISIBLE
        uploadImageToFirebaseStorage()
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            // save user without photo
            saveUserToFirebaseDatabase(null)
        } else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
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
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = if (profileImageUrl == null) {
            User(uid, email_edittext_register.text.toString(), name_edittext_register.text.toString(),null,graduationUserdetails.text.toString()," ",bioUserDetails.text.toString())
        } else {
            User(uid,email_edittext_register.text.toString(), name_edittext_register.text.toString(), profileImageUrl,graduationUserdetails.text.toString()," ",bioUserDetails.text.toString())
        }

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
                loading_view_register.visibility = View.GONE
            }
    }

}