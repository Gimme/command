package dev.gimme.gimmeapi.discord.config

/**
 * Discord bot configuration.
 */
data class DiscordConfig(

    /** Discord bot authentication token. */
    var token: String = "",
    /** Command prefix. */
    var prefix: String = "!",
)
