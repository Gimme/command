package com.github.gimme.gimmebot.core.command

/**
 * Accepts input from console.
 */
object ConsoleCommandSender : CommandSender {

    override val name: String
        get() = "#"

    override fun sendMessage(message: String) = println(message)
}
