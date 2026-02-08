package com.edwardhicks.chess;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static java.lang.Math.abs;

public class GameState {
    private final String[][] board;
    public boolean whiteToMove;
    public final List<Move> moveLog;
    private final List<CastleRights> castleRightsLog;
    private final List<Square> enPassantLog;
    private Square whiteKingLocation;
    private Square blackKingLocation;
    private Square enPassantPossible;
    private CastleRights currentCastleRights;
    public boolean checkMate;
    public boolean staleMate;
    private List<PinOrCheck> pins;
    private List<PinOrCheck> checks;
    private boolean inCheck;
    public record PinsAndChecks(boolean inCheck, List<PinOrCheck> pins, List<PinOrCheck> checks) {}




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
        this.checkMate = false;
        this.staleMate = false;

        this.moveLog = new ArrayList<Move>();

        this.currentCastleRights = new CastleRights(true, true, true, true);
        this.castleRightsLog = new ArrayList<CastleRights>(Collections.singleton(this.currentCastleRights));

        this.enPassantPossible = null;
        this.enPassantLog = new ArrayList<Square>(Collections.singleton(this.enPassantPossible));

        this.checkMate = false;
        this.staleMate = false;
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
            this.enPassantPossible = new Square(move.start().col(), (move.start().row() + move.end().row()) / 2 );  // enpassant possible to the square where the pawn would have moved if it had only moved 1 square.
            System.out.println("enpassant possible: " + this.enPassantPossible);
        } else {
            this.enPassantPossible = null;
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

        this.enPassantLog.add(this.enPassantPossible);

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
     * All moves considering check
     */
    public ArrayList<Move> getValidMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        PinsAndChecks result = checkForPinsAndChecks();
        this.inCheck = result.inCheck();
        this.pins = result.pins();
        this.checks = result.checks();

        int kingCol, kingRow;
        if (whiteToMove) {
            kingCol = whiteKingLocation.col();
            kingRow = whiteKingLocation.row();
        } else {
            kingCol = blackKingLocation.col();
            kingRow = blackKingLocation.row();
        }

        if (inCheck) {
            if (checks.size() == 1) {  // Only 1 check: block check or move king
                moves = getAllPossibleMoves();
                PinOrCheck check = checks.getFirst();  // (pos dir-col, dir-row)
                int checkCol = check.pos().col();
                int checkRow = check.pos().row();
                String pieceChecking = board[checkRow][checkCol];
                List<Square> validSquares = new ArrayList<>();

                if (pieceChecking.charAt(1) == 'N') {  // If knight, must capture knight or move king
                    validSquares.add(new Square(checkCol, checkRow));
                } else {  // Else block the check
                    for (int i = 1; i < 8; i++) {
                        Square validSquare = new Square(kingCol + check.dirCol() * i,
                                                        kingRow + check.dirRow() * i);
                        validSquares.add(validSquare);
                        if (validSquare.col() == checkCol && validSquare.row() == checkRow) {  // Go up to the check square
                            break;
                        }
                    }
                }

                // Get rid of any moves that don't block check or move king
                for (int i = moves.size() - 1; i >= 0; i--) {  // Go through backwards when removing from a list
                    if (moves.get(i).pieceMoved().charAt(1) != 'K') {  // Move doesn't move king, so must block or capture
                        Square endSquare = moves.get(i).end();
                        if (!validSquares.contains(endSquare)) {
                            moves.remove(i);
                        }
                    }
                }
            } else {  // Double check, king has to move
                getKingMoves(kingRow, kingCol, moves);
            }
        } else {  // Not in check so all moves are fine
            moves = getAllPossibleMoves();
        }

        // To generate castle moves
        if (whiteToMove) {
            getCastleMoves(7, 4, moves);  // Can only castle if the king hasn't moved
        } else {
            getCastleMoves(0, 4, moves);
        }

        return moves;
    }

    /**
     * Returns if the player is in check, a list of pins, and a list of checks
     */
    public PinsAndChecks checkForPinsAndChecks() {
        List<PinOrCheck> pins = new ArrayList<>();  // Square of pinned piece & direction pinned from
        List<PinOrCheck> checks = new ArrayList<>();  // Squares where enemy is applying a check
        boolean inCheck = false;

        char enemyColor, allyColor;
        int startCol, startRow;

        if (whiteToMove) {
            enemyColor = 'b';
            allyColor = 'w';
            startCol = whiteKingLocation.col();
            startRow = whiteKingLocation.row();
        } else {
            enemyColor = 'w';
            allyColor = 'b';
            startCol = blackKingLocation.col();
            startRow = blackKingLocation.row();
        }

        // Check outward from king for pins and checks, keep track of pins. Pin direction is OUTWARD from the KING
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        // (col, row): l, r, u, d, lu, ru, ld, rd

        for (int j = 0; j < directions.length; j++) {
            int[] d = directions[j];
            PinOrCheck possiblePin = null;  // Reset possible pin for said direction

            for (int i = 1; i < 8; i++) {  // Up until the end of the board
                int endCol = startCol + d[0] * i;
                int endRow = startRow + d[1] * i;

                if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) {
                    String endPiece = board[endRow][endCol];

                    // Check for pins
                    if (endPiece.charAt(0) == allyColor && endPiece.charAt(1) != 'K') {
                        if (possiblePin == null) {  // 1st allied piece could be pinned
                            possiblePin = new PinOrCheck(new Square(endCol, endRow), d[0], d[1]);
                        } else {  // 2nd allied piece, so no pin or check possible in this direction
                            break;
                        }
                    }
                    // Check for checks
                    else if (endPiece.charAt(0) == enemyColor) {
                        char piece = endPiece.charAt(1);

                        // Orthogonally from king & piece == rook
                        // Diagonally & piece == bishop
                        // 1 square away & piece == pawn
                        // Any direction & piece == queen
                        // Any direction 1 square away & piece == king
                        if ((0 <= j && j <= 3 && piece == 'R') ||
                            (4 <= j && j <= 7 && piece == 'B') ||
                            (i == 1 && piece == 'p' && ((enemyColor == 'w' && 6 <= j && j <= 7) ||
                                                         (enemyColor == 'b' && 4 <= j && j <= 5))) ||
                            (piece == 'Q') ||
                            (i == 1 && piece == 'K')) {

                            if (possiblePin == null) {  // If enemy piece in range and no pin, inCheck = true
                                inCheck = true;
                                checks.add(new PinOrCheck(new Square(endCol, endRow), d[0], d[1]));
                                break;
                            } else {  // Allied piece blocking so pin
                                pins.add(possiblePin);
                                break;
                            }
                        } else {  // Enemy piece not applying check
                            break;
                        }
                    }
                } else {  // Off board
                    break;
                }
            }
        }

        // Knight checks
        int[][] knightMoves = {{-1, -2}, {-2, -1}, {-2, 1}, {-1, 2},
                               {1, 2}, {2, 1}, {2, -1}, {1, -2}};
        for (int[] m : knightMoves) {
            int endCol = startCol + m[0];
            int endRow = startRow + m[1];

            if (endCol >= 0 && endCol < 8 && endRow >= 0 && endRow < 8) {
                String endPiece = board[endRow][endCol];
                if (endPiece.charAt(0) == enemyColor && endPiece.charAt(1) == 'N') {  // Enemy knight attacking king
                    inCheck = true;
                    checks.add(new PinOrCheck(new Square(endCol, endRow), m[0], m[1]));
                }
            }
        }

        return new PinsAndChecks(inCheck, pins, checks);
    }




    /**
     * Undo the last move made
     */
    public void undoMove() {
        if (!moveLog.isEmpty()) {  // Make sure that there is a move to undo
            Move move = moveLog.remove(moveLog.size() - 1);  // Pop last move
            board[move.start().row()][move.start().col()] = move.pieceMoved();
            board[move.end().row()][move.end().col()] = move.pieceCaptured();

            // Update king's location
            if (move.pieceMoved().equals("wK")) {
                whiteKingLocation = new Square(move.start().col(), move.start().row());
            } else if (move.pieceMoved().equals("bK")) {
                blackKingLocation = new Square(move.start().col(), move.start().row());
            }

            whiteToMove = !whiteToMove;  // Swap players back

            // Undo en passant
            if (move.isEnpassantMove()) {
                board[move.end().row()][move.end().col()] = "--";  // Leave landing square blank
                board[move.start().row()][move.end().col()] = move.pieceCaptured();
                enPassantPossible = new Square(move.end().col(), move.end().row());
            }

            // Undo a 2 square pawn advance
            if (move.pieceMoved().charAt(1) == 'p' &&
                Math.abs(move.start().row() - move.end().row()) == 2) {
                enPassantPossible = null;
            }

            // Undo castle move
            if (move.isCastleMove()) {
                if (move.end().col() - move.start().col() == 2) {  // Kingside
                    board[move.end().row()][move.end().col() + 1] = board[move.end().row()][move.end().col() - 1];
                    board[move.end().row()][move.end().col() - 1] = "--";  // Remove the old rook
                } else if (move.end().col() - move.start().col() == -2) {  // Queenside
                    board[move.end().row()][move.end().col() - 2] = board[move.end().row()][move.end().col() + 1];
                    board[move.end().row()][move.end().col() + 1] = "--";  // Remove the old rook
                }
            }

            // Undo castling rights
            castleRightsLog.removeLast();  // Get rid of castle rights from move we are undoing
            CastleRights lastCastleRights = castleRightsLog.getLast();
            currentCastleRights = new CastleRights(lastCastleRights.wks, lastCastleRights.bks, lastCastleRights.wqs, lastCastleRights.bqs);

            enPassantLog.removeLast();
            enPassantPossible = enPassantLog.getLast();

            checkMate = false;
            staleMate = false;

        }
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

    public boolean inCheck() {
        if (whiteToMove) {
            return squareUnderAttack(this.whiteKingLocation.row(), this.whiteKingLocation.col());
        } else {
            return squareUnderAttack(this.blackKingLocation.row(), blackKingLocation.col());
        }
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
        if (!whiteToMove && !blackKingLocation.equals(new Square(4, 0))) return;
        if (whiteToMove && !whiteKingLocation.equals(new Square(4, 7))) return;
        if (!(board[r][c - 1].equals("--") && board[r][c - 2].equals("--") && board[r][c - 3].equals("--")) &&
            !(board[r][c + 1].equals("--") && board[r][c + 2].equals("--"))) {
            return;  // If queenside and kingside blocked, return
        }

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
        boolean piecePinned = false;
        int[] pinDirection = null;

        // Check if piece is pinned
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinOrCheck pin = pins.get(i);
            if (pin.pos().col() == c && pin.pos().row() == r) {  // pins = (pos, dirCol, dirRow)
                piecePinned = true;
                pinDirection = new int[]{pin.dirCol(), pin.dirRow()};
                pins.remove(i);
                break;
            }
        }

        // White pawn logic
        if (whiteToMove) {
            // Forward moves
            if (board[r - 1][c].equals("--")) {
                if (!piecePinned || Arrays.equals(pinDirection, new int[]{0, -1})) {
                    moves.add(new Move(new Square(c, r), new Square(c, r - 1), board));
                    if (r == 6 && board[r - 2][c].equals("--")) {
                        moves.add(new Move(new Square(c, r), new Square(c, r - 2), board));
                    }
                }

            }
            // Capturing left
            if (c - 1 >= 0) {
                if (board[r - 1][c - 1].charAt(0) == 'b') {
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{-1, -1})) {
                        moves.add(new Move(new Square(c, r), new Square(c - 1, r - 1), board));
                    }
                } else if (new Square(c - 1, r - 1).equals(this.enPassantPossible) && board[r][c - 1].charAt(0) == 'b') {  // Enpassant capture left
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{-1, -1})) {
                        System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c - 1, r - 1);
                        moves.add(Move.enpassantMove(new Square(c, r), new Square(c - 1, r - 1), board));
                    }
                }
            }
            // Capturing right
            if (c + 1 <= 7) {
                if (board[r - 1][c + 1].charAt(0) == 'b') { // Capturing right
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{1, -1})) {
                        moves.add(new Move(new Square(c, r), new Square(c + 1, r - 1), board));
                    }
                } else if (new Square(c + 1, r - 1).equals(this.enPassantPossible) && board[r][c + 1].charAt(0) == 'b') {  // Enpassant capture right
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{1, -1})) {
                        System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c + 1, r - 1);
                        moves.add(Move.enpassantMove(new Square(c, r), new Square(c + 1, r - 1), board));
                    }
                }
            }
            // Black pawn logic
        } else {
            // Forward moves
            if (board[r + 1][c].equals("--")) {
                if (!piecePinned || Arrays.equals(pinDirection, new int[]{0, 1})) {
                    moves.add(new Move(new Square(c, r), new Square(c, r + 1), board));
                    if (r == 1 && board[r + 2][c].equals("--")) {
                        moves.add(new Move(new Square(c, r), new Square(c, r + 2), board));
                    }
                }
            }
            // Capturing left
            if (c - 1 >= 0) {
                if (board[r + 1][c - 1].charAt(0) == 'w') { // Capturing left
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{-1, 1})) {
                        moves.add(new Move(new Square(c, r), new Square(c - 1, r + 1), board));
                    }
                } else if (new Square(c - 1, r + 1).equals(this.enPassantPossible) && board[r][c - 1].charAt(0) == 'w') {  // Enpassant capture left
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{-1, 1})) {
                        System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c - 1, r + 1);
                        moves.add(Move.enpassantMove(new Square(c, r), new Square(c - 1, r + 1), board));
                    }
                }
            }
            // Capturing right
            if (c + 1 <= 7) {
                if (board[r + 1][c + 1].charAt(0) == 'w') { // Capturing right
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{1, 1})) {
                        moves.add(new Move(new Square(c, r), new Square(c + 1, r + 1), board));
                    }
                } else if (new Square(c + 1, r + 1).equals(this.enPassantPossible) && board[r][c + 1].charAt(0) == 'w') {  // Enpassant capture right
                    if (!piecePinned || Arrays.equals(pinDirection, new int[]{1, 1})) {
                        System.out.printf("Adding en passant move: from (%d,%d) to (%d,%d)%n", c, r, c + 1, r + 1);
                        moves.add(Move.enpassantMove(new Square(c, r), new Square(c + 1, r + 1), board));
                    }
                }
            }
        }
    }

    /*
     * Get all Rook moves for the Rook located at row, col and add these moves to the list
     */
    public void getRookMoves(int r, int c, List<Move> moves) {

        boolean piecePinned = false;
        int[] pinDirection = null;

        // Check if piece is pinned
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinOrCheck pin = pins.get(i);
            if (pin.pos().col() == c && pin.pos().row() == r) {  // pins = (pos, dirCol, dirRow)
                piecePinned = true;
                pinDirection = new int[]{pin.dirCol(), pin.dirRow()};
                if (board[r][c].charAt(1) != 'Q') {
                    pins.remove(i);
                }
                break;
            }
        }

        char enemyColor = whiteToMove ? 'b' : 'w';
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // left, right, up, down

        for (int[] d : directions) {
            for (int i = 1; i < 8; i++) {
                int endRow = r + d[0] * i;
                int endCol = c + d[1] * i;

                if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
                    if (!piecePinned || Arrays.equals(pinDirection, d) || Arrays.equals(pinDirection, new int[]{-d[0], -d[1]})) {
                        String endPiece = board[endRow][endCol];

                        if (endPiece.equals("--")) { // if blank, append move
                            moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                        } else if (endPiece.charAt(0) == enemyColor) { // hits enemy piece
                            moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                            break;
                        } else { // hits own color piece
                            break;
                        }
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

        boolean piecePinned = false;
        int[] pinDirection = null;

        // Check if piece is pinned
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinOrCheck pin = pins.get(i);
            if (pin.pos().col() == c && pin.pos().row() == r) {  // pins = (pos, dirCol, dirRow)
                piecePinned = true;
                pinDirection = new int[]{pin.dirCol(), pin.dirRow()};
                if (board[r][c].charAt(1) != 'Q') {
                    pins.remove(i);
                }
                break;
            }
        }

        char enemyColor = whiteToMove ? 'b' : 'w';
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}; // leftup, rightup, leftdown, rightdown

        for (int[] d : directions) {
            for (int i = 1; i < 8; i++) {
                int endRow = r + d[0] * i;
                int endCol = c + d[1] * i;

                if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
                    if (!piecePinned || Arrays.equals(pinDirection, d) || Arrays.equals(pinDirection, new int[]{-d[0], -d[1]})) {
                        String endPiece = board[endRow][endCol];

                        if (endPiece.equals("--")) { // if blank, append move
                            moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                        } else if (endPiece.charAt(0) == enemyColor) { // hits enemy piece
                            moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                            break;
                        } else { // hits own color piece
                            break;
                        }
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
        boolean piecePinned = false;

        // Check if piece is pinned
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinOrCheck pin = pins.get(i);
            if (pin.pos().col() == c && pin.pos().row() == r) {  // pins = (pos, dirCol, dirRow)
                piecePinned = true;
                if (board[r][c].charAt(1) != 'Q') {
                    pins.remove(i);
                }
                break;
            }
        }

        int[][] potentialMoves = {{-1, -2}, {-2, -1}, {-2, 1}, {-1, 2},
                                   {1, 2}, {2, 1}, {2, -1}, {1, -2}};
        char allyColor = whiteToMove ? 'w' : 'b';

        for (int[] m : potentialMoves) {
            int endRow = r + m[0];
            int endCol = c + m[1];

            if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) { // confine to board
                if (!piecePinned) {
                    String endPiece = board[endRow][endCol];

                    if (endPiece.charAt(0) != allyColor) {
                        moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                    }
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

                if (endPiece.charAt(0) != allyColor) {  // Empty or enemy piece
                    if (allyColor == 'w') {  // Place king on square and check for checks
                        whiteKingLocation = new Square(endCol, endRow);
                    } else {
                        blackKingLocation = new Square(endCol, endRow);
                    }

                    PinsAndChecks result = checkForPinsAndChecks();
                    boolean inCheck = result.inCheck();

                    if (!inCheck) {
                        moves.add(new Move(new Square(c, r), new Square(endCol, endRow), board));
                    }

                    if (allyColor == 'w') {
                        whiteKingLocation = new Square(c, r);  // Place king back on original location
                    } else {
                        blackKingLocation = new Square(c, r);
                    }
                }
            }
        }
    }

    public String[][] getBoard() {
        return board;
    }
}