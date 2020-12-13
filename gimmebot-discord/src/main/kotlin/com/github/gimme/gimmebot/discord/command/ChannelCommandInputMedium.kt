package com.github.gimme.gimmebot.discord.command

import com.github.gimme.gimmebot.core.command.BaseCommandInputMedium
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Accepts input from Discord channels.
 */
class ChannelCommandInputMedium(private val jda: JDA, commandPrefix: String) : BaseCommandInputMedium(commandPrefix) {

    override fun onInstall() {
        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val channel = event.channel
                val message = event.message.contentRaw

                send(ChannelCommandSender(channel), message)
            }
        })
    }
}
