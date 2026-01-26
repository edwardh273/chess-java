package com.edwardhicks.chess;

import com.edwardhicks.chess.Square;

import java.util.Objects;

public record Move(Square start, Square end, String pieceMoved, String pieceCaptured, boolean isEnpassantMove) {

    // Convenience constructor
    public Move(Square start, Square end, String[][] board) {
        this(start, end, board[start.row()][start.col()], board[end.row()][end.col()], false);
    }
    
    // Convenience constructor
    public Move(Square start, Square end, String[][] board, boolean isEnpassantMove) {
        this(start, end, board[start.row()][start.col()], board[end.row()][end.col()], isEnpassantMove);
    }

    // Computed property
    public int getMoveID() {
        return start.col() * 1000 + start.row() * 100 +
               end.col() * 10 + end.row();
    }

    public boolean isPawnPromotion() {
        return this.pieceMoved.equals("wp") && this.end.row() == 0 || this.pieceMoved.equals("bp") && this.end.row() == 7;
    }


    /**
     * Overriding equals is crucial so that validMoves.contains(userMove)
     * or userMove.equals(validMove) works correctly.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Move move = (Move) other;
        // Two moves are considered equal if they share the same start and end coordinates
        return this.getMoveID() == move.getMoveID();
    }

    /**
     * Always override hashCode when overriding equals to ensure
     * consistent behavior in Collections (like Sets or HashMaps).
     */
    @Override
    public int hashCode() {
        return Objects.hash(getMoveID());
    }

}