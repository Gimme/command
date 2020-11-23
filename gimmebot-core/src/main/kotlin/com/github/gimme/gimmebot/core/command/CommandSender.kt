package com.github.gimme.gimmebot.core.command

/**
 * Represents an entity that can send commands.
 */
interface CommandSender : MessageReceiver {

    /** A medium used to send commands. */
    enum class Medium(
        /** If inputs have to start with a specific prefix to be identified as commands. */
        val requiresCommandPrefix: Boolean = false,
    ) {
        /** All inputs count as commands. */
        CONSOLE,

        /** Commands are identified by a prefix. */
        CHAT(true),
    }

    /** The medium used by this sender to send commands. */
    val medium: Medium
}
