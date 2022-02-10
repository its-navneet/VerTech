package com.example.android.vertech

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.models.Feeds
import com.example.android.vertech.views.FeedsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.feed_content.*
import kotlinx.android.synthetic.main.feed_content.view.*
import kotlinx.android.synthetic.main.fragment_home_.*
import kotlinx.android.synthetic.main.fragment_home_.view.*
import kotlin.collections.ArrayList


class Home_Fragment : Fragment() {
    lateinit var database: DatabaseReference
    lateinit var userUid: String
    lateinit var adapterPost: FeedsAdapter
    lateinit var posts:ArrayList<Feeds>

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        getActivity()?.actionBar?.hide()
        super.onCreate(savedInstanceState)
        userUid = FirebaseAuth.getInstance().uid.toString()
        database = FirebaseDatabase.getInstance().getReference("users")
        posts= ArrayList()
        adapterPost = FeedsAdapter(requireContext(), posts)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_, container, false)
        val ref = FirebaseDatabase.getInstance().getReference("/feeds")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    it.getValue(Feeds::class.java)?.let {
                        posts.add(it)
                    }
                }
                view?.recyclerview_feeds?.adapter = adapterPost
                view?.swiperefresh_feeds?.isRefreshing = false
            }
        })

        database.child(userUid).get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value

                Glide.with(myProfile_home.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(view.myProfile_home)
            } else {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener() {
            Log.d(RegisterActivity.TAG, "failed")
        }

        view.recyclerview_feeds.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)

        view.new_post.setOnClickListener() {
            val intent = Intent(activity, com.example.android.vertech.new_post::class.java)
            startActivity(intent)
        }
        view.todo_project.setOnClickListener() {
            val intent = Intent(activity, todo::class.java)
            startActivity(intent)
        }
        view?.swiperefresh_feeds?.setOnRefreshListener {
            fetchFeeds()
        }
        view.myProfile_home.setOnClickListener() {
            val intent = Intent(activity, My_Profile::class.java)
            startActivity(intent)
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
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach {
                    it.getValue(Feeds::class.java)?.let {}
                    }
                    view?.recyclerview_feeds?.adapter = adapterPost
                view?.swiperefresh_feeds?.isRefreshing = false
            }
        })
    }
}
