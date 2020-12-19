package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.SimpleCommandManager
import com.github.gimme.gimmebot.core.command.medium.BaseCommandInputMedium
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class BaseCommandInputMediumTest {

    private val mockCommandManager = mock(SimpleCommandManager::class.java)

    @Test
    fun `should send command input`() {
        val commandInputMedium = object : BaseCommandInputMedium() {
            override val commandPrefix: String?
                get() = null

            override fun onInstall() {
                send(DUMMY_COMMAND_SENDER, "abc")
            }
        }
        commandInputMedium.install(mockCommandManager)

        verify(mockCommandManager, times(1)).parseInput(DUMMY_COMMAND_SENDER, "abc")
    }

    @Test
    fun `should send command input with prefix`() {
        val commandInputMedium = object : BaseCommandInputMedium() {
            override val commandPrefix: String
                get() = "!"

            override fun onInstall() {
                send(DUMMY_COMMAND_SENDER, "!abc def")
            }
        }
        commandInputMedium.install(mockCommandManager)

        verify(mockCommandManager, times(1)).parseInput(DUMMY_COMMAND_SENDER, "abc def")
    }
}
