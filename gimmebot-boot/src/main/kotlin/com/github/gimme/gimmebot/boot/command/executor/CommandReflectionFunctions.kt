package com.github.gimme.gimmebot.boot.command.executor

import com.github.gimme.gimmebot.boot.command.exceptions.UnsupportedParameterException
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandParameter
import com.github.gimme.gimmebot.core.command.CommandParameterSet
import com.github.gimme.gimmebot.core.command.CommandResponse
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.ErrorCode
import org.apache.commons.lang3.StringUtils
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure

private val COMMAND_SENDER_TYPE: KType = CommandSender::class.createType()

/**
 * Generates and returns a set of [CommandParameter]s that match the parameters of the first member function annotated with
 * @[CommandExecutor].
 */
internal fun Command<*>.generateParameters(): CommandParameterSet {
    val usedFlags = mutableSetOf<Char>()

    val function = this.getFirstCommandExecutorFunction()

    return CommandParameterSet(
        function.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.type.isSubtypeOf(COMMAND_SENDER_TYPE) }
            .map { param ->
                val name = param.name ?: throw UnsupportedParameterException(param)
                val id = name.splitCamelCase("-")
                val flags = generateFlags(id, usedFlags)
                usedFlags.addAll(flags)

                CommandParameter(
                    id = id,
                    displayName = name.splitCamelCase(" "),
                    type = commandParameterTypeFrom(param),
                    vararg = param.isVararg,
                    optional = param.isOptional,
                    flags = flags,
                )
            }
            .toList()
    )
}

/**
 * Generates a set of flags from the [string] without clashing with any of the [unavailableFlags].
 */
private fun generateFlags(string: String, unavailableFlags: Set<Char> = setOf()): Set<Char> {
    require(string.isNotEmpty())

    val flags = mutableSetOf<Char>()

    val firstLetterLower = string.first().let { if (it.isUpperCase()) it.toLowerCase() else it }
    val firstLetterUpper = firstLetterLower.toUpperCase()

    if (!unavailableFlags.contains(firstLetterLower)) flags.add(firstLetterLower)
    else if (!unavailableFlags.contains(firstLetterUpper)) flags.add(firstLetterUpper)

    return flags
}

/**
 * Converts this string from camel case to separate lowercase words separated by the specified [separator].
 */
internal fun String.splitCamelCase(separator: String): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), separator)
        .toLowerCase()
        .replace("$separator $separator", separator)

/**
 * Attempts to execute the given [command] as the given [commandSender], and returns the optional [CommandResponse] if
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

        @Suppress("UNCHECKED_CAST")
        return function as KFunction<T>
    }

    throw IllegalStateException("No function marked with @${CommandExecutor::class.simpleName} in the command \"${this.name}\"")
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
        typedArgsMap[param] = arg
    }

    return function.callBy(typedArgsMap)
}

/**
 * Merges [orderedArgs] amd [namedArgs] into one list matching the order of the [command]s parameters.
 *
 * @throws CommandException if there was an issue merging or matching the args
 */
@Throws(CommandException::class)
private fun mergeArgs(
    command: Command<*>,
    orderedArgs: List<String>,
    namedArgs: Map<String, String>,
): List<Any> {
    val mergedArgs = mutableListOf<Any>()
    var orderedArgsIndex = 0

    val unusedNamedArgs = namedArgs.keys.toMutableSet()

    command.parameters.forEach { param ->
        var value: Any? = namedArgs[param.id]
        unusedNamedArgs.remove(param.id)

        if (value == null) {
            if (param.vararg) {
                value = orderedArgs.subList(orderedArgsIndex, orderedArgs.size)
                orderedArgsIndex += value.size
            } else {
                if (orderedArgsIndex >= orderedArgs.size) {
                    if (param.optional) return@forEach
                    throw ErrorCode.TOO_FEW_ARGUMENTS.createException()
                }

                value = orderedArgs[orderedArgsIndex++]
            }
        }

        val typedArg = param.type.convert(value)
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
 * Empty strings are treated as null (no default value).
 */
internal fun CommandExecutor.getDefaultValue(index: Int): String? {
    val value = this.defaultValues.getOrNull(index)
    return if (value.isNullOrEmpty()) null else value
}
