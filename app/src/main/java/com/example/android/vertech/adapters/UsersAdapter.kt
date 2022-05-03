package com.example.android.vertech.adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.example.android.vertech.models.User
import com.example.android.vertech.views.BigImageDialog
import com.example.android.vertech.views.RegisterActivity
import com.example.android.vertech.views.fragments.Search_Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.user_row_new_message.view.domain
import kotlinx.android.synthetic.main.user_row_new_message.view.graduation_year
import kotlinx.android.synthetic.main.user_row_new_message.view.imageview_new_message
import kotlinx.android.synthetic.main.user_row_new_message.view.username_textview_new_message
import kotlinx.android.synthetic.main.users_search_layout.view.*

class UsersAdapter(var users: ArrayList<User>, val userItemClickInterface: Search_Fragment) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    lateinit var mContext: Context
    var testClick = false
    val favourites_database = FirebaseDatabase.getInstance().getReference("favourites")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.users_search_layout, parent, false)
        return UsersViewHolder(inflater, mContext)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(users[position])
        holder.itemView.setOnClickListener {
            userItemClickInterface.onNoteClick(users[position])
        }
        val currentUser = FirebaseAuth.getInstance().uid.toString()
        val userSaved = users[position].uid.toString()

        favourites_database.child(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild(userSaved)) {
                        holder.itemView.favourites.setImageResource(R.drawable.ic_baseline_star_24)
                    } else {
                        holder.itemView.favourites.setImageResource(R.drawable.ic_baseline_star_outline_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(RegisterActivity.TAG, "Failed to read value.", error.toException())
                }
            }
            )

        holder.itemView.favourites.setOnClickListener {
            testClick = true
            favourites_database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (testClick == true) {
                        if (snapshot.child(currentUser).hasChild(userSaved)) {
                            favourites_database.child(currentUser).child(userSaved).removeValue()
                            testClick = false
                        } else {
                            favourites_database.child(currentUser).child(userSaved)
                                .setValue(System.currentTimeMillis() / 1000)
                            testClick = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UsersViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {

        val username_textview_new_message = view.username_textview_new_message
        val domain = view.domain
        val grad_year = view.graduation_year
        val userImage = view.imageview_new_message

        fun bind(user: User) {
            username_textview_new_message.text = user.name
            domain.text = user.domain
            grad_year.text = user.graduation
            if (!user.profileImageUrl!!.isEmpty()) {
                val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)
                Glide.with(userImage.context)
                    .load(user.profileImageUrl)
                    .apply(requestOptions)
                    .into(userImage)

                userImage.setOnClickListener {
                    BigImageDialog.newInstance(user.profileImageUrl).show(
                        (context as Activity).fragmentManager, ""
                    )
                }
            }
        }
    }
}

interface UserItemClickInterface {
    fun onNoteClick(user: User)
    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
}
