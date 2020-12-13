package com.github.gimme.gimmebot.core.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ConsoleCommandInputMedium : BaseCommandInputMedium() {

    override fun onInstall() {
        val sc = Scanner(System.`in`)
        val consoleSender: CommandSender = object : CommandSender {
            override fun sendMessage(message: String) {}
        }

        GlobalScope.launch { while (true) send(consoleSender, sc.nextLine()) }
    }
}
