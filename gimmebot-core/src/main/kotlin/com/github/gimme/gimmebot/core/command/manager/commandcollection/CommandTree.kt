package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a tree structure of commands.
 */
class CommandTree : CommandCollection {

    private val root: Node<Command<*>> = Node("", null)

    override val commands: List<Command<*>>
        get() {
            val list = mutableListOf<Command<*>>()
            root.fetchAllData(list)
            return list
        }

    override fun addCommand(command: Command<*>) {
        val words = command.name.split(" ")

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children.computeIfAbsent(word) { k -> Node(k, null) }
        }
        currentNode.data = command
    }

    override fun getCommand(name: String): Command<*>? {
        var currentNode = root

        for (word in name.split(" ")) {
            currentNode = currentNode.children[word] ?: return null
        }

        return currentNode.data
    }

    override fun containsCommand(name: String): Boolean = getCommand(name) != null

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
