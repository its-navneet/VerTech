package com.example.android.vertech.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.vertech.R
import com.example.android.vertech.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private var selectedPhotoUri: Uri? = null
    var domain: String = ""
    var graduationUserdetails: String = ""

    companion object {
        val TAG = RegisterActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        // access the items of the list
        val domains = resources.getStringArray(R.array.domains_res)
        val graduation = resources.getStringArray(R.array.gyear_res)

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

            // access the spinner
            val spinner_gyear = findViewById<Spinner>(R.id.gyear_spinner)
            if (spinner_gyear != null) {
                val adapter_gyear = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item, graduation
                )
                spinner_gyear.adapter = adapter_gyear

                spinner_gyear.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        graduationUserdetails = graduation[position].toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
            }


        }

        register_button_register.setOnClickListener {
            performRegistration()
        }

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
                Picasso.get().load(selectedPhotoUri).into(selectphoto_imageview_register)
            }
        }
    }

    private fun performRegistration() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val name = name_edittext_register.text.toString()
        val graduation = graduationUserdetails
        val bio = bioUserDetails.text.toString()

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || graduation.isEmpty() || bio.isEmpty() || domain.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            return
        }

        already_have_account_text_view.visibility = View.GONE
        loading_view_register.visibility = View.VISIBLE

        // Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                // else if successful
                Log.d(TAG, "Successfully created user with uid: ${it.result!!.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to create user: ${it.message}")
                loading_view_register.visibility = View.GONE
                already_have_account_text_view.visibility = View.VISIBLE
                Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        // compressing image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        if (selectedPhotoUri == null) {
            // save user without photo
            saveUserToFirebaseDatabase(null)
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
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = if (profileImageUrl == null) {
            User(
                uid,
                email_edittext_register.text.toString(),
                name_edittext_register.text.toString(),
                null,
                graduationUserdetails,
                domain,
                bioUserDetails.text.toString()
            )
        } else {
            User(
                uid,
                email_edittext_register.text.toString(),
                name_edittext_register.text.toString(),
                profileImageUrl,
                graduationUserdetails,
                domain,
                bioUserDetails.text.toString()
            )
        }

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")

                val intent = Intent(this, GettingStarted::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
                loading_view_register.visibility = View.GONE
                already_have_account_text_view.visibility = View.VISIBLE
            }
    }

    @SuppressLint("WrongConstant", "ShowToast")
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = Firebase.auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@RegisterActivity, GettingStarted::class.java))
            finish()
        } else {
            Toast.makeText(this, "Not logged In", 500)
        }
    }
}

