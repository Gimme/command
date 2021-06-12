package dev.gimme.gimmeapi.mc

import dev.gimme.gimmeapi.core.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.core.command.manager.CommandManager
import dev.gimme.gimmeapi.mc.command.SpigotCommandChannel
import org.bukkit.plugin.java.JavaPlugin

/**
 * A Spigot plugin with a simple setup.
 */
abstract class GimmePlugin : JavaPlugin() {

    /**
     * The default command manager where custom commands can be registered.
     */
    lateinit var commandManager: CommandManager<String?>
        private set

    /**
     * The command channel through which command input and output is handled.
     */
    lateinit var commandChannel: TextCommandChannel
        private set

    final override fun onEnable() {
        commandChannel = SpigotCommandChannel(this, includeConsoleListener = false)
        commandManager = commandChannel.commandManager
        commandChannel.enable()

        onStart()
    }

    final override fun onDisable() {
        commandChannel.disable()

        onStop()
    }

    /**
     * Performs plugin startup logic.
     */
    protected abstract fun onStart()

    /**
     * Performs plugin shutdown logic.
     */
    protected abstract fun onStop()
}
