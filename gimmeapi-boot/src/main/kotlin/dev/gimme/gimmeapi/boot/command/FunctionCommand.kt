package dev.gimme.gimmeapi.boot.command

import dev.gimme.gimmeapi.boot.command.executor.CommandExecutor
import dev.gimme.gimmeapi.boot.command.executor.UnsupportedParameterException
import dev.gimme.gimmeapi.boot.command.executor.getDefaultValue
import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.ParameterTypes
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents an easy-to-set-up command with automatic generation of some properties derived from a member function
 * marked with @[CommandExecutor].
 *
 * If a method in this is marked with @[CommandExecutor], the command's [parameters] and [usage] are automatically
 * derived from it, and it gets called called when the command is [execute]d.
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

    private val commandExecutorFunction: KFunction<T> = getFirstCommandExecutorFunction()
    private val commandExecutorAnnotation: CommandExecutor = commandExecutorFunction.findAnnotation()!!
    private val COMMAND_SENDER_TYPE: KType = CommandSender::class.createType()

    final override var parameters: CommandParameterSet = generateParameters()
    final override var usage: String = generateUsage()

    /**
     * Attempts to execute this command as the [commandSender] with the args mapping of parameters to arguments and
     * returns the result.
     *
     * The [args] have to fit the parameters of the function annotated with @[CommandExecutor].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        val function = commandExecutorFunction

        val params: List<KParameter> = function.parameters

        // First argument has to be the instance (command)
        val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(params[0] to this)

        // Inject command sender
        params
            .filter { it.type.isSubtypeOf(COMMAND_SENDER_TYPE) }
            .forEach { senderParam ->
                typedArgsMap[senderParam] = senderParam.type.jvmErasure.safeCast(commandSender)
                    ?: throw ErrorCode.INCOMPATIBLE_SENDER.createException()
            }

        args.forEach { (key, value) ->
            val p = params.find { it.name == key.id } ?: throw ErrorCode.INVALID_PARAMETER.createException(key.id)
            typedArgsMap[p] = value
        }

        function.isAccessible = true
        return function.callBy(typedArgsMap)
    }

    /**
     * Generates a set of [CommandParameter]s that matches the parameters of the [FunctionCommand.commandExecutorFunction] with default values from
     * [FunctionCommand.commandExecutorAnnotation].
     */
    private fun generateParameters(): CommandParameterSet {
        val usedFlags = mutableSetOf<Char>()

        val valueParameters = commandExecutorFunction.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.type.isSubtypeOf(COMMAND_SENDER_TYPE) }

        return CommandParameterSet(
            valueParameters.map { param ->
                val name = param.name ?: throw UnsupportedParameterException(param)
                val id = name.splitCamelCase("-")
                val displayName = name.splitCamelCase(" ")
                val flags = generateFlags(id, usedFlags)
                val defaultValue = commandExecutorAnnotation.getDefaultValue(valueParameters.indexOf(param))
                usedFlags.addAll(flags)

                val vararg = param.isVararg || param.type.jvmErasure.isSuperclassOf(List::class)
                val klass: KClass<*> = if (vararg) {
                    param.type.arguments.firstOrNull()?.type?.jvmErasure
                        ?: throw RuntimeException("Unsupported parameter type: ${param.type}") // TODO: exception type
                } else {
                    param.type.jvmErasure
                }
                val optional = param.isOptional || defaultValue?.value != null

                val type = ParameterTypes.get(klass)

                CommandParameter(
                    id = id,
                    displayName = displayName,
                    type = type,
                    vararg = vararg,
                    optional = optional,
                    suggestions = type.values ?: { setOf() },
                    flags = flags,
                    defaultValue = defaultValue
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
     * Returns the first found method that is annotated with @[CommandExecutor].
     *
     * @param T the command response type
     * @throws IllegalStateException if there is no method annotated with @[CommandExecutor] or if it has the wrong return
     * type
     */
    @Throws(CommandException::class)
    internal fun <T> Command<T>.getFirstCommandExecutorFunction(): KFunction<T> {
        // Look through the public methods in the command class
        for (function in this::class.memberFunctions) {
            // Make sure it has the right annotation
            if (!function.hasAnnotation<CommandExecutor>()) continue

            return try {
                @Suppress("UNCHECKED_CAST")
                function as KFunction<T>
            } catch (e: ClassCastException) {
                throw ClassCastException(
                    "The return type: \"${function.returnType.jvmErasure.qualifiedName}\" of the" +
                            " command executor function: \"${function.name}\" in the command: \"$id\" does not match the" +
                            " command's return type."
                )
            }
        }

        throw IllegalStateException("No function marked with @${CommandExecutor::class.simpleName} in the command: \"${this.id}\"")
    }
}
