package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.annotations.Parameter
import dev.gimme.gimmeapi.command.annotations.getDefaultValue
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.node.BaseCommandNode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * A base implementation of command with useful "hashCode" and "equals" methods.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
    override var usage: String = "",
    override var parameters: CommandParameterSet = CommandParameterSet(),
) : BaseCommandNode(name, parent, aliases), Command<T> {

    override var senderTypes: Set<KClass<*>>? = null

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override fun executeBy(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        if (senderTypes?.any {
                commandSender::class.isSubclassOf(it) || SenderTypes.adapt(commandSender, it) != null
            } == false) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

        return execute(commandSender, args)
    }

    /**
     * @see executeBy
     */
    @Throws(CommandException::class)
    protected abstract fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T

    protected class ParameterSettings(
        val name: String,
        val annotation: Parameter?,
        val type: KType,
    )

    protected class SenderSettings(
        val type: KType,
    )

    /**
     * Generates a set of [CommandParameter]s based on the supplied [parameterSettings].
     */
    protected fun generateParameters(parameterSettings: List<ParameterSettings>) {
        val usedFlags = mutableSetOf<Char>()

        this.parameters = CommandParameterSet(parameterSettings.map { settings ->
            val name = settings.name

            val id = name.splitCamelCase("-")
            val displayName = name.splitCamelCase(" ")
            val flags = generateFlags(id, usedFlags)
            val defaultValue: DefaultValue? = settings.annotation?.getDefaultValue()
            usedFlags.addAll(flags)

            val jvmErasure = settings.type.jvmErasure
            val form = when {
                jvmErasure.isSuperclassOf(MutableList::class) -> CommandParameter.Form.LIST
                jvmErasure.isSuperclassOf(MutableSet::class) -> CommandParameter.Form.SET
                else -> CommandParameter.Form.VALUE
            }
            val klass: KClass<*> = if (form.isCollection) {
                settings.type.arguments.firstOrNull()?.type?.jvmErasure
                    ?: throw RuntimeException("Unsupported parameter type: ${settings.type}") // TODO: exception type
            } else {
                jvmErasure
            }

            val type = ParameterTypes.get(klass)

            if (!settings.type.isMarkedNullable && defaultValue != null && defaultValue.value == null) {
                throw IllegalStateException("Parameter \"$id\" has a null default value for a type marked as non-nullable") // TODO: exception type
            }

            CommandParameter(
                id = id,
                displayName = displayName,
                type = type,
                form = form,
                suggestions = type.values ?: { setOf() },
                flags = flags,
                defaultValue = defaultValue,
                description = settings.annotation?.description?.ifEmpty { null }
            )
        })
    }

    /**
     * Generates a set of allowed sender types based on the supplied [senderSettings].
     */
    protected fun generateSenders(senderSettings: List<SenderSettings>) {
        var requiredSender: KClass<*>? = null
        var optionalSenders: MutableSet<KClass<*>>? = null

        senderSettings.forEach {
            val klass = it.type.jvmErasure
            val optional = it.type.isMarkedNullable

            if (!optional) {
                if (requiredSender != null) throw IllegalStateException("Only one sender type can be required (i.e., non-null)") // TODO: exception type
                requiredSender = klass
            } else {
                optionalSenders = (optionalSenders ?: mutableSetOf()).apply { add(klass) }
            }
        }

        this.senderTypes = requiredSender?.let { setOf(it) } ?: optionalSenders
    }

    /**
     * Generates a set of flags from the [string] without clashing with any of the [unavailableFlags].
     */
    private fun generateFlags(string: String, unavailableFlags: Set<Char> = setOf()): Set<Char> {
        require(string.isNotEmpty())

        val flags = mutableSetOf<Char>()

        val firstLetterLower = string.first().let { if (it.isUpperCase()) it.lowercaseChar() else it }
        val firstLetterUpper = firstLetterLower.uppercaseChar()

        if (!unavailableFlags.contains(firstLetterLower)) flags.add(firstLetterLower)
        else if (!unavailableFlags.contains(firstLetterUpper)) flags.add(firstLetterUpper)

        return flags
    }
}
