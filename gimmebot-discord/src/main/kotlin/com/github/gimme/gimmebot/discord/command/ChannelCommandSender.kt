package com.github.gimme.gimmebot.discord.command

import com.github.gimme.gimmebot.core.command.CommandSender
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User

class ChannelCommandSender(private val channel: MessageChannel, private val user: User) : CommandSender {
    override val name: String
        get() = user.name

    override fun sendMessage(message: String) {
        channel.sendMessage("```$message```").queue()
    }
}
