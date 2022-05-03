package com.example.android.vertech.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vertech.R
import com.example.android.vertech.db.UserEntity
import kotlinx.android.synthetic.main.recyclerview_row.view.*

class RoomDatabaseAdapter(val listener: RowClickListener) :
    RecyclerView.Adapter<RoomDatabaseAdapter.MyViewHolder>() {

    var items = ArrayList<UserEntity>()

    fun setListData(data: ArrayList<UserEntity>) {
        this.items = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row, parent, false)
        return MyViewHolder(inflater, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener.onItemClickListener(items[position])
        }
        holder.bind(items[position])

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class MyViewHolder(view: View, val listener: RowClickListener) : RecyclerView.ViewHolder(view) {

        val tvName = view.tvName
        val tvEmail = view.tvEmail
        val tvPhone = view.tvPhone
        val deleteUserID = view.deleteUserID
        val shareData = view.shareDetails

        fun bind(data: UserEntity) {
            tvName.text = data.name

            tvEmail.text = data.email


            tvPhone.text = data.phone

            deleteUserID.setOnClickListener {
                listener.onDeleteUserClickListener(data)
            }
            shareData.setOnClickListener {
                listener.onShareClickListener(data)
            }

        }
    }

    interface RowClickListener {
        fun onDeleteUserClickListener(user: UserEntity)
        fun onItemClickListener(user: UserEntity)
        fun onShareClickListener(user: UserEntity)
    }
}
