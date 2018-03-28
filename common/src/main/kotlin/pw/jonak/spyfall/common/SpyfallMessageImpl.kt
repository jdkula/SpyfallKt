package pw.jonak.spyfall.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SpyfallMessageImpl(
        @SerialName("message_type") override val messageType: String,
        @SerialName("sender_side") override val senderSide: Side
) : SpyfallMessage