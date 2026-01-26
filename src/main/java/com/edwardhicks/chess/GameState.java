package com.edwardhicks.chess;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.abs;

public class GameState {
    private final String[][] board;
    public boolean whiteToMove;
    private final List<Move> moveLog;
    private final List<CastleRights> castleRightsLog;
    private Square whiteKingLocation;
    private Square blackKingLocation;
    private Square enpassantPossible;
    private final CastleRights currentCastleRights;

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
        this.enpassantPossible = null;

        this.moveLog = new ArrayList<Move>();

        this.currentCastleRights = new CastleRights(true, true, true, true);
        this.castleRightsLog = new ArrayList<CastleRights>(Collections.singleton(this.currentCastleRights));
    }

    /**
     * Executes a move on the board.
     * @param move The move to be executed.
     */
    public void makeMove(Move move) {
        board[move.start().row()][move.start().col()] = "--";
        board[move.end().row()][move.end().col()] = move.pieceMoved();
        moveLog.add(move);

        // update king's location
        if (move.pieceMoved().equals("wK")) {
            this.whiteKingLocation = new Square(move.end().col(), move.end().row());
        } else if (move.pieceMoved().equals("bK")) {
            this.blackKingLocation = new Square(move.end().col(), move.end().row());
        }

        if (move.isPawnPromotion()) {
            board[move.end().row()][move.end().col()] = "" + move.pieceMoved().charAt(0) + 'Q';
        }

        if (move.pieceMoved().charAt(1) == 'p' && abs(move.start().row() - move.end().row()) == 2) {  // if a pawn moves 2 squares
            this.enpassantPossible = new Square(move.start().col(), (move.start().row() + move.end().row()) / 2 );  // enpassant possible to the square where the pawn would have moved if it had only moved 1 square.
            System.out.println("enpassant possible: " + this.enpassantPossible);
        }

        if (move.isEnpassantMove()) {
            board[move.start().row()][move.end().col()] = "--";
        }

        if (move.isCastleMove()) {
            if (move.end().col() - move.start().col() == 2) { // kingside
                board[move.end().row()][move.end().col() - 1] = board[move.end().row()][move.end().col() + 1];  // copy the rook to the new square
                board[move.end().row()][move.end().col() + 1] = "--";  // remove the old rook
            } else
            if (move.end().col() - move.start().col() == -2) {  // queenside
                board[move.end().row()][move.end().col() + 1] = board[move.end().row()][move.end().col() - 2];  // copy the rook to the new square
                board[move.end().row()][move.end().col() - 2] = "--";  // remove the old rook
            }
        }

        updateCastleRights(move);
        this.castleRightsLog.add(new CastleRights(currentCastleRights.wks, currentCastleRights.bks, currentCastleRights.wqs, currentCastleRights.bqs));

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

        ArrayList<Move> moves = getAllPossibleMoves();

        // generate castle moves
        if (whiteToMove) {
            System.out.println("Getting castle moves");
            getCastleMoves(whiteKingLocation.row(), whiteKingLocation.col(), moves);
        } else {
            getCastleMoves(blackKingLocation.row(), blackKingLocation.col(), moves);
        }

        System.out.println("Getting all valid moves");
        return moves; // Simplified for now
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

    public boolean squareUnderAttack(int r, int c) {
        whiteToMove =! whiteToMove;
        ArrayList<Move> oppMoves = getAllPossibleMoves();
        whiteToMove =! whiteToMove;
        for (Move oppMove : oppMoves) {
            if (oppMove.end().row() == r && oppMove.end().col() == c) {
                return true;
            }
        }
        return false;
    }

    public void updateCastleRights(Move move) {
    // Check if the king moved or the rook moved
    if (move.pieceMoved().equals("wK")) {
        this.currentCastleRights.wks = false;
        this.currentCastleRights.wqs = false;
    } else if (move.pieceMoved().equals("bK")) {
        this.currentCastleRights.bks = false;
        this.currentCastleRights.bqs = false;
    } else if (move.pieceMoved().equals("wR")) {
        if (move.start().row() == 7) {
            if (move.start().col() == 0) {  // White's left rook
                this.currentCastleRights.wqs = false;
            }
            if (move.start().col() == 7) {  // White's right rook
                this.currentCastleRights.wks = false;
            }
        }
    } else if (move.pieceMoved().equals("bR")) {
        if (move.start().row() == 0) {
            if (move.start().col() == 0) {  // Black's left rook
                this.currentCastleRights.bqs = false;
            }
            if (move.start().col() == 7) {  // Black's right rook
                this.currentCastleRights.bks = false;
            }
        }
    }

    // Check if a rook is captured
    if (move.pieceCaptured().equals("wR")) {
        if (move.end().row() == 7) {
            if (move.end().col() == 0) {
                this.currentCastleRights.wqs = false;
            } else if (move.end().col() == 7) {
                this.currentCastleRights.wks = false;
            }
        }
    } else if (move.pieceCaptured().equals("bR")) {
        if (move.end().row() == 0) {
            if (move.end().col() == 0) {
                this.currentCastleRights.bqs = false;
            } else if (move.end().col() == 7) {
                this.currentCastleRights.bks = false;
                }
            }
        }
    }

    public void getCastleMoves(int r, int c, List<Move> moves) {
        if (squareUnderAttack(r, c)) { return; }  // King can't escape check by castling.
        if ((whiteToMove && currentCastleRights.wks) || (!whiteToMove && currentCastleRights.bks)) {
            getKingSideCastleMoves(r, c, moves);
        }
        if ((whiteToMove && currentCastleRights.wqs) || (!whiteToMove && currentCastleRights.bqs)) {
            getQueenSideCastleMoves(r, c, moves);
        }
    }

    public void getKingSideCastleMoves(int r, int c, List<Move> moves) {
        if (board[r][c + 1].equals("--") && board[r][c + 2].equals("--")) {
            if (!squareUnderAttack(r, c + 1) && !squareUnderAttack(r, c + 2)) {
                moves.add(Move.castleMove(new Square(c, r), new Square(c + 2, r), board));
            }
        }
    }
    public void getQueenSideCastleMoves(int r, int c, List<Move> moves) {
        if (board[r][c-1].equals("--") && board[r][c-2].equals("--") && board[r][c-3].equals("--")) {
            if (!squareUnderAttack(r, c-1) && !squareUnderAttack(r, c-2)) {
                moves.add(Move.castleMove(new Square(c, r), new Square(c-2, r), board));
            }
        }
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
            // Capturing left
            if (c - 1 >= 0 ) {
                if (board[r - 1][c - 1].charAt(0) == 'b') { // Capturing left
                moves.add(new Move(new Square(c, r), new Square(c - 1, r - 1), board));
                }
                else if (new Square(c - 1, r - 1).equals(this.enpassantPossible)  && board[r][c-1].charAt(0) == 'b') {  // Enpassant capture left
                    System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c-1, r-1);
                    moves.add(Move.enpassantMove(new Square(c, r), new Square(c-1, r-1), board));
                }
            }
            // Capturing right
            if (c + 1 <= 7) {
                if (board[r - 1][c + 1].charAt(0) == 'b') { // Capturing right
                    moves.add(new Move(new Square(c, r), new Square(c + 1, r - 1), board));
                }
                else if (new Square(c + 1, r - 1).equals(this.enpassantPossible) && board[r][c+1].charAt(0) == 'b') {  // Enpassant capture right
                    System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c+1, r-1);
                    moves.add(Move.enpassantMove(new Square(c, r), new Square(c + 1, r - 1), board));
                }
            }
        } else { // Black pawn logic
            // Forward moves
            if (board[r + 1][c].equals("--")) {
                moves.add(new Move(new Square(c, r), new Square(c, r + 1), board));
                if (r == 1 && board[r + 2][c].equals("--")) {
                    moves.add(new Move(new Square(c, r), new Square(c, r + 2), board));
                }
            }
            // Capturing left
            if (c - 1 >= 0 ) {
                if (board[r + 1][c - 1].charAt(0) == 'w') { // Capturing left
                moves.add(new Move(new Square(c, r), new Square(c - 1, r + 1), board));
                }
                else if (new Square(c - 1, r + 1).equals(this.enpassantPossible) && board[r][c-1].charAt(0) == 'w') {  // Enpassant capture left
                    System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c-1, r+1);
                    moves.add(Move.enpassantMove(new Square(c, r), new Square(c-1, r+1), board));
                }
            }
            // Capturing right
            if (c + 1 <= 7) {
                if (board[r + 1][c + 1].charAt(0) == 'w') { // Capturing right
                    moves.add(new Move(new Square(c, r), new Square(c + 1, r + 1), board));
                }
                else if (new Square(c + 1, r + 1).equals(this.enpassantPossible) && board[r][c+1].charAt(0) == 'w') {  // Enpassant capture right
                    System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c+1, r+1);
                    moves.add(Move.enpassantMove(new Square(c, r), new Square(c + 1, r + 1), board));
                }
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