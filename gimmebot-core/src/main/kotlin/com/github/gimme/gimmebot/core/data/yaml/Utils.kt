package com.github.gimme.gimmebot.core.data.yaml

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream

private val yaml = Yaml()

/** Loads an object with the specified [type] from the given yaml [file]. Returns null if failed to load. */
fun <T> loadYamlFromFile(file: File, type: Class<T>): T? =
    yaml.loadAs(file.bufferedReader(), type)

/**
 * Loads an object with the specified [type] from the resource at the given [resourcePath] using the given
 * [classLoader]. Returns null if failed to load.
 */
fun <T> loadYamlFromResource(
    resourcePath: String,
    type: Class<T>,
    classLoader: ClassLoader = type.classLoader,
): T? {
    val inputStream = getOptionalResourceAsStream(resourcePath, classLoader) ?: return null
    return yaml.loadAs(inputStream, type)
}

/**
 * Returns an input stream for the resource at the specified [resourcePath] from the given [classLoader], or null if the
 * resource could not be found.
 */
private fun getOptionalResourceAsStream(resourcePath: String, classLoader: ClassLoader): InputStream? {
    return classLoader.getResourceAsStream(resourcePath)
}
