package nightra.reversi.control

import nightra.reversi.ai.ReversiAI
import nightra.reversi.ai.ReversiAI.AI
import nightra.reversi.control.Controller._
import nightra.reversi.model.{Place, Pass, Move}
import nightra.reversi.util.Log

import scalaz.concurrent.Future
import scalaz.{-\/, \/-}

class AIPlayer(ai: AI) extends PlayerRunner[(Long, Float, Move)] {
  def play = player => board => playResult(Future {
    // TODO: Remove time measuring debug
    val start = System.currentTimeMillis()
    val res = ai(board) match {
      case (score, None) =>
        -\/(AIError(new IllegalStateException("No move for the AI.")))
      case (score, Some((move, newBoard))) =>
        \/-(score, move)
    }

    val time = System.currentTimeMillis() - start
    res.map(result => (time, result._1, result._2))
  })

  def selfReport = {
    case (player, (time, score, move)) =>
      playResult(Future.delay(\/- {
        val moveMessage = move match {
          case Pass => "passed"
          case Place(position) => s"moved to $position"
        }
        Log.d(time.toString)
        Log.v(s"The computer ($player) $moveMessage")
        Log.v(s"The score is: $score")
      }))
  }

  def toMove = _._3
}

object AIPlayer {
  def apply(depth: Int): AIPlayer = new AIPlayer(ReversiAI.alphaBeta(depth))
}
