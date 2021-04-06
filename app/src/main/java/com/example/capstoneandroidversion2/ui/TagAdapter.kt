package com.example.capstoneandroidversion2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage

class TagAdapter(
    val posts: List<NotificationMessage>,
    val onPostClick: (NotificationMessage) -> Unit
) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageBody = itemView.findViewById<TextView>(R.id.message_textview)
        val messageTimestamp = itemView.findViewById<TextView>(R.id.timestamp_textview)

        fun bind(
            notificationMessage: NotificationMessage,
            onPostClick: (NotificationMessage) -> Unit
        ) {
            messageBody.text = notificationMessage.body
            messageTimestamp.text = notificationMessage.timestamp
            itemView.setOnClickListener {
                onPostClick(notificationMessage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder =
        TagViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.message_layout, parent, false)
        )


    override fun onBindViewHolder(holder: TagViewHolder, position: Int) =
        holder.bind(posts[position], onPostClick)


    override fun getItemCount(): Int =
        posts.size
}
