package com.github.gimme.gimmebot.core.command

/**
 * Represents an entity that can receive messages.
 */
fun interface MessageReceiver {

    /** Sends this sender a [message]. */
    fun sendMessage(message: String)
}
