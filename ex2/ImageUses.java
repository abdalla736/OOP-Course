package image;

import java.awt.*;

/**
 * Utility methods for image processing used by the ASCII-art algorithm.
 *
 * <p>This class provides:
 * <ul>
 *   <li><b>Padding</b>: pad an image to dimensions that are powers of two (centered).</li>
 *   <li><b>Splitting</b>: split an image into square sub-images according to a given resolution.</li>
 *   <li><b>Brightness</b>: compute normalized brightness of an image/sub-image using luminance
 *       coefficients.</li>
 * </ul>
 * </p>
 *
 * <p><b>Terminology used in the exercise:</b>
 * <ul>
 *   <li><b>Resolution</b> = number of sub-images (ASCII characters) per output row.</li>
 *   <li><b>Sub-image</b> = a square block of pixels extracted from the padded image.</li>
 * </ul>
 * </p>
 */
public class ImageUses {

    /**
     * Constant white color used for padding background pixels.
     */
    public static final Color white = new Color(255, 255, 255);

    /**
     * Luminance coefficients (standard linear RGB → grayscale approximation).
     * Used to compute grayscale brightness for each pixel.
     */
    private static final double RED_FACTOR = 0.2126;
    private static final double GREEN_FACTOR = 0.7152;
    private static final double BLUE_FACTOR = 0.0722;
    private static final int RGB_MAX_VALUE = 255;

    /**
     * Pads an image so that its width and height become powers of two.
     *
     * <p>The padded image is filled with white background and the original image is placed
     * in the center (equal padding on all sides, with integer division behavior).</p>
     *
     * <p>If the image is already power-of-two in both dimensions, it is returned unchanged.</p>
     *
     * @param image source image
     * @return padded image whose dimensions are powers of two
     */
    public static Image padImageToPowerOfTwo(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int closestPowerWidth = closestPowerOfTwoFromAbove(width);
        int closestPowerHeight = closestPowerOfTwoFromAbove(height);

        // No padding needed if already powers of two.
        if (closestPowerWidth == width && closestPowerHeight == height) {
            return image;
        }

        // Create new pixel grid (initialized to white).
        Color[][] newPixels = new Color[closestPowerHeight][closestPowerWidth];
        for (int i = 0; i < closestPowerHeight; i++) {
            for (int j = 0; j < closestPowerWidth; j++) {
                newPixels[i][j] = white;
            }
        }

        // Compute top/left padding so the original is centered.
        int padCol = (closestPowerWidth - width) / 2;
        int padRow = (closestPowerHeight - height) / 2;

        // Copy the original image pixels into the center of the new padded image.
        for (int i = 0; i < height; i++) {       // i = row in original
            for (int j = 0; j < width; j++) {    // j = col in original
                newPixels[padRow + i][padCol + j] = image.getPixel(i, j);
            }
        }

        return new Image(newPixels, closestPowerWidth, closestPowerHeight);
    }

    /**
     * Computes the smallest power of two that is greater than or equal to {@code value}.
     *
     * @param value positive integer
     * @return the nearest power of two >= value
     */
    private static int closestPowerOfTwoFromAbove(int value) {
        int power = 1;
        while (power < value) {
            power *= 2;
        }
        return power;
    }

    /**
     * Splits an image into square sub-images according to the given resolution.
     *
     * <p>Resolution is interpreted as:
     * <b>the number of sub-images (ASCII characters) per output row</b>.</p>
     *
     * <p>Given:
     * <ul>
     *   <li>{@code subImageSize = image.getWidth() / resolution}</li>
     * </ul>
     * This method creates {@code resolution} sub-images per row, each of size
     * {@code subImageSize x subImageSize}, and stacks as many rows of sub-images as needed
     * to cover the image height.</p>
     *
     * <p>Assumption (allowed by the exercise): the resolution is chosen such that the image
     * can be perfectly divided into these square blocks.</p>
     *
     * @param image      image to split (typically already padded)
     * @param resolution number of sub-images per row
     * @return a 2D array of sub-images indexed as [rowOfBlocks][colOfBlocks]
     */
    public static Image[][] getSubImage(Image image, int resolution) {

        // Width/height of each square sub-image in pixels.
        int subImagesWidth = image.getWidth() / resolution;

        // Number of rows of blocks needed to cover the image height.
        int numRows = image.getHeight() / subImagesWidth;

        Image[][] subImages = new Image[numRows][resolution];

        // For each block position (i,j), copy its pixels into a new Image.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < resolution; j++) {

                // Top-left pixel of this sub-image block within the original image.
                int currentRow = i * subImagesWidth;
                int currentCol = j * subImagesWidth;

                Color[][] subImagePixel = new Color[subImagesWidth][subImagesWidth];

                // Copy pixels from original image to the block pixel array.
                for (int k = 0; k < subImagesWidth; k++) {
                    for (int l = 0; l < subImagesWidth; l++) {
                        subImagePixel[k][l] = image.getPixel(currentRow + k, currentCol + l);
                    }
                }

                subImages[i][j] = new Image(subImagePixel, subImagesWidth, subImagesWidth);
            }
        }
        return subImages;
    }

    /**
     * Computes normalized brightness of an image/sub-image.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>For each pixel, compute grayscale luminance:
     *       {@code gray = 0.2126*R + 0.7152*G + 0.0722*B}</li>
     *   <li>Average over all pixels.</li>
     *   <li>Normalize by dividing by {@code 255} so result is in [0,1].</li>
     * </ol>
     * </p>
     *
     * @param image image/sub-image to evaluate
     * @return average brightness in range [0, 1]
     */
    public static double calculateSubImageBrightness(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double greyPixels = 0.0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = image.getPixel(i, j);
                greyPixels += c.getRed() * RED_FACTOR +
                        c.getGreen() * GREEN_FACTOR +
                        c.getBlue() * BLUE_FACTOR;
            }
        }

        // Normalize by number of pixels and by the maximum channel value (255).
        return greyPixels / (width * height * RGB_MAX_VALUE);
    }
}
