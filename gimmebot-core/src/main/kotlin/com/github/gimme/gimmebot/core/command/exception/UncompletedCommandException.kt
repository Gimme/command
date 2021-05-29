package com.github.gimme.gimmebot.core.command.exception

import com.github.gimme.gimmebot.core.command.Command

/**
 * Thrown when the attempted command path to execute is uncompleted and has sub-paths that will lead to a real
 * command.
 */
class UncompletedCommandException(
    /**
     * The path used in the attempted execution.
     */
    val usedPath: List<String>,
    /**
     * All sub-branches that lead out from the path.
     */
    val subBranches: Set<String>,
    /**
     * All real commands that exist below the path.
     */
    val leafCommands: Set<Command<*>>
) : RuntimeException()
