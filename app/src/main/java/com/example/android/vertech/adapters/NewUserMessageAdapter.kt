package com.example.android.vertech.adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.R
import com.example.android.vertech.messages.NewMessageActivity
import com.example.android.vertech.models.User
import com.example.android.vertech.views.BigImageDialog
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMesaageAdapter(
    var users: ArrayList<User>,
    val userItemClickInterface: NewMessageActivity
) : RecyclerView.Adapter<NewMesaageAdapter.NewUserMessageViewHolder>() {
    lateinit var mContext: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewMesaageAdapter.NewUserMessageViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_row_new_message, parent, false)
        return NewUserMessageViewHolder(inflater, mContext)
    }

    override fun onBindViewHolder(holder: NewUserMessageViewHolder, position: Int) {
        holder.bind(users[position])
        holder.itemView.setOnClickListener {
            userItemClickInterface.onNoteClick(users[position])
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class NewUserMessageViewHolder(view: View, val context: Context) :
        RecyclerView.ViewHolder(view) {
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

interface NewUserMessageItemClickInterface {
    fun onNoteClick(user: User)
    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
}
