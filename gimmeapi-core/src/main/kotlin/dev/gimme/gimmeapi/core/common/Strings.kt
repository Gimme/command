package dev.gimme.gimmeapi.core.common

import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Converts this string from camel case to separate lowercase words (using the [locale]) separated by the specified
 * [separator].
 */
fun String.splitCamelCase(separator: String, locale: Locale = Locale.ROOT): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), separator)
        .lowercase(locale)
        .replace("$separator $separator", separator)
