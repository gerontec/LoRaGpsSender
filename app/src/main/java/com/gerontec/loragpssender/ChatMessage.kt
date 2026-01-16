package com.gerontec.loragpssender

data class ChatMessage(
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val isSent: Boolean  // true if sent by us, false if received
)
