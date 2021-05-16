package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * A collection of commands mapped by name.
 */
class CommandMap : CommandCollection {

    private val root = Node()

    override val commands: MutableSet<Command<*>> = mutableSetOf()

    override fun addCommand(command: Command<*>) {
        commands.add(command)

        command.pathAliases.forEach { path ->
            computeNodeIfAbsent(path).command = command
        }
    }

    override fun getCommand(path: List<String>): Command<*>? = getNode(path)?.command

    override fun containsCommand(path: List<String>): Boolean = getCommand(path) != null

    override fun findCommand(path: List<String>): List<String>? {
        var longestPathToCommand: List<String>? = null

        val currentPath = mutableListOf<String>()
        var node: Node = root

        for (s in path) {
            node = node[s] ?: break
            currentPath.add(s)
            if (node.command != null) longestPathToCommand = currentPath
        }

        return longestPathToCommand
    }

    override fun getBranches(path: List<String>): Set<String> = getNode(path)?.keys ?: setOf()

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

    private class Node(var command: Command<*>? = null) : LinkedHashMap<String, Node>()
}
