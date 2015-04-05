package nightra.reversi.ui

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayout
import android.util.DisplayMetrics
import android.widget.{ImageView, TextView}
import nightra.reversi.app.R
import nightra.reversi.control.Game
import nightra.reversi.image.Images
import nightra.reversi.interplay._
import nightra.reversi.model.Board
import nightra.reversi.ui.game.{Bitmaps, GameUI}

import scalaz.concurrent.Future

class MainActivity extends FragmentActivity {
  var gameUI: GameUI = _
  val boardSize = 8
  val gameType = GameType(boardSize, HumanPlayer, LocalComputerPlayer(3))
  def startGame(): Unit = Future(Game.startGame(gameType, gameUI)).runAsync(_ => ())

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val grid: GridLayout = findViewById(R.id.squareGrid).asInstanceOf[GridLayout]
    val size = squareSize() / boardSize
    val bitmaps = loadBitmaps(size)

    val whiteScore = findViewById(R.id.whiteScore).asInstanceOf[TextView]
    val blackScore = findViewById(R.id.blackScore).asInstanceOf[TextView]

    val whiteScoreImage = findViewById(R.id.whiteScoreImage).asInstanceOf[ImageView]
    val blackScoreImage = findViewById(R.id.blackScoreImage).asInstanceOf[ImageView]
    whiteScoreImage.getLayoutParams.height = size
    whiteScoreImage.getLayoutParams.width = size
    blackScoreImage.getLayoutParams.height = size
    blackScoreImage.getLayoutParams.width = size
    whiteScoreImage.requestLayout()
    blackScoreImage.requestLayout()

    gameUI = new GameUI(this, bitmaps, whiteScore, blackScore, grid, boardSize, () => restart(),
      callback => runOnUiThread(new Runnable {
        override def run(): Unit = callback()
      }))

    startGame()
  }

  def restart(): Unit = {
    gameUI.boardProp() = Board.initialBoard(boardSize)
    startGame()
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
