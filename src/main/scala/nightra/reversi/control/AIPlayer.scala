package nightra.reversi.control

import android.util.Log
import nightra.reversi.ai.ReversiAI
import nightra.reversi.ai.ReversiAI.AI
import nightra.reversi.control.Controller._
import nightra.reversi.interplay._
import nightra.reversi.model.{Move, Board}

import scalaz.{\/-, -\/}
import scalaz.concurrent.Future

class AIPlayer(ai: AI) extends PlayerRunner[(Float, Move)] {
  def play = player => board => playResult(Future {
    // TODO: Remove time measuring debug
    val start = System.currentTimeMillis()
    val res = ai(board) match {
      case (score, None) =>
        -\/(AIError(new IllegalStateException("No move for the AI.")))
      case (score, Some((move, newBoard))) =>
        \/-(score, move)
    }
    Log.d("Reversi", String.valueOf(System.currentTimeMillis() - start))
    res
  })

  def selfReport = {
    case (player, (score, move)) =>
      playResult(Future.delay(\/- {
        println(s"The computer ($player) moved to $move")
        println(s"The score is: $score")
      }))
  }

  def toMove = _._2
}

object AIPlayer {
  def apply(depth: Int): AIPlayer = new AIPlayer(ReversiAI.alphaBeta(depth))
}
