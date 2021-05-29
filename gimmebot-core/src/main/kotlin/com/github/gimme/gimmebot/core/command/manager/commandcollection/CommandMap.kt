package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandSearchResult
import com.github.gimme.gimmebot.core.command.node.CommandNode

/**
 * A collection of commands mapped by name.
 */
class CommandMap : CommandCollection {

    private val root = Node()

    override val commands: MutableSet<Command<*>> = mutableSetOf()

    override fun addCommand(command: Command<*>) {
        commands.add(command)

        addParentNodes(command)

        command.pathAliases.forEach { path ->
            computeNodeIfAbsent(path).let {
                it.command = command
                it.commandNode = command
            }
        }
    }

    private fun addParentNodes(commandNode: CommandNode) {
        commandNode.parent?.let {
            it.pathAliases.forEach { path ->
                computeNodeIfAbsent(path).commandNode = it
            }
        }
    }

    override fun getCommand(path: List<String>): Command<*>? = getNode(path)?.command

    override fun containsCommand(path: List<String>): Boolean = getCommand(path) != null

    override fun findCommand(path: List<String>): CommandSearchResult {
        var longestPathToCommand: List<String>? = null
        var command: Command<*>? = null
        var commandNode: CommandNode? = null

        val currentPath = mutableListOf<String>()
        var node: Node = root

        for (s in path) {
            node = node[s] ?: break
            currentPath.add(s)

            node.command?.let {
                longestPathToCommand = currentPath
                command = it
            }

            node.commandNode?.let {
                longestPathToCommand = currentPath
                commandNode = it
            }
        }

        return CommandSearchResult(
            path = longestPathToCommand,
            command = command,
            commandNode = commandNode,
            subBranches = node.keys,
        )
    }

    override fun getBranches(path: List<String>): Set<String> = getNode(path)?.keys ?: setOf()

    override fun getLeafCommands(path: List<String>): Set<Command<*>> =
        getNode(path)?.values?.mapNotNull { it.command }?.toSet() ?: setOf()

    override fun iterator(): Iterator<Command<*>> = commands.iterator()

    private fun getNode(path: List<String>): Node? {
        var node = root
        path.forEach { node = node[it] ?: return null }
        return node
    }

    private fun computeNodeIfAbsent(path: List<String>): Node {
        var node = root
        path.forEach { node = node.computeIfAbsent(it) { Node() } }
        return node
    }

    private class Node(
        var command: Command<*>? = null,
        var commandNode: CommandNode? = null,
    ) : LinkedHashMap<String, Node>()
}
