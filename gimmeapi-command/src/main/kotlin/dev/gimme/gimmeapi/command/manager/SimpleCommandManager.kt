package dev.gimme.gimmeapi.command.manager

import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.CommandSearchResult
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.manager.commandcollection.CommandCollection
import dev.gimme.gimmeapi.command.manager.commandcollection.CommandMap
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.sender.CommandSender


/**
 * Represents a command manager with base functionality.
 *
 * @param R the response type
 */
open class SimpleCommandManager<R>(private val defaultResponseParser: (Any?) -> R) : CommandManager<R> {

    private val registerCommandListeners: MutableList<CommandManager.RegisterCommandListener> = mutableListOf()
    private val executorByCommand: MutableMap<Command<*>, CommandNode<*, R>> = mutableMapOf()

    private val commandCollection: CommandCollection = CommandMap()

    override val commands: Iterable<Command<*>> = commandCollection

    final override fun <T> registerCommand(command: Command<T>, responseConverter: ((T) -> R)?) {
        commandCollection.addCommand(command)

        if (responseConverter != null) {
            executorByCommand[command] = CommandNode(command, responseConverter)
        }
        registerCommandListeners.forEach { it.onRegisterCommand(command) }
    }

    override fun getCommand(path: List<String>): Command<*>? = commandCollection.getCommand(path)

    override fun hasCommand(path: List<String>): Boolean = commandCollection.containsCommand(path)

    override fun findCommand(path: List<String>): CommandSearchResult = commandCollection.findCommand(path)

    override fun getBranches(path: List<String>): Set<String> = commandCollection.getBranches(path)

    override fun getLeafCommands(path: List<String>): Set<Command<*>> = commandCollection.getLeafCommands(path)

    override fun executeCommand(
        commandSender: CommandSender,
        command: Command<*>,
        args: Map<CommandParameter, Any?>,
    ): R {
        val commandNode = executorByCommand[command]

        return commandNode?.execute(commandSender, args) ?: defaultResponseParser(command.executeBy(commandSender, args))
    }

    override fun addRegisterCommandListener(registerCommandListener: CommandManager.RegisterCommandListener) {
        registerCommandListeners.add(registerCommandListener)
    }

    /**
     * Represents a wrapped [command] that can be executed with its response converted to a specific type, [R], through
     * the specified [responseParser]
     *
     * @param T the response type of the command
     * @param R the converted response type
     */
    private data class CommandNode<T, R>(
        /** The wrapped command. */
        val command: Command<T>,
        /** The response converter. */
        val responseParser: (T) -> R,
    ) {
        /**
         * Executes the wrapped [command] converting the response through the optional [responseParser] or else the
         * [defaultResponseParser].
         *
         * @throws CommandException if the command execution was unsuccessful
         */
        @Throws(CommandException::class)
        fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
            val response = command.executeBy(commandSender, args)
            return responseParser(response)
        }
    }
}
