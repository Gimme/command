package dev.gimme.command.mc

import dev.gimme.command.sender.CommandSender
import org.bukkit.command.CommandSender as SpigotCommandSender

/**
 * Returns an adapter object with this [SpigotCommandSender] as a [CommandSender].
 */
internal fun SpigotCommandSender.asGimmeCommandSender(): CommandSender = McCommandSender(this)

/**
 * TODO
 */
class McCommandSender(val spigotCommandSender: SpigotCommandSender) : CommandSender {

    override val name = spigotCommandSender.name

    override fun sendMessage(message: String) = spigotCommandSender.sendMessage(message)
}
