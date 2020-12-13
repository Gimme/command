package com.github.gimme.gimmebot.discord

import com.github.gimme.gimmebot.core.data.requireResource
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import com.github.gimme.gimmebot.core.plugin.GimmeBotPlugin
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.security.auth.login.LoginException

/**
 * Represents a plugin that connects to a Discord bot.
 */
class DiscordPlugin : GimmeBotPlugin() {

    private val discordResourcePath = "discord.yml"

    /** The Discord API interface. */
    lateinit var jda: JDA

    /** The prefix required to execute commands from chat. */
    lateinit var commandPrefix: String

    override fun onEnable() {
        val discordConfig =
            requireResource(loadYamlFromResource(discordResourcePath, DiscordConfig::class.java), discordResourcePath)

        val token = discordConfig.token
        commandPrefix = discordConfig.prefix

        try {
            jda = JDABuilder.createDefault(token).build()
        } catch (e: LoginException) {
            e.printStackTrace()
            return
        }

        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val message = event.message.contentRaw
                if (!message.startsWith(commandPrefix)) return
                val commandInput = message.removePrefix(commandPrefix)

                val sender = ChannelCommandSender(event.channel)

                bot.commandManager.parseInput(sender, commandInput)
            }
        })
    }

    override fun onDisable() {
        jda.shutdownNow()
    }
}
