package nightra.reversi.ui.game

import android.graphics.Bitmap
import nightra.reversi.model._

class Bitmaps(val white: Bitmap, val whiteOpen: Bitmap, val black: Bitmap, val blackOpen: Bitmap, val empty: Bitmap) {
  def stateBitmap(state: SquareState): Bitmap = state match {
    case WhiteSquareState => white
    case BlackSquareState => black
    case EmptySquareState => empty
    case OpenSquareState(White) => whiteOpen
    case OpenSquareState(Black) => blackOpen
  }
}
