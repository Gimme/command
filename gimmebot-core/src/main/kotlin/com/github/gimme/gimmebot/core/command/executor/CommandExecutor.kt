package com.github.gimme.gimmebot.core.command.executor

/**
 * Marks a function in a [com.github.gimme.gimmebot.core.command.Command] class as a command executor meaning it
 * gets called when that command gets executed with arguments that conform to the function's parameters.
 *
 * Supported parameter types:
 * - String
 * - Int
 * - Double
 * - Boolean
 *
 * The parameters can be vararg, nullable and have default values. But varargs and defaults have to come last and vararg
 * cannot be nullable. Nullable parameters mean that if the given argument for that parameter cannot be converted to the
 * correct type, it becomes null instead (except for vararg).
 *
 * Only one function should be marked in the same class.
 *
 * If the first parameter is a [com.github.gimme.gimmebot.core.command.CommandSender] it gets auto-injected.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandExecutor()
