package image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Represents an RGB image as a 2D grid of {@link java.awt.Color} pixels.
 *
 * <p>This class is used throughout the ASCII-art project as the main image container.
 * The internal storage is a {@code Color[][]} where the first index represents the row
 * (y coordinate) and the second index represents the column (x coordinate).</p>
 *
 * <p><b>Important note about indexing:</b>
 * This implementation stores pixels as {@code pixelArray[row][col]} and the method
 * {@link #getPixel(int, int)} accesses them as {@code pixelArray[x][y]}.
 * In other words, the naming in {@code getPixel(x, y)} is somewhat misleading, because
 * the first argument is treated like a row index and the second like a column index.</p>
 *
 * <p>This class also provides a helper method {@link #saveImage(String)} to export the image
 * into a JPEG file (used mainly for debugging / testing).</p>
 *
 * @author Dan Nirel
 */
public class Image {

    /**
     * Pixel grid of this image.
     * The array is indexed as pixelArray[row][col] in the constructors.
     */
    private final Color[][] pixelArray;

    /** Image width in pixels (number of columns). */
    private final int width;

    /** Image height in pixels (number of rows). */
    private final int height;

    /**
     * Loads an image from disk and stores its pixels in a {@link Color} matrix.
     *
     * <p>The underlying file is read using {@link ImageIO#read(File)}.
     * Pixels are copied into {@code pixelArray} such that:</p>
     *
     * <pre>
     * pixelArray[row][col] = color at (col, row) in the source BufferedImage
     * </pre>
     *
     * @param filename path to an image file supported by ImageIO (e.g., jpg/png/jpeg)
     * @throws IOException if the file cannot be read or decoded
     */
    public Image(String filename) throws IOException {
        BufferedImage im = ImageIO.read(new File(filename));
        width = im.getWidth();
        height = im.getHeight();

        // Store pixels as [row][col] = [y][x]
        pixelArray = new Color[height][width];
        for (int i = 0; i < height; i++) {          // i = row (y)
            for (int j = 0; j < width; j++) {       // j = col (x)
                pixelArray[i][j] = new Color(im.getRGB(j, i));
            }
        }
    }

    /**
     * Constructs an Image from an already-prepared pixel array.
     *
     * <p>This is mainly used by utility methods (padding, sub-image extraction) that create
     * new images from existing images.</p>
     *
     * @param pixelArray pixel grid (typically [height][width])
     * @param width      width in pixels (columns)
     * @param height     height in pixels (rows)
     */
    public Image(Color[][] pixelArray, int width, int height) {
        this.pixelArray = pixelArray;
        this.width = width;
        this.height = height;
    }

    /**
     * @return image width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return image height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns a pixel color from the internal pixel grid.
     *
     * <p><b>Indexing caveat:</b> this method returns {@code pixelArray[x][y]}.
     * Given how the array is filled in the constructor, the first index behaves like "row"
     * and the second behaves like "column".</p>
     *
     * @param x first index into {@code pixelArray} (acts like row / y in this implementation)
     * @param y second index into {@code pixelArray} (acts like column / x in this implementation)
     * @return the {@link Color} at the requested indices
     */
    public Color getPixel(int x, int y) {
        return pixelArray[x][y];
    }

    /**
     * Saves this image as a JPEG file on disk.
     *
     * <p>The method reconstructs a {@link BufferedImage} from {@code pixelArray},
     * then writes it using {@link ImageIO#write(java.awt.image.RenderedImage, String, File)}.</p>
     *
     * <p>The output file will be named {@code fileName + ".jpeg"}.</p>
     *
     * @param fileName output filename prefix (without extension)
     * @throws RuntimeException if writing fails (wraps IOException)
     */
    public void saveImage(String fileName) {
        // Create a BufferedImage with matching dimensions.
        BufferedImage bufferedImage = new BufferedImage(
                pixelArray[0].length,   // width = number of columns
                pixelArray.length,      // height = number of rows
                BufferedImage.TYPE_INT_RGB
        );

        // Copy pixels from Color[][] into BufferedImage coordinates.
        for (int x = 0; x < pixelArray.length; x++) {      // x behaves like row index
            for (int y = 0; y < pixelArray[x].length; y++) { // y behaves like col index
                bufferedImage.setRGB(y, x, pixelArray[x][y].getRGB());
            }
        }

        File outputfile = new File(fileName + ".jpeg");
        try {
            ImageIO.write(bufferedImage, "jpeg", outputfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
