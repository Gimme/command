package dev.gimme.gimmeapi.command.function

import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.sender.CommandSender

/**
 * Marks a function in a [Command] class as a command executor meaning it gets called when that command gets executed
 * with arguments that conform to the function's parameters.
 *
 * Supported parameter types:
 * - String
 * - Int
 * - Double
 * - Boolean
 *
 * The parameters can be vararg, nullable and have default values. But varargs and defaults have to come last and vararg
 * cannot be nullable. The point of nullable types is to allow null default values.
 *
 * Only one function should be marked in the same class.
 *
 * If the first parameter is a [CommandSender] it gets auto-injected.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandExecutor(
    /**
     * A list of default values to be used for optional parameters when omitted, where each value corresponds to the
     * command parameter with the same index.
     */
    vararg val defaultValues: String = [],

    /**
     * A list of visual representations for the default values to be shown in the command's usage info, where each value
     * corresponds to the command parameter with the same index.
     */
    val defaultValueRepresentations: Array<out String> = [],
) {
}
