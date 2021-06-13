package dev.gimme.gimmeapi.mc

import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.command.manager.CommandManager
import dev.gimme.gimmeapi.mc.command.ChatCommandChannel
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
    lateinit var channel: TextCommandChannel
        private set

    final override fun onEnable() {
        channel = ChatCommandChannel(this, includeConsoleListener = false)
        channel.enable()
        commandManager = channel.commandManager

        onStart()
    }

    final override fun onDisable() {
        channel.disable()

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
