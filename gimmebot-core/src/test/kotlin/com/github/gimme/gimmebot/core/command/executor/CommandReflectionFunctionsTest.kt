package com.github.gimme.gimmebot.core.command.executor

import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.DUMMY_COMMAND_SENDER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommandReflectionFunctionsTest {

    @Test
    fun `should get first command executor function`() {
        val commandExecutorFunction = object : BaseCommand<String>("") {
            fun c1(): String {
                return ""
            }

            @CommandExecutor
            fun c2(): String {
                return ""
            }

            @CommandExecutor
            fun c3(): String {
                return ""
            }
        }.getFirstCommandExecutorFunction()

        assertEquals("c2", commandExecutorFunction.name)
    }

    @Test
    fun `getting non-existing command executor should throw exception`() {
        assertThrows<IllegalStateException> {
            object : BaseCommand<String>("") {
                fun c(): String {
                    return ""
                }
            }.getFirstCommandExecutorFunction()
        }
    }

    @Test
    fun `should execute command`() {
        val command = object : BaseCommand<String>("") {
            @CommandExecutor
            fun c(): String {
                return "abc"
            }
        }

        assertEquals("abc", tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString())
    }

    @Test
    fun `executing command with wrong return type should throw exception`() {
        val command = object : BaseCommand<String>("") {
            @CommandExecutor
            fun c(): Int {
                return 1
            }
        }

        assertThrows<ClassCastException> {
            tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString()
        }
    }
}
