package com.example.android.vertech.views

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.*
import com.example.android.vertech.R
import com.example.android.vertech.db.UserEntity
import com.example.android.vertech.models.Feeds
import com.example.android.vertech.models.User
import com.example.android.vertech.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.feed_content.*
import kotlinx.android.synthetic.main.feed_content.view.*
import kotlinx.android.synthetic.main.fragment_home_.view.*
import kotlinx.android.synthetic.main.recyclerview_row.view.*

class FeedsAdapter(con: Context, private val posts : ArrayList<Feeds>): RecyclerView.Adapter<FeedsAdapter.MyViewHolder>() {
    var testClick =false
    val userId = FirebaseAuth.getInstance().uid.toString()
    val likers_database = FirebaseDatabase.getInstance().getReference("likes")
    private val mcon: Context = con

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.feed_content, parent, false)
        return MyViewHolder(inflater)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPost = posts[position]
        val postKey = currentPost.senderid.toString()
        likers_database.child(postKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {
                        val likeCount = dataSnapshot.childrenCount
                        holder.itemView.feeds_like_btn.setImageResource(R.drawable.like_on)
                        holder.itemView.feeds_likes_count.text = "$likeCount likes"
                    } else {
                        val likeCount = dataSnapshot.childrenCount
                        holder.itemView.feeds_likes_count.text = "$likeCount likes"
                        holder.itemView.feeds_like_btn.setImageResource(R.drawable.like_off)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(RegisterActivity.TAG, "Failed to read value.", error.toException())
                }
            }
            )
        holder.itemView.feeds_like_btn.setOnClickListener(){
            testClick=true
            likers_database.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (testClick==true){
                        if (snapshot.child(postKey).hasChild(userId)){
                            likers_database.child(postKey).child(userId).removeValue()
                            testClick=false
                        }
                        else{
                            likers_database.child(postKey).child(userId).setValue(System.currentTimeMillis() / 1000)
                            testClick=false

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
        holder.itemView.feeds_likes_count.setOnClickListener(){
            val intent = Intent(mcon,Likes_List::class.java)
            intent.putExtra("POST_KEY",postKey)
            mcon.startActivity(intent)
        }
        if (!currentPost.imagecontent!!.isEmpty()) {
            val requestOptions = RequestOptions().placeholder(R.drawable.uploadimage).fitCenter().centerCrop()
            Glide.with(holder.itemView.feeds_image.context)
                .load(currentPost.imagecontent)
                .apply(requestOptions)
                .into(holder.itemView.feeds_image)
            holder.itemView.feeds_timestamp.text = currentPost.timestamp?.let {
                DateUtils.getFormattedTime(
                    it
                )
            }
            holder.itemView.feeds_username.text=currentPost.sendername
            holder.itemView.feeds_text.text=currentPost.textcontent
            holder.itemView.feeds_timestamp.text=DateUtils.getFormattedTime(currentPost.timestamp!!)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        init {
        }

    }
}