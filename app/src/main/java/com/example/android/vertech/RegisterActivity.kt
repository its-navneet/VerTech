package com.example.android.vertech

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.example.android.vertech.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {

    private var selectedPhotoUri: Uri? = null
    var domain: String =""

    companion object {
        val TAG = RegisterActivity::class.java.simpleName!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout)
        supportActionBar?.elevation = 0.0f

        // access the items of the list
        val domains= resources.getStringArray(R.array.domains_res)

        // access the spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, domains
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    domain= domains[position].toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
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
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val picturePath = it.getString(columnIndex)
                // If picture chosen from camera rotate by 270 degrees else
                    Picasso.get().load(selectedPhotoUri).into(selectphoto_imageview_register)
            }
        }
    }

    private fun performRegistration() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val name = name_edittext_register.text.toString()
        val graduation=graduationUserdetails.text.toString()
        val bio=bioUserDetails.text.toString()

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
            User(uid, email_edittext_register.text.toString(), name_edittext_register.text.toString(), null,graduationUserdetails.text.toString(),domain,bioUserDetails.text.toString())
        } else {
            User(uid,email_edittext_register.text.toString(), name_edittext_register.text.toString(), profileImageUrl,graduationUserdetails.text.toString(),domain,bioUserDetails.text.toString())
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
            startActivity(Intent(this@RegisterActivity,GettingStarted::class.java))
            finish()
        } else {
            Toast.makeText(this,"Not logged In",500)
        }
    }
}

