package nightra.reversi.model

import nightra.reversi.ai.Heuristic
import nightra.reversi.model.BoardOptimized._
import nightra.reversi.util.Collections._

case class Board private[model](mat: Array[Array[Piece]], size: Int, blacks: Int, pieces: Int, stale: Boolean, turn: Player) {
  def whites = pieces - blacks

  def at(pos: Position): Option[Piece] =
    if (!pos.inBounds(size))
      None
    else
      Some(mat(pos.row)(pos.col))

  // Proof obligation: inBounds(pos)
  @inline
  def unsafeAt(pos: Position): Piece = mat(pos.row)(pos.col)
  //                                   ^ unsafe

  def setTurn(player: Player) = this.copy(turn = player)

  def passTurn: Board = setTurn(turn.opposite).copy(stale = true)

  // If valid move, return the board after placement and the flipped positions.
  // Otherwise, none.
  def place(row: Int, col: Int): Option[Board] =
    if (!inBounds(size, row, col) || mat(row)(col).isPlayer /*Out of bounds, or player*/ ) {
      None
    } else {
      val flipped = flippedIfPlaced(size, mat, row, col, turn)
      if (flipped.length == 0) {
        // No pieces flipped => Invalid move.
        None
      } else {
        val flippedMat = applyFlips(size, mat, flipped, turn.piece)

        val newBlacks = turn match {
          case Black => blacks + flipped.length // flipped includes the newly placed piece.
          case White => blacks - flipped.length + 1 // placed flippedSize white pieces, (whites - 1) black flipped.
        }
        Some(Board(flippedMat, size, newBlacks, pieces + 1, stale = false, turn.opposite))
      }
    }

  def move(move: Move): Option[Board] = move match {
    case Pass =>
      if (canPass) Some(passTurn)
      else None
    case Place(pos) => place(pos.row, pos.col)
  }

  // returns a lazy Stream of: the taken move, and the new board.
  lazy val possibleMoves: Array[(Move, Board)] = BoardOptimized.possibleMoves(this)
  /*{
    val openPositions = positions(size).collect(Function.unlift(pos => place(pos.row, pos.col).map(board => (Place(pos): Move, board))))

    if (!stale && openPositions.isEmpty) Array((Pass, passTurn))
    else openPositions
  }*/

  lazy val possibleMovesSize = possibleMoves.length

  lazy val canPass = !stale && (possibleMoves match {
    case Array((Pass, _)) => true
    case _ => false
  })


  lazy val isTerminal: Boolean = {
    pieces == size * size || possibleMovesSize == 0
  }


  def mobilityBoard: Array[Array[SquareState]] = {
    val open = possibleMoves.collect { case (Place(pos), board) => pos }.toSet
    Array.tabulate(size, size)((row, col) => if (open(Position(row, col))) OpenSquareState(turn) else mat(row)(col).squareState)
  }

  lazy val (blackOpen, whiteOpen) = turn match {
    case Black => (possibleMovesSize, setTurn(White).possibleMovesSize)
    case White => (setTurn(Black).possibleMovesSize, possibleMovesSize)
  }

  lazy val heuristic = Heuristic.heuristic(this)

  def isGameOver: Option[EndGame] =
    if (!isTerminal) None
    else {
      if (blacks > whites)
        Some(Winner(Black))
      else if (whites > blacks)
        Some(Winner(White))
      else
        Some(Tie)
    }


  // To be honest, I don't really like that piece of code.
  // Research needs to be done with sub sum types.
  // Also, local sum types would be nice. (as well as 'named' tuples)
  // row polymorphic sum types? - there are already row polymorphic product types. (in some languages)
  // Pattern matches reap down options from the sum type.
  override def toString: String =
    s"size: $size, blacks: $blacks, whites: $whites, pieces: $pieces, turn: $turn" +
      mat.map(_.mkString).mkString("\r\n", "\r\n", "\r\n")
}

object Board {
  def validBoardSize(size: Int): Boolean = size >= 4 && size % 2 == 0

  def initialBoard(size: Int) = {
    require(validBoardSize(size), s"initialBoard: A board size should be >= 4 and even, size: $size")
    val left = size / 2 - 1
    val right = size / 2
    val top = size / 2 - 1
    val bottom = size / 2

    Board(Array.tabulate(size, size) {
      (row, col) =>
        if ((row == top && col == left) || (row == bottom && col == right))
          WhitePiece
        else if ((row == top && col == right) || (row == bottom && col == left))
          BlackPiece
        else
          EmptyPiece
    }, size, 2, 4, stale = false, Black)
  }

  def lookaheadBoard(board: Board, pos: Position): Array[Array[SquareState]] = {
    val tiles = board.mobilityBoard
    board.place(pos.row, pos.col) match {
      case None => tiles
      case Some(newBoard) =>
        Array.tabulate(board.size, board.size) {
          (row, col) =>
            if (row == pos.row && col == pos.col) board.turn.piece.squareState
            else if (tiles(row)(col).isOpen) tiles(row)(col)
            else newBoard.mat(row)(col).squareState
        }
    }
  }

  //p in positions(size) => inBounds(size)
  def positions(size: Int): Array[Position] =
  /*allocation*/ Array.tabulate(size * size)(i => (Position.apply _).tupled(to2D(size, i)))

  def extractMove(before: Board, after: Board): Option[Move] = {
    // I really don't like placing preconditions.
    // I really want Dependant types please!!!! -- No one is really stopping me from using a dependently typed language..
    require(before.size == after.size, "placedPosition requires boards of equal sizes.")
    if (before == after) None
    else {
      val posMaybe = positions(before.size).find(p => before.unsafeAt(p).isEmpty && !after.unsafeAt(p).isEmpty)
      //                                                    ^ p in positions(size) => inBounds(size) => safe
      Some(posMaybe.fold[Move](Pass)(Place))
    }
  }

  // Can't determine the turn because of passes.
  // stale depends on how one got to this board.
  def fromMatrix(mat: Array[Array[Piece]], turn: Player, stale: Boolean): Board = {
    val rowCount = mat.length
    val validMatrix = validBoardSize(rowCount) && mat.forall(row => row.length == rowCount)
    require(validMatrix, "fromMatrix requires a square matrix with a valid board size; >= 4 and even.")

    val boardSize = mat.length
    val blacks = mat.map(_.count(_ == BlackPiece)).sum
    val pieces = mat.map(_.count(!_.isEmpty)).sum
    Board(mat, boardSize, blacks, pieces, stale, turn)
  }

  def none[A]: Option[A] = None
}
