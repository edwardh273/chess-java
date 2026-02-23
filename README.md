# Java Chess Engine

A fully-functional chess game built with Java Swing, featuring both human and AI gameplay with a sophisticated move validation engine.

## Features

- Complete chess rules implementation (castling, en passant, pawn promotion, check/checkmate)
- AI opponent using Negamax algorithm with alpha-beta pruning
- Interactive GUI with move highlighting
- Multiple game modes: Human vs AI, or AI vs AI
- Move undo functionality (press 'Z')
- Legal move generation with pin and check detection

## Requirements

- **OpenJDK 25** or compatible JDK

## Building the Project

Build the project using the Gradle wrapper:

```bash
./gradlew build
```

This will compile the source code and generate a JAR file in `build/libs/`.

## Running the Game

Run the generated JAR file:

```bash
java -jar build/libs/chess-java-1.0.jar
```

## How to Play

- **Click** on a piece to select it (valid moves will be highlighted in yellow)
- **Click** on a highlighted square to move the piece
- **Press 'Z'** to undo the last move
- The game detects checkmate and stalemate automatically

## Game Modes

By default, the game is configured for AI vs AI. To modify game modes, edit the `whitePlayer` and `blackPlayer` boolean flags in `BoardPanel.java`:

- `whitePlayer = true` - Human plays as White
- `blackPlayer = true` - Human plays as Black
- Both `false` - AI vs AI (automatic play)

## AI Strength

- White AI: 5-ply search depth
- Black AI: 3-ply search depth

The AI evaluates positions based on material advantage and piece positioning.
