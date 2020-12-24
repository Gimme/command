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

    override fun parseInput(commandSender: CommandSender, commandName: String, arguments: List<String>): Boolean {
        // Return if not a valid command
        val command = getCommand(commandName.toLowerCase()) ?: return false

        val response = try { // Execute the command
            CommandResponse(command.execute(commandSender, arguments))
        } catch (e: CommandException) { // The command returned with an error
            e.response()
        }

        // Send back the response
        response.sendTo(commandSender)
        outputListeners.forEach { outputListener -> response.sendTo(outputListener) }

        return response.error == null
    }
}
