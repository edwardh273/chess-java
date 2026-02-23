package com.edwardhicks.chess.ai;

import com.edwardhicks.chess.GameState;
import com.edwardhicks.chess.Move;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ChessAI {

    private static final int CHECKMATE = 1000;
    private static final int STALEMATE = 0;
    private static final int WHITE_DEPTH = 5;
    private static final int BLACK_DEPTH = 3;

    private Move nextMove = null;
    private int counter = 0;

    /**
     * Score board. Positive score is good for white, negative score is good for black.
     */
    public double scoreBoard(GameState gs) {
        if (gs.isCheckMate()) {
            if (gs.isWhiteToMove()) {
                return -CHECKMATE;  // Black wins
            } else {
                return CHECKMATE;  // White wins
            }
        } else if (gs.isStaleMate()) {
            return STALEMATE;
        }

        double score = 0;
        String[][] board = gs.getBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String square = board[row][col];
                if (!square.equals("--")) {
                    char color = square.charAt(0);
                    char piece = square.charAt(1);

                    double piecePositionScore = PieceScores.getPiecePositionScore(square, row, col);

                    if (color == 'w') {
                        score += PieceScores.getPieceScore(piece) + piecePositionScore * 0.1;
                    } else if (color == 'b') {
                        score -= (PieceScores.getPieceScore(piece) + piecePositionScore * 0.1);
                    }
                }
            }
        }
        return score;
    }

    /**
     * The function that is called by ChessMain
     */
    public Move findBestMove(GameState gs, List<Move> validMoves) {
        long startTime = System.currentTimeMillis();
        nextMove = null;
        counter = 0;

        int depth = gs.isWhiteToMove() ? WHITE_DEPTH : BLACK_DEPTH;

        // Sort moves for better alpha-beta pruning
        validMoves.sort(Comparator.comparingDouble(move -> -moveSortAlgo(move, gs)));

        double bestScore = findMoveNegaMaxAlphaBeta(
            gs,
            validMoves,
            depth,
            -CHECKMATE,
            CHECKMATE,
            gs.isWhiteToMove() ? 1 : -1,
            gs.isWhiteToMove()
        );

        long endTime = System.currentTimeMillis();
        double timeTaken = (endTime - startTime) / 1000.0;

        System.out.printf("movesSearched: %d     maxScore: %.3f     Time: %.2f%n",
                         counter, bestScore, timeTaken);

        return nextMove;
    }

    /**
     * Function to sort valid moves before they are passed into alpha-beta pruning.
     * Likely strongest moves should be searched first for better pruning efficiency.
     */
    private double moveSortAlgo(Move move, GameState gameState) {
        double score = 0;

        if (!move.pieceCaptured().equals("--")) {
            score += 10 * PieceScores.getPieceScore(move.pieceCaptured().charAt(1))
                     - PieceScores.getPieceScore(move.pieceMoved().charAt(1));
        }

        if (gameState.squareUnderAttack(move.end().row(), move.end().col())) {
            if (move.pieceCaptured().equals("--")) {
                score -= PieceScores.getPieceScore(move.pieceMoved().charAt(1));
            }
        }

        if (move.pieceCaptured().equals("--") &&
            gameState.squareUnderAttack(move.start().row(), move.start().col())) {
            score += PieceScores.getPieceScore(move.pieceMoved().charAt(1)) * 0.5;
        }

        if (move.isPawnPromotion()) {
            score += PieceScores.getPieceScore('Q') - PieceScores.getPieceScore('p');
        }

        if (move.pieceCaptured().equals("--")) {
            double centerDistance = Math.abs(3.5 - move.end().row()) +
                                   Math.abs(3.5 - move.end().col());
            score += (7 - centerDistance) * 0.1;
        }

        return score;
    }

    /**
     * findNegaMaxAlphaBeta. Always find the maximum score for black and white.
     * Alpha = Best score the current player has found so far (starts at -1000)
     * Beta = Best score the opponent has found so far (starts at +1000)
     * When beta <= alpha, the maximizing player need not consider further descendants.
     */
    private double findMoveNegaMaxAlphaBeta(GameState gs, List<Move> validMoves,
                                           int depth, double alpha, double beta,
                                           int turnMultiplier, boolean whiteAI) {
        counter++;

        if (depth == 0) {
            return turnMultiplier * scoreBoard(gs);
        }

        double maxScore = -CHECKMATE;  // Worst scenario

        for (Move move : validMoves) {
            gs.makeMove(move);
            List<Move> nextMoves = gs.getValidMoves();

            double score = -findMoveNegaMaxAlphaBeta(
                gs,
                nextMoves,
                depth - 1,
                -beta,
                -alpha,
                -turnMultiplier,
                whiteAI
            );

            if (score > maxScore) {
                maxScore = score;
                if ((depth == WHITE_DEPTH && whiteAI) || (depth == BLACK_DEPTH && !whiteAI)) {
                    nextMove = move;
                    System.out.printf("%d %.3f%n", move.getMoveID(), maxScore);
                }
            }

            gs.undoMove();

            alpha = Math.max(maxScore, alpha);  // Pruning

            if (beta <= alpha) {
                // Stop searching - opponent has already found a position limiting us
                break;
            }
        }

        return maxScore;
    }

    /**
     * Returns a random move.
     */
    public static Move findRandomMove(List<Move> validMoves) {
        Random random = new Random();
        return validMoves.get(random.nextInt(validMoves.size()));
    }
}