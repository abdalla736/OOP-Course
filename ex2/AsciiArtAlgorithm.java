package ascii_art;

import image.Image;
import image.ImageUses;
import image_char_matching.SubImgCharMatcher;

import static image.ImageUses.getSubImage;

/**
 * Runs a single execution of the ASCII-art algorithm.
 *
 * <p>This class is responsible for converting an {@link image.Image} into a 2D char matrix
 * (ASCII art) according to the current resolution and character-set matcher.</p>
 *
 * <p>High-level flow inside {@link #run()}:
 * <ol>
 *   <li>Pad the image to power-of-two dimensions (if needed).</li>
 *   <li>Split the padded image into square sub-images according to {@code resolution}
 *       (resolution = number of sub-images / ASCII chars per output row).</li>
 *   <li>Compute brightness for each sub-image (normalized to [0,1]).</li>
 *   <li>Use {@link SubImgCharMatcher} to map each sub-image brightness to the closest ASCII char.</li>
 * </ol>
 * </p>
 *
 * <p>Note: A new instance of {@code AsciiArtAlgorithm} is typically created per run, while the
 * {@link SubImgCharMatcher} is reused across runs to avoid recomputing charset brightness.</p>
 */
public class AsciiArtAlgorithm {

    /**
     * The source image to be converted to ASCII art (before padding).
     */
    private final Image image;

    /**
     * Resolution = number of sub-images (ASCII characters) per output row.
     * This value determines the sub-image size when splitting.
     */
    private final int resolution;

    /**
     * Matches an input brightness value (of a sub-image) to the closest ASCII character
     * (based on precomputed charset brightness).
     */
    private final SubImgCharMatcher charMatcher;

    /**
     * Constructs a single-run ASCII-art algorithm instance.
     *
     * @param image       the source image to convert
     * @param resolution  number of sub-images (ASCII chars) per output row
     * @param charMatcher matcher that maps brightness values to characters
     */
    public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher charMatcher) {
        this.image = image;
        this.resolution = resolution;
        this.charMatcher = charMatcher;
    }

    /**
     * Executes the ASCII-art algorithm once and returns the produced ASCII-art grid.
     *
     * <p>The returned matrix dimensions correspond to:
     * <ul>
     *   <li>rows: number of sub-image rows in the padded image</li>
     *   <li>cols: {@code resolution} (sub-images per row)</li>
     * </ul>
     * Each cell contains a single ASCII character.</p>
     *
     * @return a 2D array of ASCII characters representing the image
     */
    public char[][] run() {
        // Step 1: pad the image to the nearest powers of two (if required).
        Image imageAfterPadding = ImageUses.padImageToPowerOfTwo(image);

        // Step 2: split to square sub-images; resolution determines how many sub-images per row.
        Image[][] subImages = getSubImage(imageAfterPadding, resolution);

        // Output matrix: same grid shape as the sub-images grid.
        char[][] result = new char[subImages.length][subImages[0].length];

        // Step 3: for each sub-image, compute brightness and map it to the closest ASCII character.
        for (int i = 0; i < subImages.length; i++) {
            for (int j = 0; j < subImages[0].length; j++) {
                double brightness = ImageUses.calculateSubImageBrightness(subImages[i][j]);
                result[i][j] = charMatcher.getCharByImageBrightness(brightness);
            }
        }

        return result;
    }
}
