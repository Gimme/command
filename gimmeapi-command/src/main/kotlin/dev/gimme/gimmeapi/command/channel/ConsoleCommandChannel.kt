package dev.gimme.gimmeapi.command.channel

import dev.gimme.gimmeapi.command.sender.ConsoleCommandSender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * Accepts input from the main console.
 */
open class ConsoleCommandChannel :
    TextCommandChannel(includeConsoleListener = false, commandPrefix = null) {

    private var job: Job? = null

    override fun onEnable() {
        val sc = Scanner(System.`in`)
        val sender = ConsoleCommandSender

        job = GlobalScope.launch {
            while (true) {
                val message = sc.nextLine()

                parseInput(sender, message)
            }
        }
    }

    override fun onDisable() {
        assert(job != null)
        job?.cancel()
        job = null
    }
}
