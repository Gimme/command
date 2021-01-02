package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.ConsoleCommandSender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Scanner

/**
 * Accepts input from the main console.
 */
open class ConsoleCommandMedium : TextCommandMedium(true, false, null) {

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
