package dev.gimme.gimmeapi.mc

import dev.gimme.gimmeapi.command.parameter.ParameterTypes
import dev.gimme.gimmeapi.command.sender.SenderTypes
import dev.gimme.gimmeapi.command.channel.TextCommandChannel
import dev.gimme.gimmeapi.command.manager.CommandManager
import dev.gimme.gimmeapi.mc.command.ChatCommandChannel
import dev.gimme.gimmeapi.mc.command.McCommandSender
import org.bukkit.NamespacedKey
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
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

    init {
        registerBaseSpigotTypes()
    }

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

    private fun registerBaseSpigotTypes() {
        ParameterTypes.register(values = { server.onlinePlayers.map { it.name }.toSet() }) { server.getPlayerExact(it) }
        ParameterTypes.register(values = { server.worlds.map { it.name }.toSet() }) { server.getWorld(it) }
        ParameterTypes.register(values = {
            server.advancementIterator().asSequence().map { it.key.toString() }.toSet()
        }) {
            val pair = it.split(":", limit = 2)
            @Suppress("DEPRECATION")
            if (pair.size != 2) null else server.getAdvancement(NamespacedKey(pair[0], pair[1]))
        }

        SenderTypes.registerAdapter { s: McCommandSender -> s.spigotCommandSender }
        SenderTypes.registerAdapter { s: McCommandSender -> s.spigotCommandSender as? Player }
        SenderTypes.registerAdapter { s: McCommandSender -> s.spigotCommandSender as? ConsoleCommandSender }
    }
}
