package com.edwardhicks.chess.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.edwardhicks.chess.Constants.SQ_SIZE;

public class ImageLoader {
    private static final Map<String, Image> IMAGES = new HashMap<>();

    static {
        loadImages();
    }

    private static void loadImages() {
        String[] pieces = {"bR", "bN", "bB", "bQ", "bK", "bp",
                          "wp", "wR", "wN", "wB", "wQ", "wK"};

        for (String piece : pieces) {
            try {
                // Load from resources folder
                BufferedImage img = ImageIO.read(
                    ImageLoader.class.getResourceAsStream("/images/" + piece + ".png")
                );

                // Scale image
                Image scaledImg = img.getScaledInstance(SQ_SIZE, SQ_SIZE, Image.SCALE_SMOOTH);
                IMAGES.put(piece, scaledImg);

            } catch (IOException e) {
                System.err.println("Error loading image: " + piece);
            }
        }
    }

    public static Image getPieceImage(String piece) {
        return IMAGES.get(piece);
    }
}