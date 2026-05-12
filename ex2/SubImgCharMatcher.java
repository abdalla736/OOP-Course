package image_char_matching;

import java.util.HashMap;

/**
 * Matches a given sub-image brightness value to the closest ASCII character
 * from a configurable character set (charset).
 *
 * <p>This class supports:
 * <ul>
 *   <li>Computing <b>raw brightness</b> for each character in the charset using {@link CharConverter}.</li>
 *   <li>Normalizing those brightness values into [0,1] across the current charset.</li>
 *   <li>Given a target brightness (computed from a sub-image), returning the closest character.</li>
 *   <li>Dynamically adding/removing characters from the charset, updating brightness structures.</li>
 * </ul>
 * </p>
 *
 * <p><b>Brightness definition (exercise):</b>
 * Character brightness is calculated from a 16x16 boolean matrix:
 * <pre>
 * brightness(c) = (# of true pixels) / 256
 * </pre>
 * Then brightness values are normalized across the charset:
 * <pre>
 * normalized = (raw - rawMin) / (rawMax - rawMin)
 * </pre>
 * </p>
 *
 * <p><b>Note:</b> This class stores two maps:
 * <ul>
 *   <li>{@link #rawCharsetBrightness}: raw brightness per character</li>
 *   <li>{@link #normalizedCharsetBrightness}: normalized brightness per character</li>
 * </ul>
 * This allows efficient matching during ASCII-art generation.</p>
 */
public class SubImgCharMatcher {

    /**
     * Default initial value for normalized max brightness (used when recomputing).
     * A value < 0 ensures any computed brightness will be larger.
     */
    private static final double DEFAULT_NORMALIZED_MAX_BRIGHTNESS = -1;

    /**
     * Default initial value for normalized min brightness (used when recomputing).
     * A value > 1 ensures any computed brightness will be smaller.
     */
    private static final double DEFAULT_NORMALIZED_MIN_BRIGHTNESS = 2;

    /** Current character set used for matching. */
    private char[] charset;

    /**
     * Map of character -> normalized brightness in [0,1] (relative to the current charset).
     */
    private final HashMap<Character, Double> normalizedCharsetBrightness;

    /**
     * Map of character -> raw brightness (#true / 256) computed from the 16x16 bitmap.
     */
    private final HashMap<Character, Double> rawCharsetBrightness;

    /** Maximum raw brightness among all characters currently in charset. */
    private double rawMaxBrightness;

    /** Minimum raw brightness among all characters currently in charset. */
    private double rawMinBrightness;

    /** Maximum normalized brightness among characters currently in charset. */
    private double normalizedMaxBrightness;

    /** Minimum normalized brightness among characters currently in charset. */
    private double normalizedMinBrightness;

    /**
     * Constructs a matcher with an initial charset.
     *
     * <p>This constructor:
     * <ol>
     *   <li>Computes raw brightness for each character in {@code charset}.</li>
     *   <li>Finds raw min/max brightness values.</li>
     *   <li>Normalizes all brightness values into [0,1].</li>
     * </ol>
     * </p>
     *
     * @param charset initial character set for ASCII conversion
     */
    public SubImgCharMatcher(char[] charset) {
        this.charset = charset;
        this.normalizedCharsetBrightness = new HashMap<Character, Double>();
        this.rawCharsetBrightness = new HashMap<Character, Double>();

        // Initialize using the first character as baseline.
        double b0 = charBrightnessTemp(0);
        this.rawCharsetBrightness.put(charset[0], b0);
        this.rawMaxBrightness = b0;
        this.rawMinBrightness = b0;

        // Initialize normalized bounds to extreme defaults.
        this.normalizedMaxBrightness = DEFAULT_NORMALIZED_MAX_BRIGHTNESS;
        this.normalizedMinBrightness = DEFAULT_NORMALIZED_MIN_BRIGHTNESS;

        // Compute raw brightness for all remaining characters and update raw bounds.
        for (int i = 1; i < charset.length; i++) {
            double bi = charBrightnessTemp(i);
            this.rawCharsetBrightness.put(charset[i], bi);
            this.rawMaxBrightness = Math.max(rawMaxBrightness, bi);
            this.rawMinBrightness = Math.min(rawMinBrightness, bi);
        }

        // Compute normalized brightness values for the entire charset.
        finalCharBrightness();
    }

    /**
     * Returns the character whose <b>normalized brightness</b> is closest to the given brightness.
     *
     * <p>The input brightness is expected to be in [0,1] (brightness of a sub-image).</p>
     *
     * @param brightness target brightness value (typically brightness of a sub-image)
     * @return the closest matching character from the charset
     */
    public char getCharByImageBrightness(double brightness) {
        char closestChar = charset[0];
        double closestBrightness = Math.abs(
                normalizedCharsetBrightness.get(closestChar) - brightness);

        for (int i = 1; i < charset.length; i++) {
            char charTemp = charset[i];
            double diff = Math.abs(brightness - normalizedCharsetBrightness.get(charTemp));
            if (diff < closestBrightness) {
                closestBrightness = diff;
                closestChar = charTemp;
            }
        }
        return closestChar;
    }

    /**
     * @return the current charset as a char array
     */
    public char[] getCharset() {
        return charset;
    }

    /**
     * @return maximum normalized brightness among current characters
     */
    public double getMaxBrightness() {
        return normalizedMaxBrightness;
    }

    /**
     * @return minimum normalized brightness among current characters
     */
    public double getMinBrightness() {
        return normalizedMinBrightness;
    }

    /**
     * Adds a character to the charset (if not already present), and updates internal brightness maps.
     *
     * <p>Steps:
     * <ol>
     *   <li>If {@code c} already exists, do nothing.</li>
     *   <li>Extend the charset array and append {@code c}.</li>
     *   <li>Compute raw brightness for {@code c} and update raw min/max.</li>
     *   <li>Recompute normalized brightness for all characters (via {@link #finalCharBrightness()}).</li>
     * </ol>
     * </p>
     *
     * @param c the character to add
     */
    public void addChar(char c) {
        // Avoid duplicates.
        for (char value : charset) {
            if (value == c) {
                return;
            }
        }

        // Expand charset array by 1 and append new char.
        char[] original = charset;
        this.charset = new char[this.charset.length + 1];
        System.arraycopy(original, 0, this.charset, 0, original.length);

        int newCharIndex = this.charset.length - 1;
        this.charset[newCharIndex] = c;

        // Compute raw brightness for new char and store.
        double brightnessTempC = charBrightnessTemp(newCharIndex);
        this.rawCharsetBrightness.put(c, brightnessTempC);

        // Update raw bounds.
        this.rawMaxBrightness = Math.max(this.rawMaxBrightness, brightnessTempC);
        this.rawMinBrightness = Math.min(this.rawMinBrightness, brightnessTempC);

        // Compute preliminary normalized brightness for new char
        // (then recompute all in finalCharBrightness).
        double newCharBrightness = (brightnessTempC - this.rawMinBrightness) /
                (this.rawMaxBrightness - this.rawMinBrightness);
        this.normalizedCharsetBrightness.put(c, newCharBrightness);

        // Recompute normalized brightness for entire charset and update normalized bounds.
        finalCharBrightness();
    }

    /**
     * Removes a character from the charset (if present), and updates brightness maps and bounds.
     *
     * <p>Steps:
     * <ol>
     *   <li>If charset is empty or {@code c} is not present, do nothing.</li>
     *   <li>Build a new charset array without {@code c}.</li>
     *   <li>Remove {@code c} from both raw and normalized maps.</li>
     *   <li>If {@code c} affected min/max brightness, recompute bounds and re-normalize.</li>
     * </ol>
     * </p>
     *
     * @param c the character to remove
     */
    public void removeChar(char c) {
        if (charset.length == 0) {
            return;
        }

        // Check if c is present.
        boolean cInCharSet = false;
        for (char value : charset) {
            if (value == c) {
                cInCharSet = true;
                break;
            }
        }
        if (!cInCharSet) {
            return;
        }

        int charSetLength = this.charset.length;
        char[] CharSetWithoutC = new char[charSetLength - 1];

        // Copy all chars except c.
        for (int i = 0, j = 0; i < charSetLength; i++) {
            if (this.charset[i] == c) {
                continue;
            }
            CharSetWithoutC[j++] = this.charset[i];
        }

        // Replace charset with the reduced version.
        this.charset = CharSetWithoutC;

        // Save old brightness values of c before removal (used for boundary checks).
        double cRawBrightness = rawCharsetBrightness.get(c);
        double cNormalizedBrightness = normalizedCharsetBrightness.get(c);

        // Remove c from maps.
        normalizedCharsetBrightness.remove(c);
        rawCharsetBrightness.remove(c);

        if (this.charset.length != 0) {
            // If c was at raw min/max, recompute raw bounds from scratch.
            if (rawMaxBrightness == cRawBrightness || rawMinBrightness == cRawBrightness) {
                modifyMaxMinBrightness();
            }

            // If c was at normalized min/max, reset normalized bounds to defaults
            // before recomputing them during finalCharBrightness().
            if (normalizedMaxBrightness == cNormalizedBrightness ||
                    normalizedMinBrightness == cNormalizedBrightness) {
                normalizedMaxBrightness = DEFAULT_NORMALIZED_MAX_BRIGHTNESS;
                normalizedMinBrightness = DEFAULT_NORMALIZED_MIN_BRIGHTNESS;
            }

            // Recompute normalized brightness and normalized bounds.
            finalCharBrightness();
        } else {
            // Charset became empty.
            this.rawMaxBrightness = 0;
            this.rawMinBrightness = 0;
        }
    }

    /**
     * Recomputes raw min/max brightness across the current charset using {@link #rawCharsetBrightness}.
     *
     * <p>This is called when the removed character previously defined the min or max raw brightness.</p>
     */
    private void modifyMaxMinBrightness() {
        char firstChar = this.charset[0];
        double maxBrightnessTemp = rawCharsetBrightness.get(firstChar);
        double minBrightnessTemp = rawCharsetBrightness.get(firstChar);

        for (int i = 1; i < charset.length; i++) {
            char currentChar = charset[i];
            if (rawCharsetBrightness.get(currentChar) > maxBrightnessTemp) {
                maxBrightnessTemp = rawCharsetBrightness.get(currentChar);
            }
            if (rawCharsetBrightness.get(currentChar) < minBrightnessTemp) {
                minBrightnessTemp = rawCharsetBrightness.get(currentChar);
            }
        }
        this.rawMaxBrightness = maxBrightnessTemp;
        this.rawMinBrightness = minBrightnessTemp;
    }

    /**
     * Computes raw brightness of the character at {@code charset[index]}.
     *
     * <p>The character is converted to a 16x16 boolean bitmap via
     * {@link CharConverter#convertToBoolArray(char)} and brightness is computed as:</p>
     *
     * <pre>
     * (# of true pixels) / 256
     * </pre>
     *
     * @param index index of character in {@link #charset}
     * @return raw brightness value in [0,1]
     */
    private double charBrightnessTemp(int index) {
        boolean[][] charBrightness = CharConverter.convertToBoolArray(charset[index]);
        int pixelCount = 0;

        // DEFAULT_PIXEL_RESOLUTION is 16 in this exercise.
        for (int r = 0; r < 16; r++) {
            for (int c = 0; c < 16; c++) {
                if (charBrightness[r][c]) {
                    pixelCount++;
                }
            }
        }
        return (double) pixelCount / 256;
    }

    /**
     * Recomputes normalized brightness values for the entire charset.
     *
     * <p>Normalization uses current {@link #rawMinBrightness} and {@link #rawMaxBrightness}:</p>
     *
     * <pre>
     * normalized = (raw - rawMin) / (rawMax - rawMin)
     * </pre>
     *
     * <p>This method also updates {@link #normalizedMaxBrightness} and
     * {@link #normalizedMinBrightness} while iterating.</p>
     *
     * <p>If {@code rawMaxBrightness == rawMinBrightness}, all normalized values become 0.0
     * (to avoid division by zero), and both normalized bounds become 0.0.</p>
     */
    private void finalCharBrightness() {
        if (rawMaxBrightness != rawMinBrightness) {
            for (char c : charset) {
                double newCharBrightness = (this.rawCharsetBrightness.get(c) - this.rawMinBrightness) /
                        (this.rawMaxBrightness - this.rawMinBrightness);
                this.normalizedCharsetBrightness.put(c, newCharBrightness);

                this.normalizedMaxBrightness = Math.max(
                        this.normalizedMaxBrightness, newCharBrightness);
                this.normalizedMinBrightness = Math.min(
                        this.normalizedMinBrightness, newCharBrightness);
            }
        } else {
            // All characters have the same raw brightness.
            for (char c : charset) {
                double newCharBrightness = 0.0;
                this.normalizedCharsetBrightness.put(c, newCharBrightness);

                this.normalizedMaxBrightness = newCharBrightness;
                this.normalizedMinBrightness = newCharBrightness;
            }
        }
    }
}
