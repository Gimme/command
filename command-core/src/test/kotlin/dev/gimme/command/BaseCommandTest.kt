package dev.gimme.command

import dev.gimme.command.annotations.Parameter
import dev.gimme.command.annotations.Sender
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.sender.CommandSender
import dev.gimme.command.sender.SenderTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseCommandTest {

    private var called = false
    private val dummySender = DUMMY_COMMAND_SENDER

    @Test
    fun senderAnnotation() {
        testCommand(object : BaseCommand<Any>("test-command") {
            @Sender
            private lateinit var sender: CommandSender

            override fun call() {
                assertEquals(dummySender, sender)

                called = true
            }
        })
    }

    @Test
    fun multipleSenders() {
        class Player
        class PlayerSender(val player: Player) : CommandSender {
            override val name = "player"
            override fun sendMessage(message: String) {}
        }
        SenderTypes.registerAdapter<Player, PlayerSender> { it.player }

        class TestCommand(
            val expectedSender1: Any?,
            val expectedSender2: Any?,
            val expectedSender3: Any?,
        ) : BaseCommand<Any>("test-command") {

            @Sender
            private lateinit var sender: CommandSender
            @Sender
            private val playerSender: PlayerSender? = null
            @Sender
            private val player: Player? = null

            override fun call() {
                assertEquals(expectedSender1, sender)
                assertEquals(expectedSender2, playerSender)
                assertEquals(expectedSender3, player)

                called = true
            }
        }

        val player = Player()
        val playerSender = PlayerSender(player)
        testCommand(TestCommand(dummySender, null, null), sender = dummySender)
        testCommand(TestCommand(playerSender, playerSender, player), sender = playerSender)
    }

    @Test
    fun parameterAnnotation() {
        val arg1 = "arg1"
        val command = object : BaseCommand<Any>("test-command") {
            @Parameter
            private lateinit var param1: String

            override fun call() {
                assertEquals(arg1, param1)

                called = true
            }
        }

        testCommand(
            command,
            listOf(arg1),
        )
    }

    @Test
    fun parameterDelegate() {
        val arg1 = "arg1"
        val command = object : BaseCommand<Any>("test-command") {
            private val param1: String by param()

            override fun call() {
                assertEquals(arg1, param1)

                called = true
            }
        }

        testCommand(
            command,
            listOf(arg1),
        )
    }

    @Test
    fun `Handles various parameter types`() {
        val listArg = listOf("a", "b", "a")
        val halfArgs = listOf(
            "a",
            1,
            0.5,
            true,
            listArg.toList(),
            listArg.toSet(),
            listArg.toList(),
            listArg.toList(),
        )
        val args = halfArgs.plus(halfArgs)

        val command = object : BaseCommand<Any>("test-command") {

            val string1: String by param()
            val int1: Int by param()
            val double1: Double by param()
            val boolean1: Boolean by param()
            val list1: List<String> by param()
            val set1: Set<String> by param()
            val collection1: Collection<String> by param()
            val iterable1: Iterable<String> by param()

            override fun call() {
                val iter = args.iterator()

                assertEquals(iter.next(), string1)
                assertEquals(iter.next(), int1)
                assertEquals(iter.next(), double1)
                assertEquals(iter.next(), boolean1)
                assertEquals(iter.next(), list1)
                assertEquals(iter.next(), set1)
                assertEquals(iter.next(), collection1)
                assertEquals(iter.next(), iterable1)

                called = true
            }
        }

        testCommand(
            command,
            args,
        )
    }

    private fun testCommand(
        command: BaseCommand<*>,
        args: Map<CommandParameter, Any?> = mapOf(),
        sender: CommandSender = dummySender,
    ) {
        called = false
        command.execute(sender, args)
        assertTrue(called)
    }

    private fun testCommand(
        command: BaseCommand<*>,
        args: List<Any?>,
        sender: CommandSender = dummySender,
    ) = testCommand(
        command,
        command.parameters.mapIndexed { index, commandParameter ->
            commandParameter to args[index]
        }.toMap(),
        sender,
    )
}
