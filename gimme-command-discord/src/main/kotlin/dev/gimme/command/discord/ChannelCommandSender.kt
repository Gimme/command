package dev.gimme.command.discord

import dev.gimme.command.sender.CommandSender
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User

/**
 * Represents a command sender in Discord channels that the bot is part of.
 *
 * This includes both text channels in servers and private messages.
 */
class ChannelCommandSender(private val channel: MessageChannel, private val user: User) : CommandSender {

    override val name: String
        get() = "[DISCORD] ${user.name}"

    override fun sendMessage(message: String) {
        channel.sendMessage("```$message```").queue()
    }
}
