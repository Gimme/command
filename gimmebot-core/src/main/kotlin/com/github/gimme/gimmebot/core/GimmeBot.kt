package com.github.gimme.gimmebot.core

import com.github.gimme.gimmebot.core.command.CommandManager
import com.github.gimme.gimmebot.core.command.SimpleCommandManager
import com.github.gimme.gimmebot.core.data.config.BotConfig
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import com.github.gimme.gimmebot.core.data.DataManager
import com.github.gimme.gimmebot.core.data.requireResource
import mu.KotlinLogging
import java.io.File

/**
 * Represents a bot that can be started to perform tasks and respond to commands.
 */
abstract class GimmeBot : Bot {

    private val botResourcePath = "bot.yml"
    private val logger = KotlinLogging.logger {}

    private lateinit var botConfig: BotConfig

    /** If the bot is started. */
    var started = false

    /** The data manager. */
    lateinit var dataManager: DataManager

    /** The command manager */
    var commandManager: CommandManager = SimpleCommandManager()

    override fun start() {
        if (started) return

        botConfig = requireResource(loadYamlFromResource(botResourcePath, BotConfig::class.java), botResourcePath)
        val name = botConfig.name

        dataManager = DataManager(File(name))

        if (!connect()) {
            logger.error("Failed to connect $name")
            return
        }

        started = true
        onStart()

        logger.info("$name started!")
    }

    override fun stop() {
        if (!started) return
        started = false

        onStop()

        logger.info("${botConfig.name} stopped!")
    }

    /** Connects to any platform that the bot is using and returns if successful. */
    protected abstract fun connect(): Boolean

    /** Disconnects from any platform that the bot is connected to. */
    protected abstract fun disconnect()

    /** Performs startup logic. */
    protected abstract fun onStart()

    /** Performs shutdown logic. */
    protected open fun onStop() {}
}
