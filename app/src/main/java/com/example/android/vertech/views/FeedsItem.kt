package com.example.android.vertech.views

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.vertech.Home_Fragment
import com.example.android.vertech.R
import com.example.android.vertech.models.Feeds
import com.example.android.vertech.utils.DateUtils
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_content.view.*

class FeedsItem(val feeds: Feeds, val context: Home_Fragment) : Item<ViewHolder>() {


    override fun bind(viewHolder: ViewHolder, position: Int) {

        if (!feeds.imagecontent!!.isEmpty()) {
            val requestOptions = RequestOptions().placeholder(R.drawable.uploadimage).fitCenter().centerCrop()
            Glide.with(viewHolder.itemView.feeds_image.context)
                .load(feeds.imagecontent)
                .apply(requestOptions)
                .into(viewHolder.itemView.feeds_image)
            viewHolder.itemView.feeds_username.setText(feeds.sendername)
            viewHolder.itemView.feeds_timestamp.text = feeds.timestamp?.let {
                DateUtils.getFormattedTime(
                    it
                )
            }
            viewHolder.itemView.feeds_text.setText(feeds.textcontent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.feed_content
    }
}
