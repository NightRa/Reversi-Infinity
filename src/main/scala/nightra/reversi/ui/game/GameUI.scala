package nightra.reversi.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayout
import android.view.Gravity
import android.widget.TextView
import nightra.reversi.app.R
import nightra.reversi.control.{ExecutionError, InternalError}
import nightra.reversi.model._
import nightra.reversi.util.Log
import rx.core.{Obs, Rx, Var}

import scalaz.concurrent.Future

class GameUI(val ctx: FragmentActivity, bitmaps: Bitmaps, whiteScore: TextView, blackScore: TextView, grid: GridLayout, boardSize: Int, restartGame: () => Unit, val onUI: (() => Unit) => Unit) {
  val boardProp: Var[Board] = Var(Board.initialBoard(boardSize))
  val gameOver: Rx[Option[EndGame]] = Rx {
    boardProp().isGameOver
  }
  var playCallback: Option[Position => Unit] = None

  val play: Future[Position] =
    Future.async[Position] {
      callback =>
        onUI { () =>
          playCallback = Some(callback)
        }
    }

  private val score = Obs(boardProp) {
    updateScore(boardProp().whites, boardProp().blacks)
  }

  def updateScore(whites: Int, blacks: Int): Unit = {
    whiteScore.setText(whites.toString)
    blackScore.setText(blacks.toString)
  }

  def gameOver(endGame: EndGame): Unit = {
    // TODO: Report winner
    Log.i(s"The winner is: $endGame")
    reportWinner(endGame)
  }

  def reportWinner(endGame: EndGame): Unit = {
    val message: String = winningMessage(endGame, boardProp().blacks, boardProp().whites)
    val builder: AlertDialog.Builder = new AlertDialog.Builder(ctx)
    builder
      .setTitle(R.string.gameOver)
      .setMessage(message)
      .setPositiveButton(R.string.restart,
        new OnClickListener {
          override def onClick(dialog: DialogInterface, which: Int): Unit = {
            restartGame()
          }
        })
      .setNegativeButton(R.string.viewBoard,
        new OnClickListener {
          override def onClick(dialog: DialogInterface, which: Int): Unit = {}
        })
    val dialog = builder.show()
    val messageView = dialog.findViewById(android.R.id.message).asInstanceOf[TextView]
    messageView.setGravity(Gravity.CENTER)
  }

  def winningMessage(endGame: EndGame, blacks: Int, whites: Int): String = endGame match {
    case Winner(winner) =>
      val score = winner match {
        case Black => s"$blacks-$whites"
        case White => s"$whites-$blacks"
      }
      s"$winner won $score"
    case Tie => s"Tie $blacks-$whites"
  }

  def reportError(error: ExecutionError): Future[Unit] = Future.delay {
    error match {
      case InternalError(e) => e.printStackTrace()
      case _ => Log.e(s"Error: \r\n$error")
    }
  }

  def clickSquare(pos: Position): Unit = {
    gameOver() match {
      case Some(endGame) => reportWinner(endGame)
      case None =>
        playCallback match {
          case None => ()
          case Some(callback) =>
            if (boardProp().place(pos.row, pos.col).isDefined) {
              playCallback = None
              callback(pos)
            } else {
              ()
            }
        }
    }

  }

  val game: BoardUI = new BoardUI(ctx, bitmaps, grid, boardProp, boardSize, clickSquare)
}
