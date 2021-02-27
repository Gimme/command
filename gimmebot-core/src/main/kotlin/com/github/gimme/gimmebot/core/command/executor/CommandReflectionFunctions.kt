package com.github.gimme.gimmebot.core.command.executor

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandResponse
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.ErrorCode
import java.security.InvalidParameterException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val COMMAND_SENDER_TYPE: KType = CommandSender::class.createType()

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

    return attemptToCallFunction(function, command, commandSender, args, mapOf()) // TODO: supply with named args
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

    throw IllegalStateException("No function marked with @" + CommandExecutor::class.simpleName + " in the command \""
            + this.name + "\"")
}

/**
 * Attempts to call the specified [function] in the given [command] as the given [commandSender], and returns the
 * optional command response if the given [orderedArgs] and [namedArgs] fit the parameters of the [function] and it was
 * successfully called.
 *
 * @param T the command response type
 * @throws CommandException if the command execution was unsuccessful
 */
@Throws(CommandException::class)
private fun <T> attemptToCallFunction(
    function: KFunction<T>,
    command: Command<T>,
    commandSender: CommandSender,
    orderedArgs: List<String>,
    namedArgs: Map<String, String>,
): T {
    val parameters: List<KParameter> = function.parameters

    // First argument has to be the instance (command)
    val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(Pair(parameters[0], command))

    // If the first parameter is of a command sender type, inject it

    val param2 = parameters.getOrNull(1)
    if (param2 != null && param2.type.isSubtypeOf(COMMAND_SENDER_TYPE)) {
        typedArgsMap[param2] =
            param2.type.jvmErasure.safeCast(commandSender) ?: throw ErrorCode.INCOMPATIBLE_SENDER.createException()
    }

    mergeArgs(function, orderedArgs, namedArgs, typedArgsMap).forEach { (index, arg) ->
        val param = parameters[index]
        val value = if (param.isVararg) ParameterType.fromArrayClass(param)?.castArray(arg as Collection<*>)
            ?: throw ErrorCode.INVALID_ARGUMENT.createException(arg)
        else ParameterType.fromClass(param)?.castArg(arg) ?: throw ErrorCode.INVALID_ARGUMENT.createException(arg)
        typedArgsMap[param] = value
    }

    return function.callBy(typedArgsMap)
}

/**
 * Merges [orderedArgs] amd [namedArgs] into one map of args by parameter index for the specified [function].
 *
 * Any parameter among the already [injectedArgs] is ignored.
 *
 * @throws CommandException if there was an issue merging or matching the args
 */
@Throws(CommandException::class)
private fun mergeArgs(
    function: KFunction<*>,
    orderedArgs: List<Any>,
    namedArgs: Map<String, Any>,
    injectedArgs: Map<KParameter, Any?>,
): Map<Int, Any> {
    val mergedArgs = mutableMapOf<Int, Any>()
    var orderedArgsIndex = 0

    val namedArgsCopy = namedArgs.toMutableMap()

    function.parameters
        .filter { it.kind == KParameter.Kind.VALUE && !injectedArgs.containsKey(it) }
        .forEach loop@{ param ->
            var value = namedArgsCopy.remove(param.name)

            if (value == null) {
                if (param.isVararg) {
                    val parameterType = ParameterType.fromArrayClass(param)
                        ?: throw InvalidParameterException("The function: \"" + function.name + "\"" +
                                " has an unsupported parameter type: " + param.type.jvmErasure.jvmName)
                    value = computeVarargs(parameterType, orderedArgs, orderedArgsIndex)
                    orderedArgsIndex += value.size
                } else {
                    if (orderedArgsIndex >= orderedArgs.size) {
                        if (param.isOptional) return@loop
                        throw ErrorCode.TOO_FEW_ARGUMENTS.createException()
                    }

                    value = orderedArgs[orderedArgsIndex++]
                }
            }

            mergedArgs[param.index] = value
        }

    if (namedArgsCopy.isNotEmpty()) throw ErrorCode.INVALID_PARAMETER.createException(namedArgsCopy.iterator()
        .next().key)
    if (orderedArgsIndex < orderedArgs.size) throw ErrorCode.TOO_MANY_ARGUMENTS.createException()

    return mergedArgs
}

/**
 * Returns a typed collection of all values from the given [args] starting from the specified [startIndex] until an
 * arg is reached that cannot be casted to the type of the given vararg [param].
 */
private fun computeVarargs(param: ParameterType, args: List<Any>, startIndex: Int): Collection<*> {
    val list: MutableList<Any> = mutableListOf()

    var i = startIndex
    while (i < args.size) {
        val value = param.castArg(args[i++]) ?: break
        list.add(value)
    }

    return list
}

/**
 * Returns all data parameters from the given [function].
 *
 * This excludes the self reference and the CommandSender if present.
 */
internal fun getCommandDataParameters(function: KFunction<*>): List<KParameter> {
    val parameters: List<KParameter> = function.parameters
    val paramsToSkip = if (getCommandSenderParameter(parameters) != null) 2 else 1

    return function.parameters.drop(paramsToSkip)
}

/**
 * Returns the CommandSender parameter if present as the first declared parameter, else null.
 */
internal fun getCommandSenderParameter(parameters: List<KParameter>): KParameter? {
    val param2 = parameters.getOrNull(1)
    return if (param2 != null && param2.type.isSubtypeOf(COMMAND_SENDER_TYPE)) param2 else null
}

/**
 * Returns the default value for the parameter at the specified [index] as defined in the given [commandExecutor], or
 * null if no default value for the specified [index].
 *
 * Empty strings are treated as null (no default value).
 */
internal fun getDefaultValue(commandExecutor: CommandExecutor, index: Int): String? {
    val value = commandExecutor.defaultValues.getOrNull(index)
    return if (value.isNullOrEmpty()) null else value
}
