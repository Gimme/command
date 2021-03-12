package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class BaseCommandTest {

    @ParameterizedTest
    @CsvSource(
        "test, test",
        "test, TEST",
        "test, tESt",
    )
    fun `command names should become lower case`(expectedName: String, inputName: String) {
        val command = object : DefaultBaseCommand(inputName) {}
        assertEquals(expectedName, command.name)
    }
}
