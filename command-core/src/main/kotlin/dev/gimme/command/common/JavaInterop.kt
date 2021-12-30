package dev.gimme.command.common

/**
 * Can be used as the version string in @[SinceKotlin] to make a declaration Java-only (hidden in Kotlin).
 *
 * @see JAVA_ONLY_WARNING
 */
internal const val JAVA_ONLY = "9999.9999"

/**
 * Can be used as the suppression name in @[Suppress] to suppress the [JAVA_ONLY] compilation warning.
 */
internal const val JAVA_ONLY_WARNING = "NEWER_VERSION_IN_SINCE_KOTLIN"
