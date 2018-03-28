package pw.jonak.spyfall.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent in response to [AdminAction]
 * with [AdminActionType.PRUNE_GAMES] or
 * [AdminActionType.PRUNE_USERS] set.
 * Tells the client the number of objects
 * pruned.
 */
@Serializable
class PruneOK(
        @SerialName("num_pruned") val numPruned: Int
) : StatusMessage(Companion.status) {
    companion object {
        const val status = "prune_ok"
    }
}

/**
 * Sent in response to [AdminAction] with
 * [AdminActionType.SHUTDOWN] set.
 */
@Serializable
class ServerShutdownOK : StatusMessage(Companion.status) {
    companion object {
        const val status = "server_shutdown_ok"
    }
}

/**
 * Sent by a client to administrate the server.
 */
@Serializable
class AdminAction(
        val action: AdminActionType
) : SpyfallMessage {
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName

    companion object {
        const val messageTypeName = "admin_action"
    }
}