package com.github.gimme.gimmebot.core.data

/**
 * Throws an [IllegalStateException] with a useful message if the given [resource] is null. Otherwise returns the not
 * null value.
 */
fun <T> requireResource(resource: T?, path: String): T = checkNotNull(resource) { "Jar does not contain $path" }
