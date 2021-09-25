package dev.gimme.command.discord

import dev.gimme.command.sender.CommandSender
import dev.gimme.command.sender.SenderTypes
import net.dv8tion.jda.api.entities.User

/**
 * Represents a Discord command sender.
 */
internal class DiscordCommandSender(
    /**
     * The source Discord [user].
     */
    val user: User
) : CommandSender {

    override val name: String = this.user.name

    override fun sendMessage(message: String) {
        user.openPrivateChannel().flatMap { privateChannel -> privateChannel.sendMessage(message) }.submit()
    }

    companion object {
        init {
            SenderTypes.registerAdapter { s: DiscordCommandSender -> s.user }
        }
    }
}

/**
 * Returns this [User] as a new [DiscordCommandSender].
 */
val User.gimme: CommandSender
    get() = DiscordCommandSender(this)
