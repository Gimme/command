package dev.gimme.gimmeapi.command.function

import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.ParameterTypes
import dev.gimme.gimmeapi.command.SenderTypes
import dev.gimme.gimmeapi.command.annotations.Parameter
import dev.gimme.gimmeapi.command.annotations.Sender
import dev.gimme.gimmeapi.command.annotations.getDefaultValue
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents an easy-to-set-up command with automatic generation of some properties derived from a member function
 * marked with @[CommandFunction].
 *
 * If a method in this is marked with @[CommandFunction], the command's [parameters] and [usage] are automatically
 * derived from it, and it gets called called when the command is executed.
 *
 * @param T the response type
 */
abstract class FunctionCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    summary: String = "",
    description: String = "",
) : BaseCommand<T>(
    name = name,
    parent = parent,
    aliases = aliases,
    summary = summary,
    description = description,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    private val commandFunction: KFunction<T> = getFirstCommandFunction()
    private val commandFunctionAnnotation: CommandFunction = commandFunction.findAnnotation()!!

    final override var parameters: CommandParameterSet = generateParameters()
    final override var usage: String = generateUsage()

    private var requiredSender: KClass<*>? = null
    private var optionalSenders: MutableSet<KClass<*>>? = null
    final override val senderTypes: Set<KClass<*>>? get() = requiredSender?.let { setOf(it) } ?: optionalSenders

    /**
     * Attempts to execute this command as the [commandSender] with the args mapping of parameters to arguments and
     * returns the result.
     *
     * The [args] have to fit the parameters of the function annotated with @[CommandFunction].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        val function = commandFunction

        val params: List<KParameter> = function.parameters

        // First argument has to be the instance (command)
        val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(params[0] to this)

        // Inject command senders
        params.forEach { param ->
            val sender: Any? = when {
                commandSender::class.isSubclassOf(param.type.jvmErasure) -> {
                    param.type.jvmErasure.safeCast(commandSender)
                }
                param.hasAnnotation<Sender>() -> {
                    SenderTypes.adapt(commandSender, param.type.jvmErasure)
                }
                else -> return@forEach
            }

            if (sender == null && !param.isOptional) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

            typedArgsMap[param] = sender
        }

        args.forEach { (key, value) ->
            val p = params.find { it.name == key.id } ?: throw ErrorCode.INVALID_PARAMETER.createException(key.id)
            typedArgsMap[p] = value
        }

        function.isAccessible = true
        return function.callBy(typedArgsMap)
    }

    /**
     * Generates a set of [CommandParameter]s that matches the parameters of the [FunctionCommand.commandFunction] with default values from
     * [FunctionCommand.commandFunctionAnnotation].
     */
    private fun generateParameters(): CommandParameterSet {
        val usedFlags = mutableSetOf<Char>()

        val senderParameters: List<KParameter> = commandFunction.parameters
            .filter { it.kind == KParameter.Kind.VALUE && (it.type.isSubtypeOf(CommandSender::class.createType()) || it.hasAnnotation<Sender>()) }
        val valueParameters: List<KParameter> = commandFunction.parameters
            .minus(senderParameters)
            .filter { it.kind == KParameter.Kind.VALUE }


        senderParameters.forEach {
            val optional = it.isOptional
            val klass = it.type.jvmErasure

            if (!optional) {
                if (requiredSender != null) throw IllegalStateException("Only one sender type can be required (i.e., non-null)") // TODO: exception type
                requiredSender = klass
            } else {
                optionalSenders = (optionalSenders ?: mutableSetOf()).apply { add(klass) }
            }
        }

        return CommandParameterSet(
            valueParameters.map { param ->
                val parameterAnnotation: Parameter? = param.findAnnotation()

                val name = param.name ?: throw UnsupportedParameterException(param)
                val id = name.splitCamelCase("-")
                val displayName = name.splitCamelCase(" ")
                val flags = generateFlags(id, usedFlags)
                val defaultValue: DefaultValue? = parameterAnnotation?.getDefaultValue()
                usedFlags.addAll(flags)

                val jvmErasure = param.type.jvmErasure
                val form = when {
                    jvmErasure.isSuperclassOf(MutableList::class) -> CommandParameter.Form.LIST
                    jvmErasure.isSuperclassOf(MutableSet::class) -> CommandParameter.Form.SET
                    else -> CommandParameter.Form.VALUE
                }
                val klass: KClass<*> = if (form.isCollection) {
                    param.type.arguments.firstOrNull()?.type?.jvmErasure
                        ?: throw RuntimeException("Unsupported parameter type: ${param.type}") // TODO: exception type
                } else {
                    jvmErasure
                }

                val type = ParameterTypes.get(klass)

                if (!param.type.isMarkedNullable && defaultValue != null && defaultValue.value == null) {
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
                    description = parameterAnnotation?.description?.ifEmpty { null }
                )
            }.toList()
        )
    }

    /**
     * Generates a "usage string" that matches the [Command.parameters].
     */
    private fun Command<*>.generateUsage(): String {
        val sb = StringBuilder(name)

        parameters.forEach { parameter ->
            val defaultValueRepresentation = parameter.defaultValue?.representation
            val wrap = if (parameter.optional) Pair("[", "]") else Pair("<", ">")

            sb.append(" ${wrap.first}${parameter.id}${defaultValueRepresentation?.let { "=$defaultValueRepresentation" } ?: ""}${wrap.second}")
        }

        return sb.toString()
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

    /**
     * Returns the first found method that is annotated with @[CommandFunction].
     *
     * @param T the command response type
     * @throws IllegalStateException if there is no method annotated with @[CommandFunction] or if it has the wrong return
     * type
     */
    @Throws(CommandException::class)
    internal fun <T> Command<T>.getFirstCommandFunction(): KFunction<T> {
        // Look through the public methods in the command class
        for (function in this::class.memberFunctions) {
            // Make sure it has the right annotation
            if (!function.hasAnnotation<CommandFunction>()) continue

            return try {
                @Suppress("UNCHECKED_CAST")
                function as KFunction<T>
            } catch (e: ClassCastException) {
                throw ClassCastException(
                    "The return type: \"${function.returnType.jvmErasure.qualifiedName}\"" +
                            " of the command function: \"${function.name}\"" +
                            " in ${this::class.qualifiedName}" +
                            " does not match the command's return type."
                )
            }
        }

        throw IllegalStateException("No function marked with @${CommandFunction::class.simpleName} in ${this::class.qualifiedName}")
    }
}
