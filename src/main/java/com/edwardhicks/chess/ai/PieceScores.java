package com.edwardhicks.chess.ai;

import java.util.HashMap;
import java.util.Map;

public class PieceScores {

    public static final Map<Character, Double> pieceScore = new HashMap<>();

    static {
        pieceScore.put('K', 0.0);
        pieceScore.put('Q', 10.0);
        pieceScore.put('R', 5.0);
        pieceScore.put('B', 3.15);
        pieceScore.put('N', 3.0);
        pieceScore.put('p', 1.0);
    }

    public static final int[][] knightScore = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1}
    };

    public static final int[][] bishopScore = {
        {4, 3, 2, 1, 1, 2, 3, 4},
        {3, 4, 3, 2, 2, 3, 4, 3},
        {2, 3, 4, 3, 3, 4, 3, 2},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {2, 3, 4, 3, 3, 4, 3, 2},
        {3, 4, 3, 2, 2, 3, 4, 3},
        {4, 3, 2, 1, 1, 2, 3, 4}
    };

    public static final int[][] queenRookScore = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 2, 2, 2, 2, 1, 1},
        {1, 1, 2, 3, 3, 2, 1, 1},
        {1, 1, 2, 3, 3, 2, 1, 1},
        {1, 1, 2, 2, 2, 2, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1}
    };

    public static final int[][] kingScore = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {3, 4, 9, 2, 2, 1, 9, 3}
    };

    public static final int[][] whitePawnScore = {
        {6, 6, 6, 6, 6, 6, 6, 6},
        {5, 5, 5, 5, 5, 5, 5, 5},
        {4, 4, 4, 4, 4, 4, 4, 4},
        {3, 3, 3, 4, 4, 3, 3, 3},
        {2, 2, 2, 4, 4, 2, 2, 2},
        {1, 1, 1, 3, 3, 1, 1, 1},
        {1, 1, 1, 0, 0, 1, 1, 1},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };

    // Reverse array for black pieces
    private static final int[][] blackPawnScore = reverseArray(whitePawnScore);
    private static final int[][] blackKingScore = reverseArray(kingScore);

    private static final Map<String, int[][]> piecePositionScores = new HashMap<>();

    static {
        piecePositionScores.put("wQ", queenRookScore);
        piecePositionScores.put("wR", queenRookScore);
        piecePositionScores.put("bQ", queenRookScore);
        piecePositionScores.put("bR", queenRookScore);
        piecePositionScores.put("wN", knightScore);
        piecePositionScores.put("bN", knightScore);
        piecePositionScores.put("wB", bishopScore);
        piecePositionScores.put("bB", bishopScore);
        piecePositionScores.put("wp", whitePawnScore);
        piecePositionScores.put("bp", blackPawnScore);
        piecePositionScores.put("wK", kingScore);
        piecePositionScores.put("bK", blackKingScore);
    }

    /**
     * Get the base score for a piece type
     */
    public static double getPieceScore(char piece) {
        return pieceScore.get(piece);
    }

    /**
     * Get position score for a piece at a given position
     */
    public static int getPiecePositionScore(String piece, int row, int col) {
        int[][] scores = piecePositionScores.get(piece);
        if (scores != null) {
            return scores[row][col];
        }
        return 0;
    }

    /**
     * Helper method to reverse a 2D array
     */
    private static int[][] reverseArray(int[][] array) {
        int[][] reversed = new int[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i].clone();
        }
        return reversed;
    }
}