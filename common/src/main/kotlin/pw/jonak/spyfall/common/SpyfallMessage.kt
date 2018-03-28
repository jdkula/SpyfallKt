package pw.jonak.spyfall.common

interface SpyfallMessage {
    val messageType: String
    val senderSide: Side
}