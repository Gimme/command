package dev.gimme.command.annotations

/**
 * Marks a function as the command template.
 *
 * The command gets its parameters from the function and calls the function on execution.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandFunction
