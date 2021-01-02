package com.github.gimme.gimmebot.discord.command.mediums

import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Handles input/output through Discord channels.
 */
class ChannelCommandMedium(
    private val jda: JDA,
    commandPrefix: String?,
    includeHelpCommand: Boolean = true,
    includeConsoleListener: Boolean = true,
) : TextCommandMedium(includeHelpCommand, includeConsoleListener, commandPrefix) {

    override fun onEnable() {
        super.onEnable()

        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val sender = ChannelCommandSender(event.channel, event.author)
                val message = event.message.contentRaw

                parseInput(sender, message)
            }
        })
    }
}
