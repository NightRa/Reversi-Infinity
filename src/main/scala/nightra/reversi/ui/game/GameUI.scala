package nightra.reversi.ui.game

import android.content.Context
import android.support.v7.widget.GridLayout
import nightra.reversi.control.{ExecutionError, InternalError}
import nightra.reversi.model._
import rx.core.Var

import scalaz.concurrent.Future

class GameUI(val ctx: Context, bitmaps: Bitmaps, grid: GridLayout, boardSize: Int, returnToMainMenu: () => Unit, val onUI: (() => Unit) => Unit) {
  val boardProp: Var[Board] = Var(Board.initialBoard(boardSize))

  var playCallback: Option[Position => Unit] = None

  val play: Future[Position] =
    Future.async[Position] {
      callback =>
        onUI { () =>
          playCallback = Some(callback)
        }
    }

  def reportWinner(endGame: EndGame, blacks: Int, whites: Int): Unit = {
    // TODO: Report winner
    /*println(s"The winner is: $endGame")
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("The game ended")
    alert.setHeaderText(winningMessage(endGame, blacks, whites))
    alert.setContentText("Do you want to return to the main menu?")
    alert.getButtonTypes.setAll(ButtonType.YES, ButtonType.NO)
    alert.initModality(Modality.NONE)
    val res = toOption(alert.showAndWait())
    res match {
      case Some(ButtonType.YES) => returnToMainMenu()
      case _ => ()
    }*/
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

  def reportError(error: ExecutionError): Unit = error match {
    case InternalError(e) => e.printStackTrace()
    case _ => println(s"Error: \r\n$error")
  }

  def clickSquare(pos: Position): Unit = {
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

  val game: BoardUI = new BoardUI(ctx, bitmaps, grid, boardProp, boardSize, clickSquare)
}
