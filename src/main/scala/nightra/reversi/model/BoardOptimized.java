package nightra.reversi.model;

import scala.Option;
import scala.Some;
import scala.Tuple2;

import java.util.ArrayList;

public class BoardOptimized {
    public static Piece[][] applyFlips(int size, Piece[][] board, Position[] flipped, Piece center) {
        Piece[][] newBoard = new Piece[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, size);
        }
        for (Position pos : flipped) {
            newBoard[pos.row()][pos.col()] = center;
        }
        return newBoard;
    }

    // This is one of the main hotspots, so I optimized it as much as I could
    // Given a position, returns a list of flipped positions if it was to be placed,
    public static Position[] flippedIfPlaced(int size, Piece[][] board, int row, int col, Player turn) {
        ArrayList<Position> acc = new ArrayList<>();
        Player oppositePlayer = turn.opposite();
        Piece oppositePiece = oppositePlayer.piece();
        /*to avoid calls to inBounds()*/
        int rowFrom = row == 0 ? 0 : -1;
        int rowTo = row == size - 1 ? 0 : +1;
        int colFrom = col == 0 ? 0 : -1;
        int colTo = col == size - 1 ? 0 : +1;
        /*Android probably does loop unrolling. manual loop unrolling didn't improve.*/
        for (int drow = rowFrom; drow <= rowTo; drow++) {
            for (int dcol = colFrom; dcol <= colTo; dcol++) {
                if (!(drow == 0 && dcol == 0)) {
                    int row2 = row + drow;
                    int col2 = col + dcol;
                    if (board[row2][col2] == oppositePiece) {
                        Option<ArrayList<Position>> directedRow = terminatedPath(size, board, drow, dcol, row2, col2, oppositePlayer);
                        if (directedRow.isDefined()) {
                            acc.addAll(directedRow.get());
                        }
                    }
                }
            }
        }
        if (!acc.isEmpty()) {
            acc.add(new Position(row, col));
        }
        Position[] out = new Position[acc.size()];
        return acc.toArray(out);
    }

    public static Option<ArrayList<Position>> terminatedPath(int size, Piece[][] board, int drow, int dcol, int row, int col, Player startColor) {
        ArrayList<Position> acc = new ArrayList<>(4);
        while (true) {
            if (!inBounds(size, row, col)) {
                return Board.none();
            } else {
                Piece piece = board[row][col];
                if (piece == EmptyPiece$.MODULE$) {
                    return Board.none();
                } else {
                    Player player = piece.unsafePlayer();
                    if (player == startColor) {
                        acc.add(Position.apply(row, col));
                        row += drow;
                        col += dcol;
                    } else {
                        return Some.apply(acc);
                    }
                }
            }
        }

    }

    public static boolean inBounds(int size, int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public static Tuple2<Move, Board>[] possibleMoves(Board board) {
        ArrayList<Tuple2<Move, Board>> possibleMovesAcc = new ArrayList<>();
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.size(); col++) {
                Option<Board> placement = board.place(row, col);
                if (placement.isDefined()) {
                    possibleMovesAcc.add(new Tuple2<>(new Place(new Position(row, col)), placement.get()));
                }
            }
        }
        if (!board.stale() && possibleMovesAcc.isEmpty()) {
            @SuppressWarnings("unchecked")
            Tuple2<Move, Board>[] passArray = new Tuple2[1];
            passArray[0] = new Tuple2<>(Pass$.MODULE$, board.passTurn());
            return passArray;
        } else {
            @SuppressWarnings("unchecked")
            Tuple2<Move, Board>[] out = new Tuple2[possibleMovesAcc.size()];
            return possibleMovesAcc.toArray(out);
        }
    }

}
