package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Scanner

/**
 * Accepts input from the main console.
 */
class ConsoleCommandMedium(commandCollection: CommandCollection) : TextCommandMedium(commandCollection) {

    override val commandPrefix: String?
        get() = null

    override fun onInstall() {
        val sc = Scanner(System.`in`)
        val consoleSender: CommandSender = object : CommandSender {
            override val name: String
                get() = "#"

            override fun sendMessage(message: String) {}
        }

        GlobalScope.launch { while (true) parseInput(consoleSender, sc.nextLine()) }
    }
}
