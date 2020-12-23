package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
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

        // Execute the command
        val response = command.execute(commandSender, args)

        // Send back the response
        response?.let {
            response.sendTo(commandSender)
            outputListeners.forEach { outputListener -> response.sendTo(outputListener) }
        }

        return true
    }
}
