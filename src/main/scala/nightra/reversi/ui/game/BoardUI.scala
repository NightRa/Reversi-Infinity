package nightra.reversi.ui.game

import android.content.Context
import android.support.v7.widget.GridLayout
import android.view.View
import nightra.reversi.model._
import rx.core.Rx

class BoardUI(ctx: Context, bitmaps: Bitmaps, grid: GridLayout, boardProp: Rx[Board], boardSize: Int, squareClicked: Position => Unit) {
  val tiles: Rx[Array[Array[SquareState]]] = Rx {
    boardProp().mobilityBoard
  }

  for {
    i <- 0 until boardSize
    j <- 0 until boardSize
  } {
    val pos: Position = Position(i, j)
    val square = new ReversiSquare(
      ctx, bitmaps,
      () => squareClicked(pos),
      // () => showLookahead(pos),
      Rx {
        tiles()(i)(j)
      }
    )
    BoardUI.addAt(grid, square, i, j)
  }

  def showLookahead(pos: Position): Unit = {
    // tiles() = Board.lookaheadBoard(boardProp(), pos)
  }

}

object BoardUI {
  def addAt(grid: GridLayout, v: View, i: Int, j: Int): Unit = {
    val params = new GridLayout.LayoutParams(GridLayout.spec(i), GridLayout.spec(j))
    v.setLayoutParams(params)
    grid.addView(v)
  }
}