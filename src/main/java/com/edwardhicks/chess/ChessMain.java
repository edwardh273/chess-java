package com.edwardhicks.chess;

import javax.swing.*;
import com.edwardhicks.chess.ui.BoardPanel;


public class ChessMain extends JFrame {

    public ChessMain() {
        setTitle("Java Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize GameState and add board to window
        GameState gameState = new GameState();
        BoardPanel boardPanel = new BoardPanel(gameState);
        add(boardPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);  // Triggers first paintComponent() call
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(ChessMain::new);
    }
}