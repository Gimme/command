package dev.gimme.command.mc

import dev.gimme.command.sender.CommandSender
import dev.gimme.command.sender.SenderTypes
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
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

    override fun hasPermission(permission: String): Boolean = spigot.hasPermission(permission)

    companion object {
        init {
            SenderTypes.registerAdapter { s: McCommandSender -> s.spigot }
            SenderTypes.registerAdapter { s: McCommandSender -> s.spigot as? Player }
            SenderTypes.registerAdapter { s: McCommandSender -> s.spigot as? ConsoleCommandSender }
        }
    }
}
