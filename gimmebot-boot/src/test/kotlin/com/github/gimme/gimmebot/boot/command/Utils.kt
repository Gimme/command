package com.github.gimme.gimmebot.boot.command

import com.github.gimme.gimmebot.core.command.sender.CommandSender

val DUMMY_COMMAND_SENDER = object : CommandSender {
    override val name = "dummy"

    override fun sendMessage(message: String) {}
}

const val DUMMY_RESPONSE = ""
