package com.github.gimme.gimmebot.core.command

/**
 * Represents an entity that can receive messages.
 */
fun interface MessageReceiver {

    /** Sends the specified [message] to this receiver. */
    fun sendMessage(message: String)
}
