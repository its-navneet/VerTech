package com.example.android.vertech.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.vertech.R
import com.example.android.vertech.adapters.UserItemClickInterface
import com.example.android.vertech.adapters.UsersAdapter
import com.example.android.vertech.models.User
import com.example.android.vertech.views.MainActivity
import com.example.android.vertech.views.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home_.view.*
import kotlinx.android.synthetic.main.fragment_search_.*
import kotlinx.android.synthetic.main.fragment_search_.view.*

class Search_Fragment : Fragment(), UserItemClickInterface {
    private var users: ArrayList<User> = ArrayList()
    private var matchedUsers: ArrayList<User> = ArrayList()
    private var usersAdapter: UsersAdapter = UsersAdapter(users, this)

    companion object {
        private val TAG = Search_Fragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity?)?.toolbar_text?.text = "Search"
    }

    lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_, container, false)

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        view?.swiperefresh_search?.isRefreshing = true
        ref.orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    Log.d(TAG, it.toString())
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                            users.add(it)
                        }
                    }
                }
                initRecyclerView()
                performSearch()
                swiperefresh_search?.isRefreshing = false
            }
        })


        view.swiperefresh_search.setOnRefreshListener {
            fetchUsers()
        }
        view.recyclerview_search.layoutManager = LinearLayoutManager(context)
        return view
    }

    private fun initRecyclerView() {
        usersAdapter = UsersAdapter(users, this).also {
            recyclerview_search?.adapter = it
            view?.recyclerview_search?.adapter?.notifyDataSetChanged()
        }
    }

    private fun performSearch() {
        view?.searchbar?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                Toast.makeText(
                    context,
                    "Contact you are searching in unavailable",
                    Toast.LENGTH_SHORT
                ).show()
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        recyclerview_search.apply {
            usersAdapter.users = matchedUsers
            usersAdapter.notifyDataSetChanged()
        }
    }


    private fun fetchUsers() {
        view?.swiperefresh_search?.isRefreshing = true
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    Log.d(TAG, it.toString())
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                        }
                    }
                }
                view!!.recyclerview_search.adapter = usersAdapter
                view?.swiperefresh_feeds?.isRefreshing = false
                view!!.swiperefresh_search.isRefreshing = false
            }
        })
    }

    override fun onNoteClick(user: User) {
        val intent = Intent(view?.context, UserProfile::class.java)
        intent.putExtra(UserProfile.USER_KEY, user)
        startActivity(intent)
    }
}