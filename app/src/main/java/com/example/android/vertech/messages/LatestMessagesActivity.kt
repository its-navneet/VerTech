package com.example.android.vertech.messages

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.*
import com.example.android.vertech.R
import com.example.android.vertech.messages.NewMessageActivity.Companion.USER_KEY
import com.example.android.vertech.models.ChatMessage
import com.example.android.vertech.models.User
import com.example.android.vertech.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_latest_messages.swiperefresh_message
import kotlinx.android.synthetic.main.activity_search.*


class LatestMessagesActivity : AppCompatActivity() {
    var chipNavigationBar: ChipNavigationBar? = null
    private val adapter = GroupAdapter<ViewHolder>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()


    companion object {
        var currentUser: User? = null
        val TAG = LatestMessagesActivity::class.java.simpleName
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var database : DatabaseReference
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        verifyUserIsLoggedIn()

        myProfile_latest_messages.setOnClickListener(){
            startActivity(Intent(this@LatestMessagesActivity,My_Profile::class.java))
        }
        val userUid= FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!)?.orderByChild("timestamp").get().addOnSuccessListener {
            if(it.exists()){
                val requestOptions = RequestOptions()
                val picUrl=it.child("profileImageUrl").value
                Glide.with(myProfile_latest_messages.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(myProfile_latest_messages)
            }
            else{
                Toast.makeText(this,"Failed",500)
            }
        }.addOnFailureListener(){
            Log.d(RegisterActivity.TAG, "failed")
        }

        recyclerview_latest_messages.adapter = adapter

        swiperefresh_message.setColorSchemeColors(ContextCompat.getColor(this, R.color.ripple_material_light))

        fetchCurrentUser()
        listenForLatestMessages()

        chipNavigationBar = findViewById(R.id.bottom_nav_bar)
        chipNavigationBar?.setItemSelected(
            R.id.chats,
            true
        )
        bottomMenu()

        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }


        new_message_fab.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }

        swiperefresh_message.setOnRefreshListener {
            verifyUserIsLoggedIn()
            fetchCurrentUser()
            listenForLatestMessages()
        }
    }

    private fun bottomMenu() {
        chipNavigationBar?.setOnItemSelectedListener(object :
            ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                when (i) {
                    R.id.home -> {
                        startActivity(Intent(this@LatestMessagesActivity, Home::class.java))
                        finish()
                    }
                    R.id.search -> {
                        startActivity(Intent(this@LatestMessagesActivity, Search::class.java))
                        finish()
                    }
                    R.id.chats -> {
                        startActivity(Intent(this@LatestMessagesActivity,LatestMessagesActivity::class.java))
                        finish()
                    }
                }
            }
        })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it, this))
        }
        swiperefresh_message.isRefreshing = false
    }

    private fun listenForLatestMessages() {
        swiperefresh_message.isRefreshing = true
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "database error: " + databaseError.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {
                    swiperefresh_message.isRefreshing = false
                }
            }

        })


        ref.orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    latestMessagesMap[dataSnapshot.key!!] = it
                    refreshRecyclerViewMessages()
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    latestMessagesMap[dataSnapshot.key!!] = it
                    refreshRecyclerViewMessages()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentUser = dataSnapshot.getValue(User::class.java)
            }

        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
