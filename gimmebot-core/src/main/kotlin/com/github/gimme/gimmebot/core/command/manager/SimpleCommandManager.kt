package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandResponse
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.HelpCommand
import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree


/**
 * Represents a command manager with base functionality.
 */
class SimpleCommandManager : CommandManager {

    private val commandCollection: CommandCollection = CommandTree()
    private val outputListeners: MutableList<MessageReceiver> = mutableListOf()

    init {
        addOutputListener { message -> println(message) }
        registerCommand(HelpCommand(commandCollection))
    }

    override fun registerCommand(command: Command<*>) {
        commandCollection.addCommand(command)
    }

    override fun getCommand(name: String): Command<*>? {
        return commandCollection.getCommand(name)
    }

    override fun getCommandCollection(): CommandCollection {
        return commandCollection
    }

    override fun addOutputListener(messageReceiver: MessageReceiver) {
        outputListeners.add(messageReceiver)
    }

    override fun parseInput(commandSender: CommandSender, input: String): Boolean {
        var lowerCaseInput = input.toLowerCase()

        outputListeners.forEach { it.sendMessage("${commandSender.name}: $input") }

        // Return if not a valid command
        val command = getCommand(lowerCaseInput) ?: return false
        // Remove command name, leaving only the arguments
        lowerCaseInput = lowerCaseInput.removePrefix(command.name)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = lowerCaseInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        var body: Any? = null
        var error: CommandException? = null

        try {
            // Execute the command
            body = command.execute(commandSender, args)
        } catch (e: CommandException) {
            // The command returned with an error
            error = e
        }

        val commandResponse = CommandResponse(body, error)

        // Send back the response
        commandResponse.sendTo(commandSender)
        outputListeners.forEach { outputListener -> commandResponse.sendTo(outputListener) }

        return error == null
    }
}
