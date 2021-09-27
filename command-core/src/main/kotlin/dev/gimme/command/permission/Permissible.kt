package dev.gimme.command.permission

/**
 * Represents an entity that may be assigned permissions.
 */
interface Permissible {
    /**
     * Returns if this entity has the [permission].
     */
    fun hasPermission(permission: String): Boolean = true

    /**
     * Returns if this entity has the [permission].
     */
    fun hasPermission(permission: Permission): Boolean = permission.permission?.let { hasPermission(it) } ?: true
}
