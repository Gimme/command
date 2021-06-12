package dev.gimme.gimmeapi.boot.command

import dev.gimme.gimmeapi.command.sender.CommandSender

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = "dummy"

    override fun sendMessage(message: String) {}
}

const val DUMMY_RESPONSE = ""
