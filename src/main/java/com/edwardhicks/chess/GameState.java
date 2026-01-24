package com.edwardhicks.chess;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final String[][] board;
    private boolean whiteToMove;
    private final List<Move> moveLog;
    private Square whiteKingLocation;
    private Square blackKingLocation;

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
        this.whiteKingLocation = new Square(4, 7);  // (col, row)
        this.blackKingLocation = new Square(4, 0);  // (col, row)

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
                    switch (pieceType) {
                        case 'p' -> getPawnMoves(r, c, moves);
                        case 'R' -> getRookMoves(r, c, moves);
                        case 'N' -> getKnightMoves(r, c, moves);
                        case 'B' -> getBishopMoves(r, c, moves);
                        case 'Q' -> getQueenMoves(r, c, moves);
                        case 'K' -> getKingMoves(r, c, moves);
                    }
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

    /**
 * Get all Rook moves for the Rook located at row, col and add these moves to the list
 */
public void getRookMoves(int r, int c, List<Move> moves) {
    char enemyColor = whiteToMove ? 'b' : 'w';
    int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // left, right, up, down

    for (int[] d : directions) {
        for (int i = 1; i < 8; i++) {
            int endRow = r + d[0] * i;
            int endCol = c + d[1] * i;

            if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
                String endPiece = board[endRow][endCol];

                if (endPiece.equals("--")) { // if blank, append move
                    moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                } else if (endPiece.charAt(0) == enemyColor) { // hits enemy piece
                    moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                    break;
                } else { // hits own color piece
                    break;
                }
            } else { // off board
                break;
            }
        }
    }
}

/**
 * Get all Bishop moves for the Bishop located at row, col and add these moves to the list
 */
public void getBishopMoves(int r, int c, List<Move> moves) {
    char enemyColor = whiteToMove ? 'b' : 'w';
    int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}; // leftup, rightup, leftdown, rightdown

    for (int[] d : directions) {
        for (int i = 1; i < 8; i++) {
            int endRow = r + d[0] * i;
            int endCol = c + d[1] * i;

            if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
                String endPiece = board[endRow][endCol];

                if (endPiece.equals("--")) { // if blank, append move
                    moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                } else if (endPiece.charAt(0) == enemyColor) { // hits enemy piece
                    moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                    break;
                } else { // hits own color piece
                    break;
                }
            } else { // off board
                break;
            }
        }
    }
}

/**
 * Get all Queen moves for the Queen located at row, col and add these moves to the list
 */
public void getQueenMoves(int r, int c, List<Move> moves) {
    getBishopMoves(r, c, moves);
    getRookMoves(r, c, moves);
}

/**
 * Get all Knight moves for the Knight located at row, col and add these moves to the list
 */
public void getKnightMoves(int r, int c, List<Move> moves) {
    int[][] potentialMoves = {{-1, -2}, {-2, -1}, {-2, 1}, {-1, 2},
                               {1, 2}, {2, 1}, {2, -1}, {1, -2}};
    char allyColor = whiteToMove ? 'w' : 'b';

    for (int[] m : potentialMoves) {
        int endRow = r + m[0];
        int endCol = c + m[1];

        if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
            String endPiece = board[endRow][endCol];

            if (endPiece.charAt(0) != allyColor) {
                moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
            }
        }
    }
}

/**
 * Get all King moves for the King located at row, col and add these moves to the list
 */
public void getKingMoves(int r, int c, List<Move> moves) {
    int[][] potentialMoves = {{-1, -1}, {-1, 0}, {-1, 1}, {0, 1},
                               {1, 1}, {1, 0}, {1, -1}, {0, -1}};
    char allyColor = whiteToMove ? 'w' : 'b';

    for (int[] m : potentialMoves) {
        int endRow = r + m[0];
        int endCol = c + m[1];

        if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
            String endPiece = board[endRow][endCol];

            if (endPiece.charAt(0) != allyColor) {
                moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
            }
        }
    }
}

    public String[][] getBoard() {
        return board;
    }
}