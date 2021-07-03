package dev.gimme.command.mc

import dev.gimme.command.sender.CommandSender
import org.bukkit.command.CommandSender as SpigotCommandSender

/**
 * Returns this [SpigotCommandSender] as a new [CommandSender].
 */
internal val SpigotCommandSender.gimme: CommandSender
    get() = McCommandSender(this)

/**
 * Represents a Minecraft command sender.
 */
class McCommandSender(
    /**
     * The source [SpigotCommandSender].
     */
    val spigot: SpigotCommandSender
) : CommandSender {

    override val name = this.spigot.name

    override fun sendMessage(message: String) = this.spigot.sendMessage(message)
}
