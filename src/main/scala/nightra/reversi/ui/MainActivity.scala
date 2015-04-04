package nightra.reversi.ui

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.GridLayout
import android.util.DisplayMetrics
import android.widget.TextView
import nightra.reversi.app.R
import nightra.reversi.control.Game
import nightra.reversi.image.Images
import nightra.reversi.interplay._
import nightra.reversi.ui.game.{Bitmaps, GameUI}

import scalaz.concurrent.Future

class MainActivity extends Activity {
  var gameUI: GameUI = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val grid: GridLayout = findViewById(R.id.squareGrid).asInstanceOf[GridLayout]
    val size = squareSize() / 8
    val bitmaps = loadBitmaps(size)

    val whiteScore = findViewById(R.id.whiteScore).asInstanceOf[TextView]
    val blackScore = findViewById(R.id.blackScore).asInstanceOf[TextView]

    gameUI = new GameUI(this, bitmaps, whiteScore, blackScore, grid, 8, () => (), callback => runOnUiThread(new Runnable {
      override def run(): Unit = callback()
    }))

    val gameType = GameType(8, HumanPlayer, LocalComputerPlayer(3))

    Future(Game.startGame(gameType, gameUI)).runAsync(_ => ())
  }

  def loadBitmaps(size: Int): Bitmaps = {
    val white = Images.loadImage(getResources, R.drawable.white, size, size)
    val whiteOpen = Images.loadImage(getResources, R.drawable.white_open, size, size)
    val black = Images.loadImage(getResources, R.drawable.black, size, size)
    val blackOpen = Images.loadImage(getResources, R.drawable.black_open, size, size)
    val empty = Images.loadImage(getResources, R.drawable.empty, size, size)

    new Bitmaps(white, whiteOpen, black, blackOpen, empty)
  }

  def squareSize(): Int = {
    val metrics: DisplayMetrics = new DisplayMetrics
    getWindowManager.getDefaultDisplay.getMetrics(metrics)
    Math.min(metrics.widthPixels, metrics.heightPixels)
  }
}
