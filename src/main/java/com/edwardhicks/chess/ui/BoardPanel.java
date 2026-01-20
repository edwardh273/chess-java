package com.edwardhicks.chess.ui;

import javax.swing.*;
import java.awt.*;

import static com.edwardhicks.chess.Constants.*;

public class BoardPanel extends JPanel {

    // A 2D array to store the chess board pieces
    private String[][] board;

    // Constructor, runs when I crate a new BoardPanel
    public BoardPanel() {
        setPreferredSize(new Dimension(BOARD_LENGTH, BOARD_LENGTH));
        initializeBoard();
    }

    @Override  // replaces JPanel's default paintComponent
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
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


    private void initializeBoard() {
        board = new String[DIMENSION][DIMENSION];
    }
}