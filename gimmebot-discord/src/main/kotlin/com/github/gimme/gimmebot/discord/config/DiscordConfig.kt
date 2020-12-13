package com.github.gimme.gimmebot.discord.config

data class DiscordConfig(
    /** Discord bot authentication token. */
    var token: String = "",
    /** Command prefix. */
    var prefix: String = "!",
)
