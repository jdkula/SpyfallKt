package pw.jonak.spyfall.common

/**
 * Describe the type of action an
 * [AdminAction] will have on the server.
 */
enum class AdminActionType {
    /** Shuts down the server. */
    SHUTDOWN,

    /** Prunes games on the server. */
    PRUNE_GAMES,

    /** Prunes users on the server. */
    PRUNE_USERS
}