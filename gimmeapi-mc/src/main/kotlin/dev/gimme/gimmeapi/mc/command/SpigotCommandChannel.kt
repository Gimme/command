package dev.gimme.gimmeapi.mc.command

import dev.gimme.gimmeapi.core.command.Command
import dev.gimme.gimmeapi.core.command.channel.TextCommandChannel
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Handles input/output through Minecraft chat.
 */
class SpigotCommandChannel(
    private val plugin: JavaPlugin,
    includeConsoleListener: Boolean = true,
) : TextCommandChannel(
    includeConsoleListener = includeConsoleListener,
    commandPrefix = ""
), TabExecutor, Listener {

    private var pluginCommands = mutableMapOf<Command<*>, PluginCommand>()

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

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onChatListener(e: AsyncPlayerChatEvent) {
        val sender = e.player.asBotCommandSender()

        if (!parseInput(sender, e.message)) return

        e.isCancelled = true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf() // TODO
    }

    override fun onCommand(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val input = StringBuilder(label)
        args.forEach { input.append(" $it") }

        println("INPUT: $input") // TODO: remove

        return parseInput(sender.asBotCommandSender(), input.toString())
    }
}
