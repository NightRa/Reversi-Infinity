package nightra.reversi.ai

import nightra.reversi.ai.imperative.AlphaBeta
import nightra.reversi.model.{Board, Move}

object ReversiAI {
  // board => value, move, new state
  type AI = Board => (Float, Option[(Move, Board)])

  def alphaBeta(depth: Int): AI = board => AlphaBeta.reversiAlphaBeta(board,depth)
}
