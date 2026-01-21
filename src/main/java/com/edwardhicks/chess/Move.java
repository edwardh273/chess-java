package com.edwardhicks.chess;

import com.edwardhicks.chess.Square;

public record Move(Square start, Square end, String pieceMoved, String pieceCaptured) {

    // Convenience constructor
    public Move(Square start, Square end, String[][] board) {
        this(start, end, board[start.row()][start.col()], board[end.row()][end.col()]);
    }

    // Computed property
    public int getMoveID() {
        return start.col() * 1000 + start.row() * 100 +
               end.col() * 10 + end.row();
    }

}