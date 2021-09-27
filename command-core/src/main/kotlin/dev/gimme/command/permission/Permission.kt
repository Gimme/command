package dev.gimme.command.permission

/**
 * Represents an entity that can require a permission.
 */
interface Permission {
    /**
     * The required permission string, or null if no required permission.
     */
    val permission: String?
}
