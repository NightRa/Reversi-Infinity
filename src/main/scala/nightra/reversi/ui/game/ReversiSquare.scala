package nightra.reversi.ui.game

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import nightra.reversi.model._
import rx.core.{Obs, Rx}

class ReversiSquare(ctx: Context,
                    bitmaps: Bitmaps,
                    onClickHandler: () => Unit,
                    val state: Rx[SquareState])
  extends ImageView(ctx) {

  setOnClickListener(new OnClickListener {
    override def onClick(v: View): Unit = onClickHandler()
  })

  private val o = Obs(state){
    updatePicture(state())
  }

  private def updatePicture(st: SquareState): Unit = {
    setImageBitmap(bitmaps.stateBitmap(st))
  }
}

