package dev.gimme.gimmeapi.core.common

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringsTest {

    @Test
    fun `camel case should be split into separate lowercase words`() {
        assertAll(
            { assertEquals("lorem ipsum", "loremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "LoremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "lorem ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "Lorem Ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem-ipsum", "loremIpsum".splitCamelCase("-")) },
            { assertEquals("lorem-ipsum", "lorem ipsum".splitCamelCase("-")) },
            { assertEquals("lorem-2", "lorem2".splitCamelCase("-")) },
            { assertEquals("lorem-2-ipsum", "lorem2Ipsum".splitCamelCase("-")) },
        )
    }
}
