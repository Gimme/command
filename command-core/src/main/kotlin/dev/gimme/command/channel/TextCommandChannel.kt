package dev.gimme.command.channel

import dev.gimme.command.Command
import dev.gimme.command.exception.CommandException
import dev.gimme.command.exception.ErrorCode
import dev.gimme.command.exception.IncompleteCommandException
import dev.gimme.command.manager.CommandManager
import dev.gimme.command.manager.TextCommandManager
import dev.gimme.command.node.CommandNode
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.sender.CommandSender
import java.util.*

/**
 * Represents a text-based command channel with, for example, a chat box or a command line.
 *
 * @property commandPrefix prefix required for the input to be recognized as a command
 */
abstract class TextCommandChannel(
    includeConsoleListener: Boolean = true,
    open var commandPrefix: String? = null,
) : BaseCommandChannel<String?>(TextCommandManager(), includeConsoleListener) {

    override fun parseInput(sender: CommandSender, input: String): Boolean {
        val commandInput = validatePrefix(input) ?: return false

        super.parseInput(sender, input)

        try {
            // Execute the command
            val message = executeCommand(sender, commandInput)

            // Send back the response
            respond(sender, message)
        } catch (e: CommandException) { // The command returned with an error
            val commandErrorEvent = (e as? IncompleteCommandException)?.let {
                val event = CommandErrorEvent(e, sender)
                onIncompleteCommand(event)
                event
            } ?: CommandErrorEvent(e, sender)

            onCommandError(commandErrorEvent)

            if (!commandErrorEvent.accept) return false
            respond(commandErrorEvent.commandSender, commandErrorEvent.responseMessage)
        }

        return true
    }

    override fun respond(commandSender: CommandSender, response: String?) {
        if (response.isNullOrEmpty()) return

        super.respond(commandSender, response)

        commandSender.sendMessage(response)
    }

    @Throws(CommandException::class, IncompleteCommandException::class)
    private fun executeCommand(commandSender: CommandSender, commandInput: String): String? {
        val commandSearchResult = findCommand(commandInput.split(" "))

        val commandPath = commandSearchResult.path
            ?: throw ErrorCode.NOT_A_COMMAND.createException()

        if (commandSearchResult.command == null) {
            throw IncompleteCommandException(
                usedPath = commandSearchResult.path,
                subBranches = commandSearchResult.subBranches,
                leafCommands = commandSearchResult.commandNode?.leafCommands ?: setOf(),
            )
        }

        val commandLabel = commandPath.joinToString(" ")

        // Remove command name, leaving only the arguments
        val argsInput = commandInput.removePrefix(commandLabel)

        val args = parseArgsInput(argsInput, commandSearchResult.command)

        // TODO: Convert string input to mapped args

        return executeCommand(commandSender, commandPath, args)
    }

    @Throws(CommandException::class)
    private fun parseArgsInput(argsInput: String, command: Command<*>): Map<CommandParameter, Any?> {
        val tokens = if (argsInput.isEmpty()) {
            emptyList()
        } else {
            // Split into words on spaces, ignoring spaces between two quotation marks
            argsInput
                .removePrefix(" ")
                .split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
                .map { s -> s.replace("\"", "") }
        }

        return parseArgsInput(tokens, command)
    }

    @Throws(CommandException::class)
    private fun parseArgsInput(argsInput: List<String>, command: Command<*>): Map<CommandParameter, Any?> {
        // TODO: get named args

        val parameterStack: Queue<CommandParameter> = ArrayDeque(command.parameters)
        val tokenStack: Queue<String> = ArrayDeque(argsInput)

        // TODO: default values in case of collections should accept empty "" or null for 0 and spaced "a b" for multiple values
        // TODO: handle multiple vararg parameters?

        val args = mutableMapOf<CommandParameter, MutableList<Any?>>()

        while (parameterStack.isNotEmpty()) {
            val parameter = parameterStack.peek()

            val stringArg: String? = tokenStack.peek() ?: run {
                if (!parameter.optional) throw ErrorCode.REQUIRED_PARAMETER.createException(parameter.id)
                null
            }

            val typedArg = try {
                stringArg?.let { parameter.type.parse(it) }
            } catch (e: CommandException) {
                parameterStack.remove()
                if (args[parameter] == null || parameterStack.isEmpty()) throw e
                continue
            }
            tokenStack.poll()

            args[parameter] = (args[parameter] ?: mutableListOf()).apply { add(typedArg) }

            if (parameter.form.isCollection && tokenStack.isNotEmpty()) continue
            parameterStack.remove()
        }

        if (tokenStack.isNotEmpty()) throw ErrorCode.TOO_MANY_ARGUMENTS.createException(tokenStack.toList())

        assert(parameterStack.isEmpty())
        assert(tokenStack.isEmpty())

        return args.entries.associate {
            it.key to if (it.value.contains(null)) null else when(it.key.form) {
                CommandParameter.Form.LIST -> it.value
                CommandParameter.Form.SET -> it.value.toSet()
                else -> it.value.first()
            }
        }
    }

    /**
     * Registers the given [commandManager] making the contained commands executable through this channel with the
     * results converted to strings.
     */
    fun <T> registerCommandManager(commandManager: CommandManager<T>) {
        super.registerCommandManager(commandManager) {
            if (it is Unit) null else it?.toString()
        }
    }

    /**
     * Returns all possible completions to the last word in the [input] that fits the expected input of a registered
     * command.
     */
    fun autocomplete(input: String): Set<String> {
        val words = input.split(" ")
        return autocomplete(words)
    }

    /**
     * Returns all possible completions to the last element in the [input] that fits the expected input of a registered
     * command.
     */
    fun autocomplete(input: List<String>): Set<String> {
        val completedWords = input.dropLast(1)
        val currentWord = input.last()

        val suggestions = mutableSetOf<String>()

        val searchResult = findCommand(completedWords)

        searchResult.path?.let { path ->
            suggestions.addAll(searchResult.subBranches)

            searchResult.command?.let { command ->
                suggestions.addAll(command.autocomplete(completedWords.drop(path.size), currentWord))
            }
        }

        return suggestions
            .filter { it.startsWith(currentWord) }
            .toSet()
    }

    /**
     * Returns all possible completions to the [currentWord] that fits the expected input of this command based on
     * already submitted [completedWords].
     */
    private fun Command<*>.autocomplete(completedWords: List<String>, currentWord: String): Set<String> {
        val namedArgs = mutableSetOf<String>()
        val flags = mutableSetOf<Char>()
        var orderedArgs = 0

        completedWords.forEach { word ->
            when {
                word.startsWith("--") -> {
                    namedArgs.add(word.removePrefix("--"))
                }
                word.startsWith("-") -> {
                    word.removePrefix("-").forEach { flags.add(it) }
                }
                else -> {
                    orderedArgs++
                }
            }
        }

        return this.getCompletionSuggestions(namedArgs, flags, orderedArgs, includeFlags = currentWord.startsWith("-"))
            .filter { it.startsWith(currentWord) }
            .toSet()
    }

    /**
     * If the given input starts with the [commandPrefix], returns a copy of the input with the prefix removed.
     * Otherwise, returns null.
     */
    private fun validatePrefix(input: String): String? {
        val prefix = commandPrefix

        if (prefix.isNullOrEmpty()) return input

        // Has to start with the command prefix
        if (!input.startsWith(prefix)) return null
        // Remove prefix
        return input.removePrefix(prefix)
    }

    /**
     * Handles errors on command execution.
     *
     * Modifying the [event] affects the response. For example, it can be useful to choose your own
     * [CommandErrorEvent.responseMessage].
     */
    protected open fun onCommandError(event: CommandErrorEvent<*>) {
        when (event.cause.code) {
            ErrorCode.NOT_A_COMMAND.code -> event.accept = false
        }
    }

    /**
     * Handles the [IncompleteCommandException] error specifically, with some extra data.
     *
     * This event will still pass through the regular [onCommandError].
     *
     * @see onCommandError
     */
    protected open fun onIncompleteCommand(event: CommandErrorEvent<IncompleteCommandException>) {
        event.accept = false
    }

    /**
     * Represents an event of a command error occurring after an attempted command execution.
     *
     * @property cause the [CommandException] that caused this event
     * @property commandSender the [CommandSender] that attempted to execute the command
     */
    protected data class CommandErrorEvent<out T>(
        val cause: T,
        val commandSender: CommandSender,
    ) where T : CommandException {

        /**
         * The message to be sent out as a response in this channel, or null if no message should be sent.
         *
         * The default response message is the [CommandErrorEvent.cause]'s [CommandException.message].
         */
        var responseMessage: String? = cause.message

        /**
         * If the command execution attempt should be accepted.
         *
         * If this is set to false, the [responseMessage] is not sent and the command input parser returns with a fail.
         */
        var accept: Boolean = true
    }
}
