package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
import com.github.gimme.gimmebot.core.command.exception.UncompletedCommandException
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.manager.TextCommandManager
import com.github.gimme.gimmebot.core.command.sender.CommandSender

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

        val message = try { // Execute the command
            executeCommand(sender, commandInput)
        } catch (e: UncompletedCommandException) { // The command path is not complete
            handleUncompletedCommand(e)
        } catch (e: CommandException) { // The command returned with an error
            if (e.code == ErrorCode.NOT_A_COMMAND.code()) return false
            handleCommandError(e)
        }

        // Send back the response
        respond(sender, message)
        return true
    }

    override fun respond(commandSender: CommandSender, response: String?) {
        if (response.isNullOrEmpty()) return

        super.respond(commandSender, response)

        commandSender.sendMessage(response)
    }

    @Throws(CommandException::class, UncompletedCommandException::class)
    private fun executeCommand(commandSender: CommandSender, commandInput: String): String? {
        val commandSearchResult = findCommand(commandInput.split(" "))

        val commandPath = commandSearchResult.path
            ?: throw ErrorCode.NOT_A_COMMAND.createException()

        if (commandSearchResult.command == null) {
            throw UncompletedCommandException(
                usedPath = commandSearchResult.path,
                subBranches = commandSearchResult.subBranches,
                leafCommands = getLeafCommands(commandPath),
            )
        }

        val commandLabel = commandPath.joinToString(" ")

        // Remove command name, leaving only the arguments
        val argsInput = commandInput.removePrefix(commandLabel)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = argsInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        return executeCommand(commandSender, commandPath, args)
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
        val completedWords = words.dropLast(1)
        val currentWord = words.last()

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

        return this.getCompletionSuggestions(namedArgs, flags, orderedArgs)
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
     * Returns a formatted message based on the thrown [uncompletedCommandException] to be sent out in this channel, or null if no
     * message should be sent.
     */
    protected open fun handleUncompletedCommand(uncompletedCommandException: UncompletedCommandException): String? = null

    /**
     * Returns a formatted error message based on the thrown [commandException] to be sent out in this channel, or null if no
     * message should be sent.
     */
    protected open fun handleCommandError(commandException: CommandException): String? = commandException.message
}
