package org.example.sqleksamenhelper;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ImagePreprocessor {

    /**
     * Preprocess the image to improve OCR accuracy.
     * This includes converting to grayscale, increasing contrast, and binarization.
     *
     * @param original The original BufferedImage.
     * @return The preprocessed BufferedImage.
     */
    public static BufferedImage preprocessImage(BufferedImage original) {
        // Convert to grayscale
        BufferedImage grayscale = new BufferedImage(
                original.getWidth(), original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // Increase contrast and binarize
        BufferedImage binarized = new BufferedImage(
                grayscale.getWidth(), grayscale.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binarized.createGraphics();
        g.drawImage(grayscale, 0, 0, null);
        g.dispose();

        return binarized;
    }
}
