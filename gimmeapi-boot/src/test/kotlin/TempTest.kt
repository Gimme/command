
import dev.gimme.gimmeapi.command.ParameterTypes
import org.junit.jupiter.api.Test
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

class TempTest {

    @Test
    fun test1() {
        val type1 = Faction::class.createType(listOf(KTypeProjection.STAR))
        val type2 = Faction::class.createType(listOf(KTypeProjection.invariant(String::class.createType())))

        val result = type2.isSubtypeOf(type1)
        println("RESULT: $result")
    }

    @Test
    fun test3() {
        val factions = mapOf(
            "F1" to Faction("F11", 1),
            "F2" to Faction("F22", 2),
            "F3" to Faction("F33", 3),
        )
        val factions2 = mapOf(
            "F1" to Faction("F11", "a"),
            "F2" to Faction("F22", "b"),
            "F3" to Faction("F33", "c"),
        )

        ParameterTypes.register(typeArguments = listOf(null)) { factions[it] }
        ParameterTypes.register(typeArguments = listOf(Int::class.createType())) { factions[it] }
        ParameterTypes.register(typeArguments = listOf(String::class.createType())) { factions2[it] }

        /*val result = MyCommand().execute(
            DUMMY_COMMAND_SENDER,
            listOf("F2", "blue", "F2", "2")
        )

        println("RESULT: $result")*/
    }
}

/*class MyCommand : PropertyCommand<String>("my") {

    //TODO private val player: PlayerSender? by sender()
    private val sender: CommandSender by sender()

    @Suppress("DEPRECATION")
    private val a: Faction<*> by param()
    private val b: Color by param()
    private val c: Collection<Faction<*>> by param()
    private val d: Int? by param()

    override fun call(): String {
        return "$a-$b-${c}-${d}-${sender.name}"
    }
}*/

data class Faction<T>(val name: String, val t: T)

/*enum class Color {
    BLUE,
    RED,
}*/
