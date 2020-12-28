package com.github.gimme.gimmebot.discord.command.mediums

import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Accepts input from Discord channels.
 */
class ChannelCommandMedium(
    private val jda: JDA,
    commandPrefix: String?,
    commandManager: CommandManager<String?>,
    includeConsoleListener: Boolean = true,
) : TextCommandMedium(commandManager, includeConsoleListener, commandPrefix) {

    override fun onInstall() {
        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val sender = ChannelCommandSender(event.channel, event.author)
                val message = event.message.contentRaw

                parseInput(sender, message)
            }
        })
    }
}
