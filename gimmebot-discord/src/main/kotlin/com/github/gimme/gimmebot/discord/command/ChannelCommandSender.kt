package com.github.gimme.gimmebot.discord.command

import com.github.gimme.gimmebot.core.command.CommandSender
import net.dv8tion.jda.api.entities.MessageChannel

class ChannelCommandSender(private val channel: MessageChannel) : CommandSender {

    override fun sendMessage(message: String) {
        channel.sendMessage("```$message```").queue()
    }
}
