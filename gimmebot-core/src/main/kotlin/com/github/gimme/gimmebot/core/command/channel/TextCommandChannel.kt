package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
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
        } catch (e: CommandException) { // The command returned with an error
            if (e.code == ErrorCode.NOT_A_COMMAND.code()) return false
            e.message
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

    @Throws(CommandException::class)
    private fun executeCommand(commandSender: CommandSender, commandInput: String): String? {
        val commandPath = findBestMatchCommand(commandInput.split(" "))
            ?: throw ErrorCode.NOT_A_COMMAND.createException()
        val commandLabel = commandPath.joinToString(" ")

        // Remove command name, leaving only the arguments
        val argsInput = commandInput.removePrefix(commandLabel)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = argsInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        return executeCommand(commandSender, commandPath, args)
    }

    private fun findBestMatchCommand(input: List<String>): List<String>? {
        var bestMatchCommandPath: List<String>? = null

        registeredCommandManagers.forEach {
            val foundCommandPath = it.commandManager.findCommand(input)

            foundCommandPath?.let {
                if (foundCommandPath.size > bestMatchCommandPath?.size ?: -1) {
                    bestMatchCommandPath = foundCommandPath
                }
            }
        }

        return bestMatchCommandPath
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

        registeredCommandManagers.forEach {
            suggestions.addAll(it.commandManager.getBranches(completedWords))
        }

        findBestMatchCommand(completedWords)?.let { commandPath ->
            registeredCommandManagers.forEach {
                it.commandManager.getCommand(commandPath)?.let { command ->
                    suggestions.addAll(command.autocomplete(completedWords.drop(commandPath.size), currentWord))
                }
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
}
