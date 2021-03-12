package com.github.gimme.gimmebot.discord.command.mediums

import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Handles input/output through Discord channels.
 */
class ChannelCommandMedium(
    private val jda: JDA,
    commandPrefix: String?,
    includeConsoleListener: Boolean = true,
) : TextCommandMedium(includeConsoleListener, commandPrefix) {

    private val eventListener: EventListener = object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            val sender = ChannelCommandSender(event.channel, event.author)
            val message = event.message.contentRaw

            parseInput(sender, message)
        }
    }

    override fun onEnable() {
        jda.addEventListener(eventListener)
    }

    override fun onDisable() {
        jda.removeEventListener(eventListener)
    }
}
