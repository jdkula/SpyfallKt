package pw.jonak.spyfall.backend

class GameNotFoundException(gameCode: String) : Error("The game $gameCode couldn't be found!")