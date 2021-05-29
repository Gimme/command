package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.node.CommandNode

/**
 * Holds information about the result of a command search.
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
