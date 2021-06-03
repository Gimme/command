package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
import com.github.gimme.gimmebot.core.command.parameter.CommandParameter
import com.github.gimme.gimmebot.core.command.parameter.CommandParameterSet
import com.github.gimme.gimmebot.core.command.parameter.DefaultValue
import com.github.gimme.gimmebot.core.command.sender.CommandSender
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

private val COMMAND_SENDER_TYPE: KType = CommandSender::class.createType()

/**
 * Generates a set of [CommandParameter]s that matches the parameters of the [function] with default values from
 * [commandExecutor].
 */
internal fun generateParameters(function: KFunction<Any?>, commandExecutor: CommandExecutor): CommandParameterSet {
    val usedFlags = mutableSetOf<Char>()

    val valueParameters = function.parameters
        .filter { it.kind == KParameter.Kind.VALUE && !it.type.isSubtypeOf(COMMAND_SENDER_TYPE) }

    return CommandParameterSet(
        valueParameters.map { param ->
            val name = param.name ?: throw UnsupportedParameterException(param)
            val id = name.splitCamelCase("-")
            val displayName = name.splitCamelCase(" ")
            val commandParameterType = ParameterTypes.get(param.type)
            val flags = generateFlags(id, usedFlags)
            val defaultValue = commandExecutor.getDefaultValue(valueParameters.indexOf(param))
            usedFlags.addAll(flags)

            CommandParameter(
                id = id,
                displayName = displayName,
                type = commandParameterType,
                suggestions = commandParameterType.values ?: { setOf() },
                vararg = param.isVararg,
                optional = param.isOptional || defaultValue?.value != null,
                flags = flags,
                defaultValue = defaultValue
            )
        }
            .toList()
    )
}

/**
 * Generates a "usage string" that matches the [Command.parameters].
 */
internal fun Command<*>.generateUsage(): String {
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
 * Converts this string from camel case to separate lowercase words (using the [locale]) separated by the specified
 * [separator].
 */
internal fun String.splitCamelCase(separator: String, locale: Locale = Locale.ROOT): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), separator)
        .lowercase(locale)
        .replace("$separator $separator", separator)

/**
 * Attempts to execute the given [command] as the given [commandSender], and returns the response if
 * the given [args] fit the parameters of a function in the [command] annotated with @[CommandExecutor] and it was
 * successfully called.
 *
 * @param T the command response type
 * @throws CommandException if the command execution was unsuccessful
 */
@Throws(CommandException::class)
internal fun <T> tryExecuteCommandByReflection(
    command: Command<T>,
    commandSender: CommandSender,
    args: List<String>,
): T {
    val function = command.getFirstCommandExecutorFunction()

    return command.attemptToCallFunction(function, commandSender, args, mapOf()) // TODO: supply with named args
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

/**
 * Attempts to call the specified [function] in this [Command] as the given [commandSender], and returns the
 * optional command response if the given [orderedArgs] and [namedArgs] fit the parameters of the [function] and it was
 * successfully called.
 *
 * @param T the command response type
 * @throws CommandException if the command execution was unsuccessful
 */
@Throws(CommandException::class)
private fun <T> Command<T>.attemptToCallFunction(
    function: KFunction<T>,
    commandSender: CommandSender,
    orderedArgs: List<String>,
    namedArgs: Map<String, String>,
): T {
    val params: List<KParameter> = function.parameters
    var paramsIndex = 0

    // First argument has to be the instance (command)
    val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(Pair(params[paramsIndex], this))
    paramsIndex++

    // If the first parameter is of a command sender type, inject it
    val param2 = params.getOrNull(paramsIndex)
    if (param2 != null && param2.type.isSubtypeOf(COMMAND_SENDER_TYPE)) {
        typedArgsMap[param2] = param2.type.jvmErasure.safeCast(commandSender)
            ?: throw ErrorCode.INCOMPATIBLE_SENDER.createException()
        paramsIndex++
    }

    mergeArgs(this, orderedArgs, namedArgs).forEach { arg ->
        val param = params[paramsIndex++]
        if (arg != null) typedArgsMap[param] = arg
    }

    function.isAccessible = true
    return function.callBy(typedArgsMap)
}

/**
 * Merges [orderedArgs] amd [namedArgs] into one list matching the order of the [command]s parameters. Null values that
 * are returned are expected to receive default values through the [KParameter].
 *
 * @throws CommandException if there was an issue merging or matching the args
 */
@Throws(CommandException::class)
private fun mergeArgs(
    command: Command<*>,
    orderedArgs: List<String>,
    namedArgs: Map<String, String>,
): List<Any?> {
    val mergedArgs = mutableListOf<Any?>()
    var orderedArgsIndex = 0

    val unusedNamedArgs = namedArgs.keys.toMutableSet()

    command.parameters.forEach { param ->
        val values = mutableListOf<String>()
        namedArgs[param.id]?.let { values.add(it) }
        unusedNamedArgs.remove(param.id)

        if (values.isEmpty()) {
            if (param.vararg) {
                values.addAll(orderedArgs.subList(orderedArgsIndex, orderedArgs.size))
                orderedArgsIndex += values.size
            } else {
                if (orderedArgsIndex >= orderedArgs.size) {
                    if (param.optional) {
                        param.defaultValue?.value?.let {
                            values.add(it)
                        } ?: run {
                            mergedArgs.add(null)
                            return@forEach
                        }
                    } else throw ErrorCode.TOO_FEW_ARGUMENTS.createException()
                } else {
                    values.add(orderedArgs[orderedArgsIndex++])
                }
            }
        }

        if (param.type.singular) {
            if (values.size < 1) throw ErrorCode.REQUIRED_PARAMETER.createException(param.id)
            if (values.size > 1) throw ErrorCode.TOO_MANY_ARGUMENTS.createException("${values.drop(1)}")
        }
        val typedArg = param.type.convert(values)
        mergedArgs.add(typedArg)
    }

    if (unusedNamedArgs.size > 0) throw ErrorCode.INVALID_PARAMETER.createException(unusedNamedArgs.first())
    if (orderedArgsIndex < orderedArgs.size) throw ErrorCode.TOO_MANY_ARGUMENTS.createException()

    return mergedArgs
}

/**
 * Returns the default value for the parameter at the specified [index], or null if no default value for the specified
 * [index].
 *
 * Empty strings are treated and returned as null (no default value).
 */
internal fun CommandExecutor.getDefaultValue(index: Int): DefaultValue? {
    val value = this.defaultValues.getOrNull(index)?.let { if (it.isEmpty()) null else it }
    val representation = this.defaultValueRepresentations.getOrNull(index)?.let { if (it.isEmpty()) null else it }

    if (representation != null) return DefaultValue(value, representation)
    if (value != null) return DefaultValue(value, value)
    return null
}
