package com.example.android.vertech.messages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.vertech.R
import com.example.android.vertech.adapters.NewMesaageAdapter
import com.example.android.vertech.adapters.NewUserMessageItemClickInterface
import com.example.android.vertech.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity(), NewUserMessageItemClickInterface {
    private var users: ArrayList<User> = ArrayList()
    private var matchedUsers: ArrayList<User> = ArrayList()
    private var usersAdapter: NewMesaageAdapter = NewMesaageAdapter(users, this)

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        swiperefresh_newMessage.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorAccent
            )
        )
        recyclerview_newmessage.layoutManager = LinearLayoutManager(this@NewMessageActivity)

        supportActionBar?.title = "Select User"
        val currentUser = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/favourites")
        val user_ref = FirebaseDatabase.getInstance().getReference("/users")

        swiperefresh_newMessage?.isRefreshing = true
        ref.child(currentUser).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value.toString() == "null") {
                    swiperefresh_newMessage.isRefreshing = false
                    Toast.makeText(
                        this@NewMessageActivity,
                        "Your starred contacts appears here",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dataSnapshot.children.forEach {
                    val userId = it.key.toString()
                    user_ref.orderByChild("name")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {}
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                dataSnapshot.children.forEach {
                                    it.getValue(User::class.java)?.let {
                                        if (it.uid == userId) {
                                            users.add(it)
                                        }
                                    }
                                }
                                initRecyclerView()
                                performSearch()
                                swiperefresh_newMessage.isRefreshing = false
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        swiperefresh_newMessage.setOnRefreshListener {
            fetchUsers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        TODO("Not yet implemented")
    }

    private fun initRecyclerView() {
        usersAdapter = NewMesaageAdapter(users, this).also {
            recyclerview_newmessage?.adapter = it
            recyclerview_newmessage?.adapter?.notifyDataSetChanged()
        }
    }

    private fun performSearch() {
        searchbar_new_message.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return false
            }
        })
    }

    private fun search(text: String?) {
        matchedUsers = ArrayList()
        text?.let {
            users.forEach { person ->
                if (person.name.toString().contains(text, true) ||
                    person.domain.toString().contains(text, true)
                ) {
                    matchedUsers.add(person)
                }
            }
            updateRecyclerView()
            if (matchedUsers.isEmpty()) {
                Toast.makeText(this, "Contact you are searching in unavailable", Toast.LENGTH_SHORT)
                    .show()
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        recyclerview_newmessage.apply {
            usersAdapter.users = matchedUsers
            usersAdapter.notifyDataSetChanged()
        }
    }


    override fun onNoteClick(user: User) {
        val intent = Intent(this, ChatLogActivity::class.java)
        intent.putExtra(USER_KEY, user)
        startActivity(intent)
    }

    private fun fetchUsers() {
        swiperefresh_newMessage.isRefreshing = true
        val currentUser = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/favourites")
        val user_ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.child(currentUser).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    user_ref.orderByChild("name")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {}
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                dataSnapshot.children.forEach {
                                    it.getValue(User::class.java)?.let {
                                    }
                                }
                            }
                        })
                }
                recyclerview_newmessage.layoutManager = LinearLayoutManager(this@NewMessageActivity)
                initRecyclerView()
                performSearch()
                recyclerview_newmessage.adapter = usersAdapter
                swiperefresh_newMessage.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}