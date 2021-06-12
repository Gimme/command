package dev.gimme.gimmeapi.command.manager

/**
 * A command manager with text response type.
 */
class TextCommandManager : SimpleCommandManager<String?>({
    if (it is Unit) null else it?.toString()
})
