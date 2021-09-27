package dev.gimme.command.sender

import dev.gimme.command.permission.Permissible

/**
 * Represents an entity that can send commands.
 */
interface CommandSender : MessageReceiver, Permissible {

    /** The display name of this command sender. */
    val name: String
}
