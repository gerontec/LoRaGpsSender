package com.gerontec.loragpssender

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContainer: LinearLayout = view.findViewById(R.id.messageContainer)
        val senderText: TextView = view.findViewById(R.id.senderText)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val context = holder.itemView.context

        // Set message content
        holder.senderText.text = message.senderId
        holder.messageText.text = message.message

        // Format timestamp
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        holder.timestampText.text = timeFormat.format(Date(message.timestamp))

        // Style differently for sent vs received messages
        val layoutParams = holder.messageContainer.layoutParams as LinearLayout.LayoutParams

        if (message.isSent) {
            // Sent messages: aligned right, blue background
            layoutParams.gravity = Gravity.END
            holder.messageContainer.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            )
            holder.senderText.text = "Du (${message.senderId})"
        } else {
            // Received messages: aligned left, gray background
            layoutParams.gravity = Gravity.START
            holder.messageContainer.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.darker_gray)
            )
        }

        holder.messageContainer.layoutParams = layoutParams
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }
}
