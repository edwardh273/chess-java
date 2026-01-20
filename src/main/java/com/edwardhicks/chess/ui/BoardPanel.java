package com.edwardhicks.chess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.edwardhicks.chess.Constants.*;
import static com.edwardhicks.chess.ui.ImageLoader.getPieceImage;

public class BoardPanel extends JPanel {

    // A 2D array to store the chess board pieces
    private String[][] board;

    // Constructor, runs when I crate a new BoardPanel
    public BoardPanel() {
        setPreferredSize(new Dimension(BOARD_LENGTH, BOARD_LENGTH));
        initializeBoard();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / SQ_SIZE;
                int row = e.getY() / SQ_SIZE;
                handleSquareClick(col, row);
            }
        });
    }

    @Override  // replaces JPanel's default paintComponent
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
    }

    private void drawBoard(Graphics g) {
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                Color color = (r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                g.setColor(color);
                g.fillRect(c * SQ_SIZE, r * SQ_SIZE, SQ_SIZE, SQ_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                String piece = board[r][c];
                if (!piece.equals("--")) {
                    Image pieceImage = getPieceImage(piece);
                    g.drawImage(pieceImage, c * SQ_SIZE, r * SQ_SIZE, null);
                }

            }
        }
    }

    private void handleSquareClick(int col, int row) {
        System.out.println("Clicked: " + col + ", " + row);
    }


    private void initializeBoard() {
        board = new String[][] {
                {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
                {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
                {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}
        };
    }
}