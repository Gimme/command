package com.github.gimme.gimmebot.core.common.grouped

/**
 * Represents something that can be uniquely identified by its group.
 */
interface Grouped {
    /**
     * A unique identifier with a group structure separated by the [groupDelimiter].
     *
     * An example group could be: "foo.bar", where "foo" is the parent group and "bar" is added as the unique identifier
     * among elements inside that parent group, and "." as the [groupDelimiter].
     */
    val id: String

    /**
     * Returns the delimiter used in the [id] between elements in the [path].
     */
    val groupDelimiter: String get() = "."

    /**
     * Returns all individual tokens in the id split by the delimiter.
     */
    val path: List<String> get() = id.split(groupDelimiter)
}

/**
 * Returns the id with a custom [delimiter].
 */
fun Grouped.id(delimiter: String) = id.replace(groupDelimiter, delimiter)
