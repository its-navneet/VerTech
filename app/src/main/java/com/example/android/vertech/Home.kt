package com.example.android.vertech

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.RegisterActivity.Companion.TAG
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.LatestMessagesActivity
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.messages.UserItem
import com.example.android.vertech.models.ChatMessage
import com.example.android.vertech.models.Feeds
import com.example.android.vertech.models.User
import com.example.android.vertech.views.BigImageDialog
import com.example.android.vertech.views.FeedsItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.feed_content.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import kotlinx.android.synthetic.main.activity_home.new_post as new_post


class Home : AppCompatActivity() {
    lateinit var database: DatabaseReference
    private val adapter = GroupAdapter<ViewHolder>()
    private val latestFeedsMap = HashMap<String, Feeds>()

    companion object {
        const val USER_KEY = "USER_KEY"
    }


    var chipNavigationBar: ChipNavigationBar? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!)?.get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value
                Glide.with(myProfile_home.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(myProfile_home)
            } else {
                Toast.makeText(this, "Failed", 500)
            }
        }.addOnFailureListener() {
            Log.d(TAG, "failed")
        }

        new_post.setOnClickListener() {
            startActivity(Intent(this@Home, com.example.android.vertech.new_post::class.java))
        }

        recyclerview_feeds.adapter = adapter
        swiperefresh_feeds.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        fetchFeeds()
        swiperefresh_feeds.setOnRefreshListener {
            fetchFeeds()
        }


        myProfile_home.setOnClickListener() {
            startActivity(Intent(this@Home, My_Profile::class.java))
        }

        todo_project.setOnClickListener(){
            startActivity(Intent(this@Home,todo::class.java))
        }


        chipNavigationBar = findViewById(R.id.bottom_nav_bar)
        chipNavigationBar?.setItemSelected(
            R.id.home,
            true
        )
        bottomMenu()
    }

    private fun bottomMenu() {
        chipNavigationBar?.setOnItemSelectedListener(object :
            ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                when (i) {
                    R.id.home -> {
                        startActivity(Intent(this@Home, Home::class.java))
                        finish()
                    }
                    R.id.search -> {
                        startActivity(Intent(this@Home, Search::class.java))
                        finish()
                    }
                    R.id.chats -> {
                        startActivity(Intent(this@Home, LatestMessagesActivity::class.java))
                        finish()
                    }
                }
            }
        })
    }

    private fun fetchFeeds() {
        swiperefresh_feeds.isRefreshing=true

        val ref = FirebaseDatabase.getInstance().getReference("/feeds")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                dataSnapshot.children.forEach {
                    it.getValue(Feeds::class.java)?.let {
                        if (it.senderid != FirebaseAuth.getInstance().uid) {
                            adapter.add(FeedsItem(it, this@Home))
                        }
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val feedItem = item as FeedsItem
                    val intent = Intent(view.context, Home::class.java)
                    startActivity(intent)
                    finish()
                }

                recyclerview_feeds.adapter = adapter
                swiperefresh_feeds.isRefreshing = false
            }

        })
    }
}

