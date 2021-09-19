package dev.gimme.command.common

import java.util.*

/**
 * Converts this string from camel case to separate lowercase words (using the [locale]) separated by the specified
 * [separator].
 *
 * Also splits on spaces.
 */
fun String.splitCamelCase(separator: String, locale: Locale = Locale.ROOT): String =
    split(Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])| "))
        .joinToString(separator) { it.lowercase(locale) }
        .replace("$separator$separator", separator)
