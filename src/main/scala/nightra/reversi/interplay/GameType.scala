package nightra.reversi.interplay

// Int x PlayerType x PlayerType
/*size >= 4 && size is even.*/
case class GameType(boardSize: Int, blackPlayer: Player, whitePlayer: Player)

sealed trait Player
case object HumanPlayer extends Player
case class LocalComputerPlayer(depth: Int) extends Player

// 2 + AIType
sealed trait PlayerType
case object Human extends PlayerType {
  override def toString = "Human"
}
case object LocalComputer extends PlayerType {
  override def toString = "Local computer AI"
}