package pw.jonak.spyfall.common

import kotlinx.serialization.json.JSON

fun String.deserialize(): SpyfallMessage {
    val stubMessage = JSON.nonstrict.parse<SpyfallMessageImpl>(this)

    return when (stubMessage.messageType) {
        UserRegistrationRequest.messageTypeName -> JSON.parse<UserRegistrationRequest>(this)
        UserRegistrationInformation.messageTypeName -> JSON.parse<UserRegistrationInformation>(this)
        EnsureUserRegistration.messageTypeName -> JSON.parse<EnsureUserRegistration>(this)
        LobbyInformation.messageTypeName -> JSON.parse<LobbyInformation>(this)
        LobbyInformationRequest.messageTypeName -> JSON.parse<LobbyInformationRequest>(this)
        CreateGameRequest.messageTypeName -> JSON.parse<CreateGameRequest>(this)
        JoinGameRequest.messageTypeName -> JSON.parse<JoinGameRequest>(this)
        LeaveGameRequest.messageTypeName -> JSON.parse<LeaveGameRequest>(this)
        StartGameRequest.messageTypeName -> JSON.parse<StartGameRequest>(this)
        StopGameRequest.messageTypeName -> JSON.parse<StopGameRequest>(this)
        PauseGameRequest.messageTypeName -> JSON.parse<PauseGameRequest>(this)
        UnpauseGameRequest.messageTypeName -> JSON.parse<UnpauseGameRequest>(this)
        LocationListRequest.messageTypeName -> JSON.parse<LocationListRequest>(this)
        LocationListAnswer.messageTypeName -> JSON.parse<LocationListAnswer>(this)
        MessageError.messageTypeName -> {
            val error = JSON.nonstrict.parse<MessageError>(this)
            when (error.reason) {
                InvalidParameters.reason -> JSON.parse<InvalidParameters>(this)
                else -> error
            }
        }
        ActionFailure.messageTypeName -> {
            val failure = JSON.nonstrict.parse<ActionFailure>(this)
            when (failure.reason) {
                GameNotCreatedError.reason -> JSON.parse<GameNotCreatedError>(this)
                else -> failure
            }
        }
        StatusMessage.messageTypeName -> {
            val status = JSON.nonstrict.parse<StatusMessage>(this)
            when (status.status) {
                UserNotFound.status -> JSON.parse<UserNotFound>(this)
                GameNotFound.status -> JSON.parse<GameNotFound>(this)
                PruneOK.status -> JSON.parse<PruneOK>(this)
                ServerShutdownOK.status -> JSON.parse<ServerShutdownOK>(this)
                Acknowledged.status -> JSON.parse<Acknowledged>(this)
                else -> status
            }
        }
        AdminAction.messageTypeName -> JSON.parse<AdminAction>(this)
        else -> stubMessage
    }
}

fun SpyfallMessage.serialize(): String {
    return when (this.messageType) {
        UserRegistrationRequest.messageTypeName -> JSON.stringify(this as UserRegistrationRequest)
        UserRegistrationInformation.messageTypeName -> JSON.stringify(this as UserRegistrationInformation)
        EnsureUserRegistration.messageTypeName -> JSON.stringify(this as EnsureUserRegistration)
        LobbyInformation.messageTypeName -> JSON.stringify(this as LobbyInformation)
        LobbyInformationRequest.messageTypeName -> JSON.stringify(this as LobbyInformationRequest)
        CreateGameRequest.messageTypeName -> JSON.stringify(this as CreateGameRequest)
        JoinGameRequest.messageTypeName -> JSON.stringify(this as JoinGameRequest)
        LeaveGameRequest.messageTypeName -> JSON.stringify(this as LeaveGameRequest)
        StartGameRequest.messageTypeName -> JSON.stringify(this as StartGameRequest)
        StopGameRequest.messageTypeName -> JSON.stringify(this as StopGameRequest)
        PauseGameRequest.messageTypeName -> JSON.stringify(this as PauseGameRequest)
        UnpauseGameRequest.messageTypeName -> JSON.stringify(this as UnpauseGameRequest)
        LocationListRequest.messageTypeName -> JSON.stringify(this as LocationListRequest)
        LocationListAnswer.messageTypeName -> JSON.stringify(this as LocationListAnswer)
        MessageError.messageTypeName -> {
            when ((this as MessageError).reason) {
                InvalidParameters.reason -> JSON.stringify(this as InvalidParameters)
                else -> JSON.stringify(this)
            }
        }
        ActionFailure.messageTypeName -> {
            when ((this as ActionFailure).reason) {
                GameNotCreatedError.reason -> JSON.stringify(this as GameNotCreatedError)
                else -> JSON.stringify(this)
            }
        }
        StatusMessage.messageTypeName -> {
            when ((this as StatusMessage).status) {
                UserNotFound.status -> JSON.stringify(this as UserNotFound)
                GameNotFound.status -> JSON.stringify(this as GameNotFound)
                PruneOK.status -> JSON.stringify(this as PruneOK)
                ServerShutdownOK.status -> JSON.stringify(this as ServerShutdownOK)
                Acknowledged.status -> JSON.stringify(this as Acknowledged)
                else -> JSON.stringify(this)
            }
        }
        AdminAction.messageTypeName -> JSON.stringify(this as AdminAction)
        else -> JSON.stringify(SpyfallMessageImpl(this.messageType, this.senderSide))
    }
}