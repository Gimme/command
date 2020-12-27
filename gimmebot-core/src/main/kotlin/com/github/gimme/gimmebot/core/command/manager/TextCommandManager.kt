package com.github.gimme.gimmebot.core.command.manager

/**
 * A command manager with text response type.
 */
class TextCommandManager : SimpleCommandManager<String?>({ it?.toString() })
