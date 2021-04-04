package com.github.gimme.gimmebot.core

/**
 * Represents a bot that can be started to perform tasks.
 */
interface Bot {
    /** Starts this bot. */
    fun start()

    /** Stops this bot. */
    fun stop()
}
