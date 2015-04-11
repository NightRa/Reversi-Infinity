package nightra.reversi.control

import nightra.reversi.ai.ReversiAI
import nightra.reversi.ai.ReversiAI.AI
import nightra.reversi.control.Controller._
import nightra.reversi.model.{Place, Pass, Move}
import nightra.reversi.util.Log

import scalaz.concurrent.Future
import scalaz.{-\/, \/-}

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
    Log.d((System.currentTimeMillis() - start).toString)
    res
  })

  def selfReport = {
    case (player, (score, move)) =>
      playResult(Future.delay(\/- {
        val moveMessage = move match {
          case Pass => "passed"
          case Place(position) => s"moved to $position"
        }
        Log.v(s"The computer ($player) $moveMessage")
        Log.v(s"The score is: $score")
      }))
  }

  def toMove = _._2
}

object AIPlayer {
  def apply(depth: Int): AIPlayer = new AIPlayer(ReversiAI.alphaBeta(depth))
}
