package dev.gimme.gimmeapi.core.data

import dev.gimme.gimmeapi.core.data.yaml.loadYamlFromFile
import java.io.File

/**
 * Handles the saving and loading of data files stored in the given [dataFolder].
 */
class DataManager(private var dataFolder: File) {

    init {
        dataFolder = File("[^a-zA-Z0-9_\\-]".toRegex().replace(dataFolder.name, ""))
        require(dataFolder.name.isNotEmpty()) { "Invalid data folder name" }
    }

    /**
     * Copies the resource at the given [resourcePath] into the data folder using the same relative path. If the file
     * already exists in the data folder and [overwrite] is false, it does not get replaced.
     */
    fun saveResource(resourcePath: String, overwrite: Boolean = false) {
        val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        File(checkNotNull(classLoader.getResource(resourcePath), { "$resourcePath resource cannot be found" }).path)
            .copyTo(File(dataFolder, resourcePath), overwrite)
    }

    /** Loads an object from a yaml file at the given [path] from the data folder. */
    fun <T> loadYaml(path: String, type: Class<T>): T? = loadYamlFromFile(getDataFile(path), type)

    /** Returns the file at the given [relativePath] from the data folder. */
    private fun getDataFile(relativePath: String): File = dataFolder.resolve(relativePath)
}
