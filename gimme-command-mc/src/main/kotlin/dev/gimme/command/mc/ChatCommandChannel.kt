package dev.gimme.command.mc

import dev.gimme.command.Command
import dev.gimme.command.channel.TextCommandChannel
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Handles input/output through Minecraft chat.
 */
class ChatCommandChannel(
    private val plugin: JavaPlugin,
    includeConsoleListener: Boolean = true,
) : TextCommandChannel(
    includeConsoleListener = includeConsoleListener,
    commandPrefix = ""
), TabExecutor, Listener {

    private var pluginCommands = mutableMapOf<Command<*>, PluginCommand>()

    init {
        enable()
    }

    override fun onRegisterCommand(command: Command<*>) {
        super.onRegisterCommand(command)

        val registeredPluginCommand = plugin.registerCommand(command)
        pluginCommands[command] = registeredPluginCommand
        registeredPluginCommand.setExecutor(this)
    }

    override fun onEnable() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val input = mutableListOf(alias)
        input.addAll(args)
        return autocomplete(input).toMutableList()
    }

    override fun onCommand(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val input = StringBuilder(label)
        args.forEach { input.append(" $it") }

        return parseInput(sender.asGimmeCommandSender(), input.toString())
    }
}
