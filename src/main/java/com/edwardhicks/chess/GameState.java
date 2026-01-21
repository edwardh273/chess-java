package com.edwardhicks.chess;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final String[][] board;
    private boolean whiteToMove;
    private final List<Move> moveLog;

    public GameState() {

        // String Pool efficiency
        this.board = new String[][] {
            {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
            {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
            {"--", "--", "--", "--", "--", "--", "--", "--"},
            {"--", "--", "--", "--", "--", "--", "--", "--"},
            {"--", "--", "--", "--", "--", "--", "--", "--"},
            {"--", "--", "--", "--", "--", "--", "--", "--"},
            {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
            {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}
        };
        this.whiteToMove = true;
        this.moveLog = new ArrayList<>();
    }

    /**
     * Executes a move on the board.
     * @param move The move to be executed.
     */
    public void makeMove(Move move) {
        board[move.start().row()][move.start().col()] = "--";
        board[move.end().row()][move.end().col()] = move.pieceMoved();

        moveLog.add(move);
        whiteToMove = !whiteToMove; // Swap turns

        System.out.println("Move executed: " + move.pieceMoved() + " to " + move.end().col() + "," + move.end().row());
        System.out.println();

        if (whiteToMove) {
            System.out.println("White to move");
        } else {
            System.out.println("Black to move");
        }
    }

    public String[][] getBoard() {
        return board;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }
}