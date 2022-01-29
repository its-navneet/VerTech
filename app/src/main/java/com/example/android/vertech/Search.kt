package com.example.android.vertech

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.messages.ChatLogActivity
import com.example.android.vertech.messages.LatestMessagesActivity
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.models.User
import com.example.android.vertech.views.BigImageDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class Search : AppCompatActivity() {
    companion object {
        const val USER_KEY = "USER_KEY"
        private val TAG = Search::class.java.simpleName
    }
    lateinit var searchView: SearchView
    var mMediaPlayer: MediaPlayer? = null
    lateinit var database: DatabaseReference
    var chipNavigationBar: ChipNavigationBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        swiperefresh_search.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))

        fetchUsers()
        //Todo - Add more users and messages for screenshots

        swiperefresh_search.setOnRefreshListener {
            fetchUsers()
        }

        searchView=findViewById(R.id.searchbar)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                processSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                processSearch(newText)
                return false
            }
        })

        myProfile_search.setOnClickListener() {
            startActivity(Intent(this@Search, My_Profile::class.java))
        }
        val userUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userUid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val requestOptions = RequestOptions()
                val picUrl = it.child("profileImageUrl").value
                Glide.with(myProfile_search.context)
                    .load(picUrl)
                    .apply(requestOptions)
                    .into(myProfile_search)
            } else {
                Toast.makeText(this, "Failed", LENGTH_SHORT)
            }
        }.addOnFailureListener() {
            Log.d(TAG, "failed")
        }

        chipNavigationBar = findViewById(R.id.bottom_nav_bar)
        chipNavigationBar?.setItemSelected(
            R.id.search,
            true
        )
        bottomMenu()
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
                            adapter.add(UserItem(it, this@Search))
                        }
                    }
                }
                recyclerview_search.adapter = adapter
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
                            adapter.add(UserItem(it, this@Search))
                        }
                    }
                }
                recyclerview_search.adapter = adapter
            }
        })
    }

    // 1. Plays the water sound
    fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.refresh)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    // 2. Pause playback
    fun pauseSound() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) mMediaPlayer!!.pause()
    }

    private fun fetchUsers() {
        playSound()
        pauseSound()
        swiperefresh_search.isRefreshing = true

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach {
                    Log.d(TAG, it.toString())
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    it.getValue(User::class.java)?.let {
                        if (it.uid != FirebaseAuth.getInstance().uid) {
                            adapter.add(UserItem(it, this@Search))
                        }
                    }
                }


                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, UserProfile::class.java)
                    intent.putExtra(UserProfile.USER_KEY, userItem.user)
                    startActivity(intent)
                }

                recyclerview_search.adapter = adapter
                swiperefresh_search.isRefreshing = false
            }

        })
    }

    private fun bottomMenu() {
        chipNavigationBar?.setOnItemSelectedListener(object :
            ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                when (i) {
                    R.id.home -> {
                        startActivity(Intent(this@Search, Home::class.java))
                        finish()
                    }
                    R.id.search -> {
                        startActivity(Intent(this@Search, Search::class.java))
                        finish()
                    }
                    R.id.chats -> {
                        startActivity(Intent(this@Search, LatestMessagesActivity::class.java))
                        finish()
                    }
                }
            }
        })
    }

    class UserItem(val user: User, val context: Context) : Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_new_message.text = user.name
            viewHolder.itemView.domain.text= user.domain
            viewHolder.itemView.graduation_year.text=user.graduation
            if (!user.profileImageUrl!!.isEmpty()) {
                val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)
                Glide.with(viewHolder.itemView.imageview_new_message.context)
                    .load(user.profileImageUrl)
                    .apply(requestOptions)
                    .into(viewHolder.itemView.imageview_new_message)

                viewHolder.itemView.imageview_new_message.setOnClickListener {
                    BigImageDialog.newInstance(user?.profileImageUrl!!).show(
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