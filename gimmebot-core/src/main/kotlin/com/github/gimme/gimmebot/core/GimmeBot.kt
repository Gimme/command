package com.github.gimme.gimmebot.core

import com.github.gimme.gimmebot.core.data.DataManager
import mu.KotlinLogging
import java.io.File

/**
 * Represents a bot that can be started to perform tasks and respond to commands.
 */
open class GimmeBot : Bot {

    private val botResourcePath = "bot.yml"
    private val logger = KotlinLogging.logger {}

    /** If the bot is started. */
    var started = false
        private set

    /** The data manager. */
    lateinit var dataManager: DataManager

    override fun start() {
        if (started) return
        started = true

        dataManager = DataManager(File("bot-name")) // TODO: unique name

        onStart()

        logger.info("Bot started!")
    }

    override fun stop() {
        if (!started) return
        started = false

        onStop()

        logger.info("Bot stopped!")
    }

    /** Performs startup logic. */
    protected open fun onStart() {}

    /** Performs shutdown logic. */
    protected open fun onStop() {}
}
