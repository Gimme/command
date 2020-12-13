package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.SimpleCommandManager
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BaseCommandInputMediumTest {

    private val mockCommandManager = mock(SimpleCommandManager::class.java)

    @Test
    fun `should send command input`() {
        val commandInputMedium = object : BaseCommandInputMedium(null) {
            override fun onInstall() {
                send(DUMMY_COMMAND_SENDER, "abc")
            }
        }
        commandInputMedium.install(mockCommandManager)

        verify(mockCommandManager, times(1)).parseInput(DUMMY_COMMAND_SENDER, "abc")
    }

    @Test
    fun `should send command input with prefix`() {
        val commandInputMedium = object : BaseCommandInputMedium("!") {
            override fun onInstall() {
                send(DUMMY_COMMAND_SENDER, "!abc def")
            }
        }
        commandInputMedium.install(mockCommandManager)

        verify(mockCommandManager, times(1)).parseInput(DUMMY_COMMAND_SENDER, "abc def")
    }
}
