package pw.jonak.spyfall.frontend

/**
 * Enumerates the states the application can be in:
 *
 * [LOGIN] -> The login screen (asks for name)
 * [MAINMENU] -> The screen where one can join or create a game
 * [JOIN] -> The screen where one enters in a game code
 * [LOBBY] -> The screen that shows the current players, game code, and from where you can start/leave the game.
 * [GAME] -> The screen that shows when the game is running.
 * [ADMINMENU] -> TODO remove this.
 */
enum class ApplicationState {
    LOGIN,
    MAINMENU,
    JOIN,
    LOBBY,
    GAME,
    ADMINMENU
}