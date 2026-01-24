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

        if (move.isPawnPromotion()) {
            board[move.end().row()][move.end().col()] = "" + move.pieceMoved().charAt(0) + 'Q';
        }

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


    /**
     * Returns all moves considering checks (the 'legal' moves).
     */
    public ArrayList<Move> getValidMoves() {
        System.out.println("Getting all valid moves");
        return getAllPossibleMoves(); // Simplified for now
    }

    /**
     * Returns all possible moves without considering if the King is in check.
     */
    public ArrayList<Move> getAllPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                char color = piece.charAt(0);

                if ((color == 'w' && whiteToMove) || (color == 'b' && !whiteToMove)) {
                    char pieceType = piece.charAt(1);
                    if (pieceType == 'p') {
                        getPawnMoves(r, c, moves);
                    }
                    // Add other piece types here later
                }
            }
        }
        return moves;
    }

    public void getPawnMoves(int r, int c, List<Move> moves) {
        if (whiteToMove) { // White pawn logic
            // Forward moves
            if (board[r - 1][c].equals("--")) {
                moves.add(new Move(new Square(c, r), new Square(c, r - 1), board));
                if (r == 6 && board[r - 2][c].equals("--")) {
                    moves.add(new Move(new Square(c, r), new Square(c, r - 2), board));
                }
            }
            // Captures
            if (c - 1 >= 0 && board[r - 1][c - 1].charAt(0) == 'b') {
                moves.add(new Move(new Square(c, r), new Square(c - 1, r - 1), board));
            }
            if (c + 1 <= 7 && board[r - 1][c + 1].charAt(0) == 'b') {
                moves.add(new Move(new Square(c, r), new Square(c + 1, r - 1), board));
            }
        } else { // Black pawn logic
            // Forward moves
            if (board[r + 1][c].equals("--")) {
                moves.add(new Move(new Square(c, r), new Square(c, r + 1), board));
                if (r == 1 && board[r + 2][c].equals("--")) {
                    moves.add(new Move(new Square(c, r), new Square(c, r + 2), board));
                }
            }
            // Captures
            if (c - 1 >= 0 && board[r + 1][c - 1].charAt(0) == 'w') {
                moves.add(new Move(new Square(c, r), new Square(c - 1, r + 1), board));
            }
            if (c + 1 <= 7 && board[r + 1][c + 1].charAt(0) == 'w') {
                moves.add(new Move(new Square(c, r), new Square(c + 1, r + 1), board));
            }
        }
    }

    public String[][] getBoard() {
        return board;
    }
}