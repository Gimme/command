package com.github.gimme.gimmebot.core.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Accepts input from the main console.
 */
class ConsoleCommandInputMedium : BaseCommandInputMedium() {
    override val commandPrefix: String?
        get() = null

    override fun onInstall() {
        val sc = Scanner(System.`in`)
        val consoleSender: CommandSender = object : CommandSender {
            override fun sendMessage(message: String) {}
        }

        GlobalScope.launch { while (true) send(consoleSender, sc.nextLine()) }
    }
}
