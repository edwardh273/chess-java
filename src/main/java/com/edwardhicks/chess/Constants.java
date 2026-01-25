package com.edwardhicks.chess;

import java.awt.Color;

public class Constants {

    // Square window dimensions
    public static final int BOARD_LENGTH = 768;

    // Fit board to window
    public static final int DIMENSION = 8;
    public static final int SQ_SIZE = BOARD_LENGTH / DIMENSION;

    // Set square colors
    public static final Color LIGHT_SQUARE = new Color(240, 240, 240);
    public static final Color DARK_SQUARE = new Color(100, 100, 100);
    public static final Color BLUE_TRANSPARENT = new Color(0, 0, 255, 100);
    public static final Color YELLOW_TRANSPARENT = new Color(255, 255, 0, 100);

}
