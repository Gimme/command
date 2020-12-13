package com.github.gimme.gimmebot.core

import com.github.gimme.gimmebot.core.plugin.GimmeBotPlugin

/**
 * Represents a bot that can be started to perform tasks.
 */
interface Bot {
    /** Starts this bot. */
    fun start()

    /** Stops this bot. */
    fun stop()

    /** Adds and enables the given [plugin]. */
    fun install(plugin: GimmeBotPlugin)
}
