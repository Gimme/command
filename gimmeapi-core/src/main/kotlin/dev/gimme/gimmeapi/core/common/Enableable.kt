package dev.gimme.gimmeapi.core.common

/**
 * Represents something that can be enabled and disabled.
 */
interface Enableable {

    /** If this is enabled. */
    var enabled: Boolean

    /** Enables this. */
    fun enable() {
        enabled = true
    }

    /** Disables this. */
    fun disable() {
        enabled = false
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

    companion object {
        /**
         * Attempts to enable/disable, based on [enabled], the given [enableable] and returns the new value.
         *
         * Does nothing If [enabled] is the same as the [enableable]'s current value.
         */
        fun enable(enableable: Enableable, enabled: Boolean): Boolean {
            val field = enableable.enabled

            if (field == enabled) return field

            if (enabled) enableable.onEnable() else enableable.onDisable()
            return enabled
        }
    }
}
