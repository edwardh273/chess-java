package com.edwardhicks.chess;

import javax.swing.*;
import com.edwardhicks.chess.ui.BoardPanel;


public class ChessMain extends JFrame {

    public ChessMain() {
        setTitle("Java Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Add chess board panel
        BoardPanel boardPanel = new BoardPanel();
        add(boardPanel);

        pack();

        setLocationRelativeTo(null);
        setVisible(true);  // Triggers first paintComponent() call
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessMain());
    }
}