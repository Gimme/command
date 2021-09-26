package dev.gimme.command.channel

import dev.gimme.command.Command
import dev.gimme.command.CommandSearchResult
import dev.gimme.command.exception.CommandException
import dev.gimme.command.exception.ErrorCode
import dev.gimme.command.manager.CommandManager
import dev.gimme.command.node.CommandNode
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.sender.CommandSender
import dev.gimme.command.sender.ConsoleCommandSender
import dev.gimme.command.sender.MessageReceiver

/**
 * Represents a command input/output channel with base functionality.
 *
 * @param R the output response type
 */
abstract class BaseCommandChannel<R>(
    final override val commandManager: CommandManager<R>,
    includeConsoleListener: Boolean = false,
) : CommandChannel<R> {

    private val registeredCommandManagers: MutableList<CommandManagerRegistration<*, R>> = mutableListOf()
    private val ioListeners: MutableList<MessageReceiver> = mutableListOf()

    override val commandManagers: List<CommandManager<*>>
        get() = registeredCommandManagers.map { it.commandManager }

    override var enabled = false

    init {
        if (includeConsoleListener) {
            addIOListener(ConsoleCommandSender)
        }

        registerCommandManager(commandManager) { it }
    }

    override fun parseInput(sender: CommandSender, input: String): Boolean {
        ioListeners.forEach { it.sendMessage("${sender.name}: $input") }

        return false
    }

    override fun respond(commandSender: CommandSender, response: R) {
        ioListeners.forEach { it.sendMessage(response.toString()) }
    }

    /**
     * Executes a command at the [commandPath] through one of the [registeredCommandManagers].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    protected fun executeCommand(
        commandSender: CommandSender,
        commandPath: List<String>,
        args: Map<CommandParameter, Any?>
    ): R {
        registeredCommandManagers.forEach {
            val command: Command<*> = it.commandManager.getCommand(commandPath) ?: return@forEach

            return it.executeCommand(commandSender, command, args)
        }

        throw ErrorCode.NOT_A_COMMAND.createException()
    }

    final override fun <T> registerCommandManager(commandManager: CommandManager<T>, responseWrapper: (T) -> R) {
        registeredCommandManagers.add(CommandManagerRegistration(commandManager, responseWrapper))
        commandManager.commands.forEach { onRegisterCommand(it) }
        commandManager.addRegisterCommandListener(this)
    }

    final override fun addIOListener(messageReceiver: MessageReceiver) {
        ioListeners.add(messageReceiver)
    }

    override fun onRegisterCommand(command: Command<*>) {}

    /**
     * Searches for the command or node with the longest matching sub-set from the start of the [path] and returns the
     * best result from all [registeredCommandManagers].
     *
     * If multiple commands/nodes are found at the same path, only the one with highest priority is returned.
     *
     * The result's [CommandSearchResult.subBranches] always includes all sub-branches found for the
     * [CommandSearchResult.path].
     */
    protected fun findCommand(path: List<String>): CommandSearchResult {
        var longestPath: List<String>? = null
        var command: Command<*>? = null
        var commandNode: CommandNode? = null
        val subBranches: MutableSet<String> = mutableSetOf()

        registeredCommandManagers.forEach {
            val searchResult = it.commandManager.findCommand(path)

            searchResult.path?.let { commandResultPath ->
                val bestResultPathSize = longestPath?.size ?: -1

                if (commandResultPath.size > bestResultPathSize) {
                    longestPath = searchResult.path
                    command = searchResult.command
                    commandNode = searchResult.commandNode
                    subBranches.addAll(searchResult.subBranches)
                } else if (commandResultPath.size == bestResultPathSize) {
                    if (command == null) command = searchResult.command
                    if (commandNode == null) commandNode = searchResult.commandNode
                    subBranches.addAll(searchResult.subBranches)
                }
            }
        }

        return CommandSearchResult(
            path = longestPath,
            command = command,
            commandNode = commandNode,
            subBranches = subBranches,
        )
    }

    /**
     * Represents a registered [commandManager] that can be used to execute commands with its responses converted to a
     * specific type, [R], through the specified [responseWrapper]
     *
     * @param T the response type of the command manager
     * @param R the converted response output type
     */
    protected data class CommandManagerRegistration<T, R>(
        /** The wrapped command manager. */
        val commandManager: CommandManager<T>,
        /** The response wrapper. */
        val responseWrapper: (T) -> R,
    ) {
        /**
         * Executes the [command] through this registered [commandManager] converting the response through the
         * [responseWrapper].
         *
         * @throws CommandException if the command execution was unsuccessful
         */
        @Throws(CommandException::class)
        fun executeCommand(commandSender: CommandSender, command: Command<*>, args: Map<CommandParameter, Any?>): R {
            val response = commandManager.executeCommand(commandSender, command, args)
            return responseWrapper(response)
        }
    }
}
