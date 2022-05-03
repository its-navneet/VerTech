package com.example.android.vertech.views

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.example.android.vertech.models.User
import com.example.android.vertech.utils.DateUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_likes_list.*
import kotlinx.android.synthetic.main.likes_row.view.*


class Likes_List : AppCompatActivity() {
    // Bundle Data
    private val postKey: String?
        get() = intent.getStringExtra("POST_KEY")

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes_list)
        fetchLikes()
        swiperefresh_likes.setOnRefreshListener {
            fetchLikes()
        }
    }


    private fun fetchLikes() {
        swiperefresh_likes?.isRefreshing = true
        val ref = FirebaseDatabase.getInstance().getReference("/likes")
        val user_ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.child(postKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach {
                    val key = it.key.toString()
                    val likedTime = it.value
                    user_ref.orderByChild("name")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {}
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                dataSnapshot.children.forEach {
                                    it.getValue(User::class.java)?.let {
                                        if (it.uid == key) {
                                            adapter.add(
                                                LikesItem(
                                                    it.name.toString(),
                                                    it.profileImageUrl.toString(),
                                                    DateUtils.getFormattedTime(likedTime as Long),
                                                    this@Likes_List
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        })
                }
                recyclerview_likes.adapter = adapter
                swiperefresh_likes.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class LikesItem(
        val liker: String,
        val profilePic: String,
        val timeStamp: String,
        val context: Context
    ) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.liker_name.text = liker
            viewHolder.itemView.liked_time.text = timeStamp
            val requestOptions = RequestOptions()
            Glide.with(viewHolder.itemView.imageview_Likes.context)
                .load(profilePic)
                .apply(requestOptions)
                .into(viewHolder.itemView.imageview_Likes)
        }

        override fun getLayout(): Int {
            return R.layout.likes_row
        }
    }
}