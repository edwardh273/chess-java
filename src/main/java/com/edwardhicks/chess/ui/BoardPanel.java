package com.edwardhicks.chess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.edwardhicks.chess.Move;
import com.edwardhicks.chess.Square;
import com.edwardhicks.chess.GameState;

import static com.edwardhicks.chess.Constants.*;
import static com.edwardhicks.chess.ui.ImageLoader.getPieceImage;

public class BoardPanel extends JPanel {

    private final GameState gameState;

    // TODO: should the below be a fixed Array?
    private final ArrayList<Square> playerClicks = new ArrayList<>();
    private ArrayList<Move> validMoves = new ArrayList<>();



    // Constructor, runs when I crate a new BoardPanel
    public BoardPanel(GameState gs) {
        this.gameState = gs;
        setPreferredSize(new Dimension(BOARD_LENGTH, BOARD_LENGTH));

        validMoves = gameState.getValidMoves();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / SQ_SIZE;
                int row = e.getY() / SQ_SIZE;

                System.out.println("Clicked: " + col + ", " + row);
                // piece selected
                Square sqSelected = new Square(col, row);

                if (playerClicks.size() == 1 && playerClicks.get(0).col() == col && playerClicks.get(0).row() == row) {
                    // Deselect logic
                    System.out.println("same sq collected, clicks cleared");
                    playerClicks.clear();
                    repaint();
                } else{
                    playerClicks.add(sqSelected);
                    repaint();
                }

                if (playerClicks.size() == 2) {
                    // attempt a move by passing move to GameState
                    Square start = playerClicks.get(0);
                    Square end = playerClicks.get(1);

                    Move moveAttempt = new Move(start, end, gameState.getBoard());

                    for (Move move : validMoves) {
                        if (moveAttempt.equals(move)) {
                            gameState.makeMove(move);
                            validMoves = gameState.getValidMoves();
                            break;
                        }
                    }

                    playerClicks.clear();
                    repaint();
                    }
                }
        });
    }

    @Override  // replaces JPanel's default paintComponent
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);

        if (!playerClicks.isEmpty()) {
            highlightSquares(g, gameState, validMoves, playerClicks.getFirst());
        }
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
        String[][] currentBoard = gameState.getBoard();
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                String piece = currentBoard[r][c];
                if (!piece.equals("--")) {
                    Image pieceImage = getPieceImage(piece);
                    g.drawImage(pieceImage, c * SQ_SIZE, r * SQ_SIZE, null);
                }

            }
        }
    }

    /**
     * Highlight square selected and available moves.
     */
    public void highlightSquares(Graphics g, GameState gs, ArrayList<Move> validMoves, Square sqSelected) {
        if (sqSelected != null) {
            int c = sqSelected.col();
            int r = sqSelected.row();

            String piece = gs.getBoard()[r][c];
            char pieceColor = piece.charAt(0);
            char currentPlayerColor = gs.whiteToMove ? 'w' : 'b';

            // Square selected is a piece of the player whose turn it is
            if (pieceColor == currentPlayerColor) {
                Graphics2D g2d = (Graphics2D) g;

                // Highlight selected square in blue with transparency
                g2d.setColor(BLUE_TRANSPARENT);
                g2d.fillRect(c * SQ_SIZE, r * SQ_SIZE, SQ_SIZE, SQ_SIZE);

                // Highlight valid moves from that square in yellow with transparency
                g2d.setColor(YELLOW_TRANSPARENT);

                for (Move move : validMoves) {
                    if (move.start().row() == r && move.start().col() == c) {
                        g2d.fillRect(SQ_SIZE * move.end().col(), SQ_SIZE * move.end().row(), SQ_SIZE, SQ_SIZE);
                    }
                }
            }
        }
    }
}