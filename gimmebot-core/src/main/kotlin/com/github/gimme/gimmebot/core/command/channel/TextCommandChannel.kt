package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.manager.TextCommandManager
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
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
        var bestMatchCommandPath: List<String>? = null

        registeredCommandManagers.forEach {
            val foundCommandPath = it.commandManager.commandCollection.findCommand(commandInput.split(" "))

            foundCommandPath?.let {
                if (foundCommandPath.size > bestMatchCommandPath?.size ?: -1) {
                    bestMatchCommandPath = foundCommandPath
                }
            }
        }

        val commandPath = bestMatchCommandPath ?: throw ErrorCode.NOT_A_COMMAND.createException()
        val commandLabel = commandPath.joinToString(" ")

        // Remove command name, leaving only the arguments
        val argsInput = commandInput.removePrefix(commandLabel)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = argsInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        return executeCommand(commandSender, commandPath, args)
    }

    /**
     * Returns the best matching command label from the [commandInput], or null if no match.
     */
    private fun CommandCollection.findCommand(commandInput: String): String? {
        val words = commandInput.split(" ")

        var s = commandInput

        for (word in words.reversed()) {
            if (this.containsCommand(s)) return s

            s = s.removeSuffix(" $word")
        }

        return null
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
