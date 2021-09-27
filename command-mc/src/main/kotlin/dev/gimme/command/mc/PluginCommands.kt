package dev.gimme.command.mc

import dev.gimme.command.node.CommandNode
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import java.lang.reflect.Constructor
import java.lang.reflect.Field

/**
 * Registers the root of the [command] to this plugin and returns the generated [PluginCommand].
 */
internal fun Plugin.registerCommand(command: CommandNode): PluginCommand {
    val pluginCommand = this.createCommand(command)

    this.getCommandMap().register(this.name, pluginCommand)
    return pluginCommand
}

private fun Plugin.createCommand(command: CommandNode): PluginCommand {
    val rootCommand = command.root

    val pluginCommand = this.createCommand(rootCommand.name)

    pluginCommand.aliases = rootCommand.aliases.toList()
    pluginCommand.description = rootCommand.description

    return pluginCommand
}

private fun Plugin.createCommand(commandName: String): PluginCommand {
    val constructor: Constructor<PluginCommand> =
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
    constructor.isAccessible = true

    return constructor.newInstance(commandName, this)
}

private fun Plugin.getCommandMap(): CommandMap {
    val f: Field = SimplePluginManager::class.java.getDeclaredField("commandMap")
    f.isAccessible = true
    return f.get(this.server.pluginManager as SimplePluginManager) as CommandMap
}
