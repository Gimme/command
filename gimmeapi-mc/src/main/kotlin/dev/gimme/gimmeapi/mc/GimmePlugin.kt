package dev.gimme.gimmeapi.mc

import dev.gimme.gimmeapi.core.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.core.command.manager.CommandManager
import dev.gimme.gimmeapi.core.command.manager.TextCommandManager
import dev.gimme.gimmeapi.mc.command.SpigotCommandChannel
import org.bukkit.plugin.java.JavaPlugin

/**
 * A Spigot plugin with a simple setup.
 */
abstract class GimmePlugin : JavaPlugin() {

    /**
     * The default command manager where custom commands can be registered.
     */
    open val commandManager: CommandManager<String?> = TextCommandManager()

    /**
     * The command channel through which command input and output is handled.
     */
    lateinit var commandChannel: TextCommandChannel
        private set

    override fun onEnable() {
        commandChannel = SpigotCommandChannel(this, includeConsoleListener = false)
        commandChannel.registerCommandManager(commandManager)
        commandChannel.enable()
    }

    override fun onDisable() {
        commandChannel.disable()
    }
}
