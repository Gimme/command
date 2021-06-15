package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.DUMMY_COMMAND_SENDER
import dev.gimme.gimmeapi.command.SenderTypes
import dev.gimme.gimmeapi.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class DelegatePropertyTest {

    @Test
    fun `Converts delegate properties to parameters that take arguments`() {
        val listInput = listOf("a", "b", "a")

        var called = false

        val command = object : PropertyCommand<Unit>("test-command") {

            val string: String by param()
            val int: Int by param()
            val double: Double by param()
            val boolean: Boolean by param()
            val list: List<String> by param()
            val set: Set<String> by param()
            val collection: Collection<String> by param()
            val iterable: Iterable<String> by param()

            override fun call() {
                called = true
                assertAll(
                    { assertEquals("a", string) },
                    { assertEquals(1, int) },
                    { assertEquals(0.5, double) },
                    { assertEquals(true, boolean) },
                    { assertEquals(listInput, list) },
                    { assertEquals(listInput.toSet(), set) },
                    { assertEquals(listInput, collection) },
                    { assertEquals(listInput, iterable) },
                )
            }
        }
        val input = mapOf<String, Any?>(
            "string" to "a",
            "int" to 1,
            "double" to 0.5,
            "boolean" to true,
        )
            .map { command.parameters[it.key]!! to it.value }
            .plus( command.parameters["list"]!! to listInput)
            .plus( command.parameters["set"]!! to listInput.toSet())
            .plus( command.parameters["collection"]!! to listInput)
            .plus( command.parameters["iterable"]!! to listInput)
            .toMap()

        assertFalse(called)
        command.executeBy(DUMMY_COMMAND_SENDER, input)
        assertTrue(called)
    }

    @Test
    fun `Converts delegate properties to command senders`() {
        open class Sender1 : CommandSender {
            override val name = "sender1"

            override fun sendMessage(message: String) {}
        }
        open class Sender2 : CommandSender {
            override val name = "sender2"

            override fun sendMessage(message: String) {}
        }


        val commandSender = Sender1()

        var called = false

        val command = object : PropertyCommand<Unit>("test-command") {

            val senderSuper: CommandSender by sender()
            val senderSub1: Sender1? by sender()
            val senderSub2: Sender2? by sender()

            override fun call() {
                called = true

                assertEquals(commandSender, senderSuper)
                assertEquals(commandSender, senderSub1)
                assertEquals("sender1", senderSub1!!.name)
                assertEquals(senderSub1!!.name, senderSuper.name)
                assertNull(senderSub2)
            }
        }

        assertFalse(called)
        command.executeBy(commandSender, mapOf())
        assertTrue(called)
    }

    @Test
    fun `Handles custom adapted senders`() {
        open class Player {
            val name = "player"
        }
        open class PlayerSender(val player: Player) : CommandSender {
            override val name = "sender"

            override fun sendMessage(message: String) {}
        }

        SenderTypes.registerAdapter { sender: PlayerSender -> sender.player }

        val commandSender = PlayerSender(Player())

        var called = false

        val command = object : PropertyCommand<Unit>("test-command") {

            val player: Player by sender()

            override fun call() {
                called = true

                assertEquals(commandSender.player, player)
                assertEquals("player", player.name)
            }
        }

        assertFalse(called)
        command.executeBy(commandSender, mapOf())
        assertTrue(called)
    }
}
