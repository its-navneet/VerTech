package com.example.android.vertech

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.models.User
import com.example.android.vertech.views.BigImageDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_search_.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class Search_Fragment : Fragment() {

    companion object {
        const val USER_KEY = "USER_KEY"
        private val TAG = Search_Fragment::class.java.simpleName
    }
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_, container, false)
        fetchUsers()

        view.swiperefresh_search.setOnRefreshListener {
            fetchUsers()
        }

        view.searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                view.searchbar.clearFocus()
                processSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                processSearch(newText)
                return false
            }
        })

        view.myProfile_search.setOnClickListener() {
            val intent = Intent(activity, My_Profile::class.java)
            startActivity(intent)
        }
        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value
                Glide.with(view.myProfile_search.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(view.myProfile_search)
            } else {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener() {
            Log.d(Search_Fragment.TAG, "failed")
        }
        return view
    }
    private fun processSearch(s: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        val queryRequest_domain: Query = ref.orderByChild("domain").startAt(s).endAt(s+"\uf8ff")
        queryRequest_domain.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach {
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                            adapter.add(Search_Fragment.UserItem(it, context!!))
                        }
                    }
                }
                view!!.recyclerview_search?.adapter = adapter
            }
        })
        val queryRequest_name: Query = ref.orderByChild("name").startAt(s).endAt(s+"\uf8ff")
        queryRequest_name.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach {
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                            adapter.add(Search_Fragment.UserItem(it, context!!))
                        }
                    }
                }
                view!!.recyclerview_search.adapter = adapter
            }
        })
    }

    private fun fetchUsers() {
        view?.swiperefresh_search?.isRefreshing = true

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                dataSnapshot.children.forEach {
                    Log.d(Search_Fragment.TAG, it.toString())
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                            adapter.add(UserItem(it, context!!))
                        }
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as Search_Fragment.UserItem
                    val intent = Intent(view.context, UserProfile::class.java)
                    intent.putExtra(UserProfile.USER_KEY, userItem.user)
                    startActivity(intent)
                }

                view!!.recyclerview_search.adapter = adapter
                view!!.swiperefresh_search.isRefreshing = false
            }

        })
    }



    class UserItem(val user: User, val context: Context) : Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_new_message.text = user.name
            viewHolder.itemView.domain.text=user.domain
            viewHolder.itemView.graduation_year.text=user.graduation

            if (!user.profileImageUrl!!.isEmpty()) {
                val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)
                Glide.with(viewHolder.itemView.imageview_new_message.context)
                    .load(user.profileImageUrl)
                    .apply(requestOptions)
                    .into(viewHolder.itemView.imageview_new_message)

                viewHolder.itemView.imageview_new_message.setOnClickListener {
                    BigImageDialog.newInstance(user.profileImageUrl!!).show(
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