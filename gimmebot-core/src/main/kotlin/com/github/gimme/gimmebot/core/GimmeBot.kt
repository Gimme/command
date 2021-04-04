package com.github.gimme.gimmebot.core

import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.manager.SimpleCommandManager
import com.github.gimme.gimmebot.core.command.channel.ConsoleCommandChannel
import com.github.gimme.gimmebot.core.data.DataManager
import com.github.gimme.gimmebot.core.data.config.BotConfig
import com.github.gimme.gimmebot.core.data.requireResource
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import mu.KotlinLogging
import java.io.File

/**
 * Represents a bot that can be started to perform tasks and respond to commands.
 */
open class GimmeBot : Bot {

    private val botResourcePath = "bot.yml"
    private val logger = KotlinLogging.logger {}

    private lateinit var botConfig: BotConfig

    /** If the bot is started. */
    var started = false
        private set

    /** The data manager. */
    lateinit var dataManager: DataManager

    /** This bot's main command manager */
    val commandManager: CommandManager<Any?> = SimpleCommandManager { it }

    override fun start() {
        if (started) return
        started = true

        botConfig = requireResource(loadYamlFromResource(botResourcePath, BotConfig::class.java), botResourcePath)
        val name = botConfig.name

        dataManager = DataManager(File(name))

        // This starts a new thread and keeps the bot running
        ConsoleCommandChannel().apply {
            registerCommandManager(this@GimmeBot.commandManager)
            enable()
        }

        onStart()

        logger.info("$name started!")
    }

    override fun stop() {
        if (!started) return
        started = false

        onStop()

        logger.info("${botConfig.name} stopped!")
    }

    /** Performs startup logic. */
    protected open fun onStart() {}

    /** Performs shutdown logic. */
    protected open fun onStop() {}
}
