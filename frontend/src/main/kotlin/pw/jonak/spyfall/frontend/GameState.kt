package pw.jonak.spyfall.frontend

import pw.jonak.spyfall.common.LobbyInformation
import pw.jonak.spyfall.common.UserRegistrationInformation

/**
 * Represents the state of Spyfall as a whole. This is immutable, so that
 * Observable delegates can be used with each update.
 * @property userInfo Stores the current [UserRegistrationInformation] for this session.
 * @property state The current [ApplicationState] that the webapp should display.
 * @property currentLobby The current lobby the user has joined.
 * @property locationList The current cached list of locations.
 */
internal data class GameState(
        val userInfo: UserRegistrationInformation,
        val state: ApplicationState,
        val currentLobby: LobbyInformation? = null,
        val locationList: List<String>? = null
) {
    /**
     * Returns a version of this [GameState] with [state] modified to [newState]
     */
    internal fun changeState(newState: ApplicationState): GameState =
        GameState(userInfo, newState, currentLobby, locationList)

    /**
     * Returns a version of this [GameState] with [userInfo] modified to [newUserInfo]
     */
    internal fun changeUserInfo(newUserInfo: UserRegistrationInformation): GameState =
        GameState(newUserInfo, state, currentLobby, locationList)

    /**
     * Returns a version of this [GameState] with [currentLobby] modified to [newLobby]
     */
    internal fun changeLobby(newLobby: LobbyInformation?): GameState =
        GameState(userInfo, state, newLobby, locationList)

    /**
     * Returns a version of this [GameState] with [locationList] modified to [newLocations]
     */
    internal fun changeLocations(newLocations: List<String>?): GameState =
        GameState(userInfo, state, currentLobby, newLocations)
}
