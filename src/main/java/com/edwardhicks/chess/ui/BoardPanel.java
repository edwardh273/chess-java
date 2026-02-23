package com.edwardhicks.chess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.edwardhicks.chess.Move;
import com.edwardhicks.chess.Square;
import com.edwardhicks.chess.GameState;
import com.edwardhicks.chess.ai.ChessAI;

import static com.edwardhicks.chess.Constants.*;
import static com.edwardhicks.chess.ui.ImageLoader.getPieceImage;

public class BoardPanel extends JPanel {

    private final GameState gameState;

    // TODO: should the below be a fixed Array?
    private final ArrayList<Square> playerClicks = new ArrayList<>();
    private ArrayList<Move> validMoves = new ArrayList<>();

    private boolean gameOver = false;
    private boolean whitePlayer = false;
    private boolean blackPlayer = false;
    private boolean humanTurn;
    private final ChessAI ai;




    // Constructor, runs when I create a new BoardPanel
    public BoardPanel(GameState gs) {
        this.gameState = gs;
        this.ai = new ChessAI();
        setPreferredSize(new Dimension(BOARD_LENGTH, BOARD_LENGTH));

        // set humanTurn
        updateTurn();
        validMoves = gameState.getValidMoves();


        // if AI vs AI, need to free up Event Dispatch Thread to allow graphics to fire periodically.
        if (!whitePlayer && !blackPlayer) {
            Timer aiTimer = new Timer(500, null); // 500ms delay between moves

            aiTimer.addActionListener(e -> {
                if (!gameOver) {
                    makeAIMove();
                    repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });

            aiTimer.start();
        }


        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver || !humanTurn) {
                    return;
                }

                int col = e.getX() / SQ_SIZE;
                int row = e.getY() / SQ_SIZE;

                System.out.println("Clicked: " + col + ", " + row);
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

                    // This is where I actually make the move
                    for (Move move : validMoves) {
                        if (moveAttempt.equals(move)) {

                            // execute player move, update turn
                            executeMove(move);
                            break;
                        }
                    }
                    playerClicks.clear();
                }

                if (!humanTurn && !gameOver) {
                    makeAIMove();
                }


            }
        });

        // Add this to your BoardPanel constructor
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Z && !gameState.moveLog.isEmpty()) {  // Undo when 'z' is pressed
                    gameState.undoMove();
                    System.out.println("Undone move");

                    validMoves = gameState.getValidMoves();
                    gameOver = false;
                    repaint();  // Refresh the board display
                }
            }
        });

        // IMPORTANT: Make the panel focusable so it can receive key events
        setFocusable(true);
        requestFocusInWindow();
    }


    /**
     * Execute a move and update the game state
     * @param move The move to execute
     */
    private void executeMove(Move move) {

        // Make the move
        gameState.makeMove(move);

        // update board graphics immediately.
        paintImmediately(getBounds());
        updateTurn();

        // Update valid moves for next turn
        validMoves = gameState.getValidMoves();

        // Check for game over
        if (validMoves.isEmpty()) {
            gameOver = true;
        }
    }

    /**
     * Update whose turn it is and trigger AI move if needed
     */
    private void updateTurn() {
        humanTurn = (gameState.isWhiteToMove() && whitePlayer) || (!gameState.isWhiteToMove() && blackPlayer);
    }

    /**
     * Make the AI move
     */
    private void makeAIMove() {
        Move aiMove = ai.findBestMove(gameState, validMoves);

        if (aiMove != null) {
            executeMove(aiMove);
        } else {
            // Fallback to random move
            Move randomMove = ChessAI.findRandomMove(validMoves);
            if (randomMove != null) {
                executeMove(randomMove);
            }
        }
    }


    @Override  // replaces JPanel's default paintComponent
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);

        if (!playerClicks.isEmpty()) {
            highlightSquares(g, gameState, validMoves, playerClicks.getFirst());
        }

        if (gameOver && gameState.inCheck() && gameState.whiteToMove) {
            drawText(g, "Black wins by checkmate");
        } else if (gameOver && gameState.inCheck() && !gameState.whiteToMove) {
            drawText(g, "White wins by checkmate");
        } else if (gameOver) {
            drawText(g, "Stalemate");
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

    private void drawText(Graphics g, String text) {
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smooth text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(font);

        // Get text dimensions for centering
        FontMetrics metrics = g2d.getFontMetrics(font);
        int x = (BOARD_LENGTH - metrics.stringWidth(text)) / 2;
        int y = BOARD_LENGTH / 2;

        // Draw shadow/outline for better visibility
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x + 2, y + 2);

        // Draw main text
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x, y);
    }


    /**
     * Highlight square selected and available moves.
     */
    private void highlightSquares(Graphics g, GameState gs, ArrayList<Move> validMoves, Square sqSelected) {
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