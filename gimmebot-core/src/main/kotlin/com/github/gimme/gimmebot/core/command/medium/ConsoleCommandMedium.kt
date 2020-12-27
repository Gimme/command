package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.ConsoleCommandSender
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Scanner

/**
 * Accepts input from the main console.
 */
class ConsoleCommandMedium(commandManager: CommandManager<String?>) : TextCommandMedium(commandManager, false) {

    override val commandPrefix: String?
        get() = null

    override fun onInstall() {
        val sc = Scanner(System.`in`)
        val sender = ConsoleCommandSender

        GlobalScope.launch {
            while (true) {
                val message = sc.nextLine()

                parseInput(sender, message)
            }
        }
    }

}
