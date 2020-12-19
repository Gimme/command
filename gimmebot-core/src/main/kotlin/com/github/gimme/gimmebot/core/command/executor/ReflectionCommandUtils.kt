package com.github.gimme.gimmebot.core.command.executor

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandResponse
import com.github.gimme.gimmebot.core.command.CommandSender
import java.security.InvalidParameterException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

private val COMMAND_SENDER_TYPE: KType = CommandSender::class.createType()
private val NULLABLE_COMMAND_RESPONSE_TYPE: KType = CommandResponse::class.createType(emptyList(), true)

/**
 * Attempts to execute the given [command] as the given [commandSender], and returns the optional command response if
 * the given [args] fit the parameters of a function in the [command] annotated with @[CommandExecutor] and it was
 * successfully called.
 */
internal fun tryExecuteCommandByReflection(
    command: Command,
    commandSender: CommandSender,
    args: List<String>,
): CommandResponse? {
    val function = command.getFirstCommandExecutorFunction()

    return attemptToCallFunction(function, command, commandSender, args)
}

/**
 * Returns the first found method that is annotated with @[CommandExecutor].
 *
 * @throws IllegalStateException if there is no method annotated with @[CommandExecutor]
 */
internal fun Command.getFirstCommandExecutorFunction(): KFunction<*> {
    // Look through the public methods in the command class
    for (method in this.javaClass.methods) {
        val function = method.kotlinFunction ?: continue
        // Make sure it has the right annotation
        if (!function.hasAnnotation<CommandExecutor>()) continue

        return function
    }

    throw IllegalStateException("No function marked with @" + CommandExecutor::class.simpleName + " in the command \""
            + this.name + "\"")
}

/**
 * Attempts to call the specified [function] in the given [command] as the given [commandSender], and returns the
 * optional command response if the given [args] fit the parameters of the [function] and it was successfully called.
 */
// TODO: return response with error message instead of null
private fun attemptToCallFunction(
    function: KFunction<*>,
    command: Command,
    commandSender: CommandSender,
    args: List<String>,
): CommandResponse? {
    val parameters: List<KParameter> = function.parameters

    // First argument has to be the instance (command)
    val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(Pair(parameters[0], command))
    //val typedArgs: MutableList<Any?> = mutableListOf(command)

    var paramIndex = 1 // Current parameter index
    var argIndex = 0 // Current argument index

    // If the first parameter has the command sender type, we inject it
    if (parameters.size > paramIndex) {
        val firstParam: KParameter = parameters[paramIndex]
        if (firstParam.type.isSubtypeOf(COMMAND_SENDER_TYPE)) {
            typedArgsMap[firstParam] =
                firstParam.type.jvmErasure.safeCast(commandSender) ?: return CommandResponse.INCOMPATIBLE_SENDER
            paramIndex++
        }
    }

    val amountOfInputParameters = parameters.size - paramIndex
    var amountOfOptionalArgs = 0
    for (i in parameters.size - 1 downTo 0) {
        if (!parameters[i].isOptional) break
        amountOfOptionalArgs++
    }
    val hasVararg = parameters[parameters.size - 1].isVararg
    val minRequiredAmountOfArgs = amountOfInputParameters - (if (hasVararg) 1 else 0) - amountOfOptionalArgs

    if (args.size < minRequiredAmountOfArgs) return CommandResponse.TOO_FEW_ARGUMENTS
    if (!hasVararg && args.size > amountOfInputParameters) return CommandResponse.TOO_MANY_ARGUMENTS

    while (argIndex < args.size) {
        if (paramIndex >= parameters.size) return CommandResponse.TOO_MANY_ARGUMENTS
        val param = parameters[paramIndex]
        val arg = args[argIndex]

        if (param.isVararg) {
            val parameterType: ParameterType = ParameterType.fromArrayClass(param)
                ?: throw InvalidParameterException("The function: \"" + function.name + "\" in " + command.javaClass.name +
                        " has an unsupported parameter type: " + param.type.jvmErasure.jvmName)

            val varargCollection = computeVarargs(parameterType, args, argIndex)

            argIndex += varargCollection.size
            typedArgsMap[param] = parameterType.castArray(varargCollection)
        } else {
            val value = ParameterType.fromClass(param)?.castArg(arg)
            if (value == null && !param.type.isMarkedNullable) return CommandResponse.INVALID_ARGUMENT
            typedArgsMap[param] = value
            argIndex++
        }

        paramIndex++
    }

    if (paramIndex < parameters.size && parameters[paramIndex].isVararg) {
        typedArgsMap[parameters[paramIndex]] =
            ParameterType.fromArrayClass(parameters[paramIndex])!!.castArray(mutableListOf<String>())
    }

    return if (function.returnType.isSubtypeOf(NULLABLE_COMMAND_RESPONSE_TYPE)) {
        function.callBy(typedArgsMap) as CommandResponse?
    } else {
        function.callBy(typedArgsMap)
        null
    }
}

/**
 * Returns a typed collection of all values from the given [args] starting from the specified [startIndex] until an
 * arg is reached that cannot be casted to the type of the given vararg [param].
 */
private fun computeVarargs(param: ParameterType, args: List<String>, startIndex: Int): Collection<*> {
    val list: MutableList<Any> = mutableListOf()

    var i = startIndex
    while (i < args.size) {
        val value = param.castArg(args[i++]) ?: break
        list.add(value)
    }

    return list
}
