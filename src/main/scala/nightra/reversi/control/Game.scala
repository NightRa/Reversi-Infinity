package nightra.reversi.control

import nightra.reversi.interplay
import nightra.reversi.interplay._
import nightra.reversi.model._
import nightra.reversi.ui.game.GameUI

import scalaz.concurrent.Future
import scalaz.{-\/, \/, \/-}

object Game {
  def startGame(gameType: GameType, gameUI: GameUI): Future[Unit] = {
    val blackPlayerRunner = playerRunner(gameType.blackPlayer, gameUI)
    val whitePlayerRunner = playerRunner(gameType.whitePlayer, gameUI)
    runGame(blackPlayerRunner, whitePlayerRunner, gameType, gameUI.boardProp(), gameUI)
  }

  def runGame(blackPlayerRunner: PlayerRunner[_], whitePlayerRunner: PlayerRunner[_], gameType: GameType, board: Board, gameUI: GameUI): Future[Unit] = {
    val currentPlayer = board.turn
    board.isGameOver match {
      case Some(endGame) =>

        Future.delay {
          gameUI.onUI { () =>
            gameUI.gameOver(endGame)
          }
        }
      case None =>
        val playInstructions: Future[\/[ExecutionError, Board]] = (currentPlayer match {
          case White => Controller.run(board, whitePlayerRunner)
          case Black => Controller.run(board, blackPlayerRunner)
        }).run

        playInstructions.flatMap {
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
