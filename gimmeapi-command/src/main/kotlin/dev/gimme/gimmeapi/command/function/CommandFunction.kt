package dev.gimme.gimmeapi.command.function

/**
 * Marks a function in a [FunctionCommand] class as a command executor, meaning it gets called when that command gets
 * executed with arguments that conform to the function's parameters.
 *
 * Only one command function is allowed per class.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandFunction
