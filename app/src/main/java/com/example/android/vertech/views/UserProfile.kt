package com.example.android.vertech.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.models.User
import com.example.android.vertech.views.fragments.Search_Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_my_profile.biotext
import kotlinx.android.synthetic.main.activity_my_profile.email
import kotlinx.android.synthetic.main.activity_my_profile.profile_domain
import kotlinx.android.synthetic.main.activity_my_profile.profile_graduation
import kotlinx.android.synthetic.main.activity_my_profile.profile_username
import kotlinx.android.synthetic.main.activity_my_profile.profilepic
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class UserProfile : AppCompatActivity() {
    companion object {
        const val USER_KEY = "USER_KEY"
    }

    lateinit var database: DatabaseReference

    // Bundle Data
    private val userData: User?
        get() = intent.getParcelableExtra(USER_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val userUid = userData?.uid
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
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d(RegisterActivity.TAG, "failed")
        }

        back.setOnClickListener {
            startActivity(Intent(this@UserProfile, Search_Fragment::class.java))
            finish()
        }

        chat.setOnClickListener {
            val userItem = userData?.let { it1 -> UserItem(it1, this) }
            intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, userItem?.user)
            startActivity(intent)
            finish()
        }
        mail.setOnClickListener {
            val recipient = userData?.email
            val mIntent = Intent(Intent.ACTION_SEND)
            mIntent.data = Uri.parse("mailto:")
            mIntent.type = "message/rfc822"
            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            try {
                startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    class UserItem(val user: User, val context: Context) : Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_new_message.text = user.name
            viewHolder.itemView.domain.text = user.domain
            viewHolder.itemView.graduation_year.text = user.graduation

            if (!user.profileImageUrl!!.isEmpty()) {
                val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)
                Glide.with(viewHolder.itemView.imageview_new_message.context)
                    .load(user.profileImageUrl)
                    .apply(requestOptions)
                    .into(viewHolder.itemView.imageview_new_message)

                viewHolder.itemView.imageview_new_message.setOnClickListener {
                    BigImageDialog.newInstance(user.profileImageUrl).show(
                        (context as Activity).fragmentManager, ""
                    )
                }
            }
        }

        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }
    }
}