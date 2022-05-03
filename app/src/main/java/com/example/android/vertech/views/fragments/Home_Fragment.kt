package com.example.android.vertech.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.vertech.R
import com.example.android.vertech.adapters.FeedsAdapter
import com.example.android.vertech.models.Feeds
import com.example.android.vertech.views.MainActivity
import com.example.android.vertech.views.todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home_.*
import kotlinx.android.synthetic.main.fragment_home_.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Home_Fragment : Fragment() {
    lateinit var database: DatabaseReference
    lateinit var userUid: String
    lateinit var adapterPost: FeedsAdapter
    lateinit var posts: ArrayList<Feeds>

    override fun onStart() {
        super.onStart()
        (activity as MainActivity?)?.toolbar_text?.text = "Home"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.actionBar?.hide()
        super.onCreate(savedInstanceState)
        userUid = FirebaseAuth.getInstance().uid.toString()
        database = FirebaseDatabase.getInstance().getReference("users")
        posts = ArrayList()
        adapterPost = FeedsAdapter(requireContext(), posts)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_, container, false)
        val ref = FirebaseDatabase.getInstance().getReference("/feeds")
        view?.swiperefresh_feeds?.isRefreshing = true
        GlobalScope.launch {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    posts.clear()
                    dataSnapshot.children.forEach {
                        it.getValue(Feeds::class.java)?.let {
                            posts.add(it)
                        }
                    }
                    view?.recyclerview_feeds?.adapter = adapterPost
                    adapterPost.notifyDataSetChanged()
                    swiperefresh_feeds?.isRefreshing = false
                }
            })
        }

        view.recyclerview_feeds.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.recyclerview_feeds.isNestedScrollingEnabled = false
        view.new_post.setOnClickListener {
            val intent = Intent(activity, com.example.android.vertech.views.new_post::class.java)
            startActivity(intent)
        }
        view.todo_project.setOnClickListener {
            val intent = Intent(activity, todo::class.java)
            startActivity(intent)
        }
        view?.swiperefresh_feeds?.setOnRefreshListener {
            fetchFeeds()
        }
        return view
    }

    private fun fetchFeeds() {
        view?.swiperefresh_feeds?.isRefreshing = true
        val ref = FirebaseDatabase.getInstance().getReference("/feeds")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    it.getValue(Feeds::class.java)?.let {}
                }
                view?.recyclerview_feeds?.adapter = adapterPost
                view?.swiperefresh_feeds?.isRefreshing = false
            }
        })
    }

}
