package nightra.reversi.control

import nightra.reversi.interplay
import nightra.reversi.interplay._
import nightra.reversi.model._
import nightra.reversi.ui.game.GameUI

import scala.annotation.tailrec
import scalaz.{-\/, \/-}

object Game {
  def startGame(gameType: GameType, gameUI: GameUI): Unit = {
    val blackPlayerRunner = playerRunner(gameType.blackPlayer, gameUI)
    val whitePlayerRunner = playerRunner(gameType.whitePlayer, gameUI)

    runGame(blackPlayerRunner, whitePlayerRunner, gameType, gameUI.boardProp(), gameUI)
  }

  @tailrec
  def runGame(blackPlayerRunner: PlayerRunner[_], whitePlayerRunner: PlayerRunner[_], gameType: GameType, board: Board, gameUI: GameUI): Unit = {
    val currentPlayer = board.turn
    board.winner match {
      case Some(endGame) =>
        gameUI.onUI { () =>
          gameUI.reportWinner(endGame, board.blacks, board.whites)
        }
      case None =>
        val playInstructions = currentPlayer match {
          case White => Controller.run(board, whitePlayerRunner)
          case Black => Controller.run(board, blackPlayerRunner)
        }
        playInstructions.run.run match {
          case -\/(executionError) => gameUI.reportError(executionError)
          case \/-(newBoard) =>
            gameUI.onUI { () =>
              gameUI.boardProp() = newBoard
            }
            runGame(blackPlayerRunner, whitePlayerRunner, gameType, newBoard, gameUI)
          // recurse and play :)
        }
    }
  }

  // Existential type, I don't care over what type the runner is, it is quantified inside already.
  def playerRunner(player: interplay.Player, gameUI: GameUI): PlayerRunner[_] = player match {
    case HumanPlayer => HumanRunner(gameUI)
    case LocalComputerPlayer(aiType) => AIPlayer(aiType)
  }

}
