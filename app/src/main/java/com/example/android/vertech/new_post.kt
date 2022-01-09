package com.example.android.vertech

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.RegisterActivity.Companion.TAG
import com.example.android.vertech.models.Feeds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_post.*
import kotlinx.android.synthetic.main.feed_content.*
import java.util.*

class new_post : AppCompatActivity() {
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        upload_btn.setOnClickListener {
            sendNewFeed()

        }


        selectphoto_imageview_feed.setOnClickListener {
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

                    Picasso.get().load(selectedPhotoUri).into(selectphoto_imageview_feed)
            }
        }
    }

    private fun sendNewFeed() {
        loading_view_post.visibility = View.VISIBLE
        val text_content = feed_text_content.text.toString()

        if (text_content.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            return
        }
        uploadImageToFirebaseStorage()
    }


    fun uploadImageToFirebaseStorage() {
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
                        Log.d(RegisterActivity.TAG, "File Location: $it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to upload image to storage: ${it.message}")
                    loading_view_post.visibility = View.GONE
                }
        }

    }

    private fun saveUserToFirebaseDatabase(feedsImageUrl: String?) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        lateinit var database : DatabaseReference
        val ref = FirebaseDatabase.getInstance().getReference("/feeds/$uid")
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(uid!!)?.get().addOnSuccessListener {
            if(it.exists()){
                val name=it.child("name").value
                val feed = if (feedsImageUrl == null) {
                    Feeds(uid,name.toString(),null, feed_text_content.text.toString(),System.currentTimeMillis() / 1000)
                } else {
                    Feeds(uid,name.toString(), feedsImageUrl,feed_text_content.text.toString(),System.currentTimeMillis() / 1000)
                }



                ref.setValue(feed)
                    .addOnSuccessListener {
                        Log.d(TAG, "Finally we saved the user to Firebase Database")

                        val intent = Intent(this, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Failed to set value to database: ${it.message}")
                        loading_view_post.visibility = View.GONE
                    }

            }
            else{
                Toast.makeText(this,"Failed",LENGTH_SHORT)
            }
        }.addOnFailureListener(){
            Log.d(TAG, "failed")
        }

    }
}