package com.example.android.vertech

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.models.ChatMessage
import com.example.android.vertech.models.User
import com.example.android.vertech.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chats_.view.*

class Chats_Fragment : Fragment() {
    private val adapter = GroupAdapter<ViewHolder>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()

    companion object {
        var currentUser: User? = null
        val TAG = Chats_Fragment::class.java.simpleName
    }
    lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_chats_, container, false)
        verifyUserIsLoggedIn()

        view.myProfile_latest_messages.setOnClickListener(){
            val intent = Intent(activity, My_Profile::class.java)
            startActivity(intent)
        }
        val userUid= FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!).get().addOnSuccessListener {
            if(it.exists()){
                val requestOptions = RequestOptions()
                val picUrl=it.child("profileImageUrl").value
                Glide.with(view.myProfile_latest_messages.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(view.myProfile_latest_messages)
            }
            else{
                Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener(){
            Log.d(RegisterActivity.TAG, "failed")
        }

        view.recyclerview_latest_messages.adapter = adapter

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
        view.swiperefresh_message.setOnRefreshListener {
            verifyUserIsLoggedIn()
            fetchCurrentUser()
            listenForLatestMessages()
        }
        return view
    }
    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it, requireContext()))
        }
        view?.swiperefresh_message?.isRefreshing = false
    }

    private fun listenForLatestMessages() {
        view?.swiperefresh_message?.isRefreshing = true
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(Chats_Fragment.TAG, "database error: " + databaseError.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(Chats_Fragment.TAG, "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {
                    view?.swiperefresh_message?.isRefreshing = false
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
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}