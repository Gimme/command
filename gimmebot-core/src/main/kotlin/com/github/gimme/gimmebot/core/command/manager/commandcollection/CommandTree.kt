package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a tree structure of commands.
 */
class CommandTree : CommandCollection {

    private val delimiter = "."
    private val root: Node<Command<*>> = Node("", null)

    override fun addCommand(command: Command<*>) {
        val words = command.group.split(delimiter)

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children.computeIfAbsent(word.toLowerCase()) { k -> Node(k, null) }
        }
        currentNode.data = command
    }

    override fun getCommand(name: String): Command<*>? {
        var currentNode = root

        for (word in name.toLowerCase().split(delimiter)) {
            currentNode = currentNode.children[word] ?: return null
        }

        return currentNode.data
    }

    override fun getCommands(): List<Command<*>> {
        val list = mutableListOf<Command<*>>()
        root.fetchAllData(list)
        return list
    }

    override fun findCommand(path: List<String>): Command<*>? {
        var lastFound: Command<*>? = null

        var currentNode = root
        for (word in path) {
            currentNode = currentNode.children[word.toLowerCase()] ?: break
            lastFound = currentNode.data
        }
        return lastFound
    }

    private data class Node<T>(
        val name: String,
        var data: T?,
    ) {
        val children: MutableMap<String, Node<T>> = mutableMapOf()

        fun fetchAllData(list: MutableList<T>) {
            data?.let { list.add(it) }
            children.values.forEach { child -> child.fetchAllData(list) }
        }
    }
}
