package com.example.android.vertech.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.android.vertech.R
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.models.ChatMessage
import com.example.android.vertech.models.User
import com.example.android.vertech.views.LatestMessageRow
import com.example.android.vertech.views.MainActivity
import com.example.android.vertech.views.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.type.DateTime
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chats_.*
import kotlinx.android.synthetic.main.fragment_chats_.view.*
import kotlinx.android.synthetic.main.fragment_home_.view.*


class Chats_Fragment : Fragment() {
    private val adapter = GroupAdapter<ViewHolder>()
    private val latestMessagesMap = LinkedHashMap<String, ChatMessage>()

    override fun onStart() {
        super.onStart()
        (activity as MainActivity?)?.toolbar_text?.text = "Chats"
    }

    companion object {
        var currentUser: User? = null
        val TAG = Chats_Fragment::class.java.simpleName
    }

    lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats_, container, false)
        verifyUserIsLoggedIn()
        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")

        view.recyclerview_latest_messages.adapter = adapter

        view?.swiperefresh_chats?.isRefreshing = true

        fetchCurrentUser()
        listenForLatestMessages()
        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(activity, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }


        view.new_message_fab.setOnClickListener {
            val intent = Intent(activity, NewMessageActivity::class.java)
            startActivity(intent)
        }
        view.swiperefresh_chats.setOnRefreshListener {
            view?.swiperefresh_feeds?.isRefreshing = true
            verifyUserIsLoggedIn()
            fetchCurrentUser()
            listenForLatestMessages()
        }
        return view
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it, this@Chats_Fragment))
            adapter.notifyDataSetChanged()
            recyclerview_latest_messages.smoothScrollToPosition(0)
        }
        view?.swiperefresh_chats?.isRefreshing = false
    }

    private fun listenForLatestMessages() {
        view?.swiperefresh_chats?.isRefreshing = true
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "database error: " + databaseError.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {
                    view?.swiperefresh_chats?.isRefreshing = false
                }
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    latestMessagesMap[dataSnapshot.key!!] = it
                    adapter.notifyDataSetChanged()
                    recyclerview_latest_messages.smoothScrollToPosition(0)
                    refreshRecyclerViewMessages()
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    latestMessagesMap[dataSnapshot.key!!] = it
                    latestMessagesMap.toSortedMap()
                    adapter.notifyDataSetChanged()
                    recyclerview_latest_messages.smoothScrollToPosition(0)
                    refreshRecyclerViewMessages()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun fetchCurrentUser() {
        view?.swiperefresh_chats?.isRefreshing = true
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentUser = dataSnapshot.getValue(User::class.java)
                view?.swiperefresh_chats?.isRefreshing = false
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}