package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.node.CommandNode

/**
 * Holds information about the result of a command search.
 *
 * If a command was found, both [command] and [commandNode] will refer to it. If just a node was found, only
 * [commandNode] will refer to it and [command] will be null.
 *
 * @property path the found path to the [command]/[commandNode], or null if nothing found
 * @property command the found command
 * @property commandNode the found commandNode
 * @property subBranches the sub-branches available under the [path]
 */
data class CommandSearchResult(
    val path: List<String>?,
    val command: Command<*>?,
    val commandNode: CommandNode?,
    val subBranches: Set<String>,
)
