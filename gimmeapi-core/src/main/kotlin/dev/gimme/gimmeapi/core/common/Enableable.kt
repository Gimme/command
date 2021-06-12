package dev.gimme.gimmeapi.core.common

/**
 * Represents something that can be enabled and disabled.
 */
interface Enableable {

    /** If this is enabled. */
    var enabled: Boolean

    /** Enables this and returns true if it was previously disabled. */
    fun enable(): Boolean = setEnabled(true)

    /** Disables this and returns true if it was previously enabled. */
    fun disable(): Boolean = setEnabled(false)

    /**
     * Attempts to enable/disable and returns if the value changed.
     *
     * Does nothing If [enabled] is the same as the current value.
     */
    private fun setEnabled(enabled: Boolean): Boolean {
        val field = this.enabled
        this.enabled = enabled

        if (field == enabled) return false

        if (enabled) onEnable() else onDisable()
        return true
    }

    /**
     * Disables then re-enables this.
     */
    fun reload() {
        disable()
        enable()
    }

    /** Performs logic when enabled. */
    fun onEnable()

    /** Performs logic when disabled. */
    fun onDisable()
}
