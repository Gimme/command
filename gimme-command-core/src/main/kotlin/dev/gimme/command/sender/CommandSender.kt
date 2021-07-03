package dev.gimme.command.sender

/**
 * Represents an entity that can send commands.
 */
interface CommandSender : MessageReceiver {

    /** The display name of this command sender. */
    val name: String
}
