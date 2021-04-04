package com.github.gimme.gimmebot.boot.command

import com.github.gimme.gimmebot.boot.command.commands.HelpCommand
import com.github.gimme.gimmebot.boot.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.channel.TextCommandChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelpCommandTest {

    @Test
    fun `should return list of commands`() {
        val commandChannel = object : TextCommandChannel() {
            override fun onEnable() {}
            override fun onDisable() {}
        }

        commandChannel.commandManager.registerCommand(DummyCommand("one"))
        commandChannel.commandManager.registerCommand(DummyCommand("two"))
        commandChannel.commandManager.registerCommand(DummyCommand("three"))

        val response = HelpCommand(commandChannel).execute(DUMMY_COMMAND_SENDER, listOf())

        assertEquals( 3, response.size)
        assertEquals("one", response[0].name)
        assertEquals("two", response[1].name)
        assertEquals("three", response[2].name)
    }

    private class DummyCommand(name: String) : SimpleCommand<Any>(name) {
        @CommandExecutor
        fun execute() {
        }
    }
}
