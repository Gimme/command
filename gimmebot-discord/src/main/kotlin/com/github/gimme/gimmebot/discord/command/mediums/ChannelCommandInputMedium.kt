package com.github.gimme.gimmebot.discord.command.mediums

import com.github.gimme.gimmebot.core.command.medium.BaseCommandInputMedium
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Accepts input from Discord channels.
 */
class ChannelCommandInputMedium(private val jda: JDA, private val config: DiscordConfig) : BaseCommandInputMedium() {

    override val commandPrefix: String
        get() {
            val prefix = config.prefix
            if (prefix.isEmpty()) throw IllegalStateException("No command prefix defined in the Discord config")
            return prefix
        }

    override fun onInstall() {
        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val channel = event.channel
                val user = event.author
                val message = event.message.contentRaw

                parseInput(ChannelCommandSender(channel, user), message)
            }
        })
    }
}
