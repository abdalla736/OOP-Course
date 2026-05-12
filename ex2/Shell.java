package ascii_art;

import Exceptions.InvalidInput;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import image.ImageUses;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

import static image.ImageUses.getSubImage;

/**
 * An interactive command-line shell for the ASCII-art generator.
 *
 * <p>The shell is responsible for:
 * <ul>
 *   <li>Loading an input image (once, when {@link #run(String)} is called).</li>
 *   <li>Maintaining the current state of the program: resolution, charset, and output method.</li>
 *   <li>Reading user commands, validating them, and executing the requested operation.</li>
 * </ul>
 * </p>
 *
 * <p>Supported commands (based on the constants used in this class):
 * <ul>
 *   <li><b>chars</b> - prints the current character set.</li>
 *   <li><b>add X</b> / <b>remove X</b> - add/remove a single character.</li>
 *   <li><b>add a-z</b> / <b>remove a-z</b> - add/remove a character range.</li>
 *   <li><b>add all</b> / <b>remove all</b> - add/remove all printable ASCII characters.</li>
 *   <li><b>add space</b> / <b>remove space</b> - add/remove the space character.</li>
 *   <li><b>res</b> - prints the current resolution.</li>
 *   <li><b>res up</b> / <b>res down</b> - changes the resolution within allowed boundaries.</li>
 *   <li><b>output console</b> / <b>output html</b> - changes output method.</li>
 *   <li><b>asciiArt</b> - runs the ASCII-art algorithm and outputs the result.</li>
 *   <li><b>reverse</b> - runs ASCII-art with inverted brightness mapping and outputs the result.</li>
 *   <li><b>exit</b> - exits the shell.</li>
 * </ul>
 * </p>
 *
 * <p><b>Whitespace handling:</b>
 * Commands may include multiple spaces between tokens. The shell normalizes the input by
 * trimming and replacing consecutive spaces with a single space before parsing.</p>
 */
public class Shell {

    /* ===========================
       Default values and messages
       =========================== */

    /**
     * Default resolution. In this implementation, resolution represents the number of sub-images
     * (ASCII characters) per output row.
     */
    private static final int DEFAULT_RESOLUTION = 2;

    /** Message printed when image loading fails. */
    private static final String IMAGE_ERROR_PATH = "There was an error loading the image.";

    /** Command prefix / exact command to exit the shell loop. */
    private static final String EXIT_KEY = "exit ";
    private static final String ONLY_EXIT_KEY = "exit";

    /** Error message for invalid "add" format. */
    private static final String ADD_INCORRECT_FORM_ERROR =
            "Did not add due to incorrect format.";

    /** Error message for invalid "remove" format. */
    private static final String REMOVE_INCORRECT_FORM_ERROR =
            "Did not remove due to incorrect format.";

    /** Keywords used in add/remove commands. */
    private static final String ADD_OR_REMOVE_SPACE = "space ";
    private static final String ADD_OR_REMOVE_ONLY_SPACE = "space";
    private static final String ADD_OR_REMOVE_ALL = "all ";
    private static final String ADD_OR_REMOVE_ONLY_ALL = "all";

    /** Error messages for resolution changes. */
    private static final String RESOLUTION_CHANGE_INCORRECT_FORM =
            "Did not change resolution due to incorrect format.";
    private static final String EXCEEDING_BOUNDARIES_RESOLUTION_CHANGE =
            "Did not change resolution due to exceeding boundaries.";

    /** Keywords for "res up" and "res down". */
    private static final String RES_DOWN_KEY = "down ";
    private static final String RES_DOWN_ONLY_KEY = "down";
    private static final String RES_UP_KEY = "up ";
    private static final String RES_UP_ONLY_KEY = "up";

    /** Error message for executing asciiArt with too small charset. */
    private static final String SMALL_NUMBER_OF_CHARSET = "Did not execute. Charset is too small.";

    /** Error message for invalid output change. */
    private static final String OUTPUT_CHANGE_INCORRECT_FORM =
            "Did not change output method due to incorrect format.";

    /** Keywords for output selection. */
    private static final String OUTPUT_CONSOLE_KEY = "console ";
    private static final String OUTPUT_CONSOLE_ONLY_KEY = "console";
    private static final String OUTPUT_HTML_KEY = "html ";
    private static final String OUTPUT_HTML_ONLY_KEY = "html";

    /** HTML output settings. */
    private static final String FILE_NAME_HTML = "out.html";
    private static final String FONT_NAME_HTML = "Courier New";

    /**
     * Index in the string where the second argument of "output ..." starts.
     * "output " is 7 characters long.
     */
    private static final int OUTPUT_SECOND_ARG_BEGINNING = 7;

    /** Output command name without arguments. */
    private static final String OUTPUT_ONLY_COMMAND = "output";

    /** Generic error for unknown commands. */
    private static final String INCORRECT_COMMAND = "Did not execute due to incorrect command.";

    /** "chars" command. */
    private static final String CHARS_COMMAND = "chars ";
    private static final String CHARS_COMMAND_ONLY = "chars";

    /** "add" and "remove" commands. */
    private static final String ADD_ONLY_COMMAND = "add";
    private static final String REMOVE_ONLY_COMMAND = "remove";
    private static final String ADD_COMMAND = "add ";
    private static final String REMOVE_COMMAND = "remove ";

    /** "res" command and resolution printing prefix. */
    private static final String RES_COMMAND = "res ";
    private static final String RES_ONLY_COMMAND = "res";
    private static final String RES_PRINT = "Resolution set to ";

    /** "reverse" command. */
    private static final String REVERSE_COMMAND = "reverse ";
    private static final String REVERSE_ONLY_COMMAND = "reverse";

    /** "output" command prefix. */
    private static final String OUTPUT_COMMAND = "output ";

    /** "asciiArt" command. */
    private static final String RUN_ASCII_ART_COMMAND = "asciiArt ";
    private static final String RUN_ASCII_ART_ONLY_COMMAND = "asciiArt";

    private static final int DEFAULT_CHAR_SET_LENGTH = 10;
    private static final int ZERO_ASCII_VALUE = 48;
    private static final int NINE_ASCII_VALUE = 57;
    private static final int MINIMAL_CHARSET_LENGTH_TO_RUN_ASCI_IART = 2;
    private static final double FULL_BRIGHTNESS = 1.0;
    private static final int FIRST_ASCII_VALUE = 32;
    private static final int LAST_ASCII_VALUE = 126;

    /* ===========================
       Shell state (mutable fields)
       =========================== */

    /**
     * Maintains a character set and provides brightness-to-character matching.
     * It is initialized with the digits '0'..'9' by default.
     */
    private final SubImgCharMatcher subImgCharMatcher;

    /** The image currently loaded for this shell session. */
    private Image image;

    /**
     * Current resolution (number of sub-images / ASCII characters per output row).
     */
    private int resolution;

    /**
     * Maximum allowed resolution (derived from the image width each time resolution changes).
     */
    private int maxResolution;

    /**
     * Minimum allowed resolution (derived from the image aspect ratio each time resolution changes).
     */
    private int minResolution;

    /**
     * Stores the most recently produced ASCII output, so it can be re-output if needed.
     */
    private char[][] updatedImage;

    /** Current output strategy (console or HTML). */
    private AsciiOutput output;

    /**
     * Constructs a new Shell with default settings:
     * <ul>
     *   <li>Charset is digits '0'..'9'</li>
     *   <li>Resolution = {@link #DEFAULT_RESOLUTION}</li>
     *   <li>Output = {@link ConsoleAsciiOutput}</li>
     * </ul>
     */
    public Shell() {

        // Default charset: digits '0'..'9' (ASCII codes 48..57).
        char[] charsetAscii = new char[DEFAULT_CHAR_SET_LENGTH];
        for (int i = ZERO_ASCII_VALUE, c = 0; i <= NINE_ASCII_VALUE; i++, c++) {
            charsetAscii[c] = (char) i;
        }

        this.subImgCharMatcher = new SubImgCharMatcher(charsetAscii);

        this.resolution = DEFAULT_RESOLUTION;
        maxResolution = 0;
        minResolution = 0;

        // Default output method: console.
        output = new ConsoleAsciiOutput();
    }

    /**
     * Runs the interactive shell for a given image file.
     *
     * <p>This method:
     * <ol>
     *   <li>Loads the image from disk.</li>
     *   <li>Starts an input loop reading commands.</li>
     *   <li>Normalizes whitespace (multiple spaces are allowed).</li>
     *   <li>Validates and executes commands.</li>
     *   <li>Stops when the user enters "exit".</li>
     * </ol>
     * </p>
     *
     * @param imageName the image filename/path to load (e.g., "cat.jpeg")
     */
    public void run(String imageName) {
        try {
            image = new Image(imageName);
        } catch (IOException e) {
            System.out.println(IMAGE_ERROR_PATH);
            return;
        }

        // Initial prompt + read first command.
        System.out.print(">>> ");
        String inputString = KeyboardInput.readLine();

        // Main REPL loop: read-eval-print until exit.
        while (!checkStartsEquals(inputString, EXIT_KEY, ONLY_EXIT_KEY)) {
            try {
                // Normalize spaces:
                // 1) trim leading/trailing spaces
                // 2) collapse multiple spaces into a single space
                inputString = inputString.trim();
                while (inputString.contains("  ")) {
                    inputString = inputString.replace("  ", " ");
                }

                // Parse and execute the command.
                checkInputString(inputString);

            } catch (InvalidInput e) {
                // All parsing/validation errors funnel into this catch.
                System.out.println(e.getMessage());
            }

            // Prompt for next command.
            System.out.print(">>> ");
            inputString = KeyboardInput.readLine();
        }
    }

    /**
     * Parses and executes a single command string.
     *
     * <p>The command is assumed to already be whitespace-normalized by {@link #run(String)}.</p>
     *
     * @param inputString the normalized command line
     * @throws InvalidInput if the command is unknown or has invalid format
     */
    private void checkInputString(String inputString) throws InvalidInput {
        if (checkStartsEquals(inputString, CHARS_COMMAND, CHARS_COMMAND_ONLY)) {
            // Print current charset.
            printCharSet();
        }

        else if (checkStartsEquals(inputString, ADD_COMMAND, ADD_ONLY_COMMAND) ||
                checkStartsEquals(inputString, REMOVE_COMMAND, REMOVE_ONLY_COMMAND)) {

            // Handle "add ..." or "remove ..." commands.
            allCasesAddRemove(inputString);
        }

        else if (checkStartsEquals(inputString, RES_COMMAND, RES_ONLY_COMMAND)) {
            // "res" prints resolution; "res up/down" modifies then prints.
            if (inputString.startsWith(RES_COMMAND)) {
                changeResolution(inputString);
            }
            System.out.println(RES_PRINT + resolution + ".");
        }

        else if (checkStartsEquals(inputString, REVERSE_COMMAND, REVERSE_ONLY_COMMAND)) {
            // Run inverted-brightness version of the algorithm and output.
            updatedImage = runReverse();
            output.out(updatedImage);
        }

        else if (checkStartsEquals(inputString, OUTPUT_COMMAND, OUTPUT_ONLY_COMMAND)) {
            // Change output method (console/html).
            changeOutput(inputString);
        }

        else if (checkStartsEquals(inputString, RUN_ASCII_ART_COMMAND, RUN_ASCII_ART_ONLY_COMMAND)) {
            // Run standard ascii art generation and output.
            runAsciiArt();
        }

        else {
            // Unknown command.
            throw new InvalidInput(INCORRECT_COMMAND);
        }
    }

    /**
     * Parses and applies "output ..." command.
     *
     * <p>Supported:
     * <ul>
     *   <li>output console</li>
     *   <li>output html</li>
     * </ul>
     * </p>
     *
     * @param inputString normalized input string
     * @throws InvalidInput if missing/invalid output argument
     */
    private void changeOutput(String inputString) throws InvalidInput {
        if (inputString.equals(OUTPUT_ONLY_COMMAND)) {
            // "output" with no argument.
            throw new InvalidInput(OUTPUT_CHANGE_INCORRECT_FORM);
        } else {
            // Extract second arg after "output ".
            String arg2 = inputString.substring(OUTPUT_SECOND_ARG_BEGINNING);

            if (checkStartsEquals(arg2, OUTPUT_HTML_KEY, OUTPUT_HTML_ONLY_KEY)) {
                output = new HtmlAsciiOutput(FILE_NAME_HTML, FONT_NAME_HTML);
            } else if (checkStartsEquals(arg2, OUTPUT_CONSOLE_KEY, OUTPUT_CONSOLE_ONLY_KEY)) {
                output = new ConsoleAsciiOutput();
            } else {
                throw new InvalidInput(OUTPUT_CHANGE_INCORRECT_FORM);
            }
        }
    }

    /**
     * Runs the main ASCII-art algorithm, stores the result in {@link #updatedImage},
     * and outputs it using the current {@link #output} strategy.
     *
     * @throws InvalidInput if the charset is too small to execute (fewer than 2 chars)
     */
    private void runAsciiArt() throws InvalidInput {
        char[] tempCharSet = subImgCharMatcher.getCharset();

        // The assignment requires at least 2 characters to generate ASCII art.
        if (tempCharSet.length < MINIMAL_CHARSET_LENGTH_TO_RUN_ASCI_IART) {
            throw new InvalidInput(SMALL_NUMBER_OF_CHARSET);
        }

        // One algorithm run instance per execution.
        AsciiArtAlgorithm asciiArtAlgorithm = new AsciiArtAlgorithm(
                image, resolution, subImgCharMatcher
        );

        this.updatedImage = asciiArtAlgorithm.run();
        output.out(updatedImage);
    }

    /**
     * Parses and executes a resolution change command: "res up" or "res down".
     *
     * <p>Boundaries are calculated from the loaded image:
     * <ul>
     *   <li>maxResolution = image width</li>
     *   <li>minResolution = max(1, imageWidth / imageHeight)</li>
     * </ul>
     * </p>
     *
     * @param inputString normalized input string beginning with "res "
     * @throws InvalidInput if format is invalid or requested change exceeds boundaries
     */
    private void changeResolution(String inputString) throws InvalidInput {
        // Recompute boundaries based on current image.
        maxResolution = image.getWidth();
        minResolution = Math.max(1, image.getWidth() / image.getHeight());

        // Second token after "res ".
        String arg2 = inputString.substring(4);

        if (checkStartsEquals(arg2, RES_UP_KEY, RES_UP_ONLY_KEY)) {
            if (resolution * 2 <= maxResolution) {
                resolution *= 2;
            } else {
                throw new InvalidInput(EXCEEDING_BOUNDARIES_RESOLUTION_CHANGE);
            }
        } else if (checkStartsEquals(arg2, RES_DOWN_KEY, RES_DOWN_ONLY_KEY)) {
            if (resolution / 2 >= minResolution) {
                resolution /= 2;
            } else {
                throw new InvalidInput(EXCEEDING_BOUNDARIES_RESOLUTION_CHANGE);
            }
        } else {
            throw new InvalidInput(RESOLUTION_CHANGE_INCORRECT_FORM);
        }
    }

    /**
     * Runs ASCII-art generation with inverted brightness.
     *
     * <p>For each sub-image:
     * <ul>
     *   <li>Compute brightness in [0,1]</li>
     *   <li>Invert it: 1 - brightness</li>
     *   <li>Clamp to matcher's [minBrightness, maxBrightness]</li>
     *   <li>Choose closest character</li>
     * </ul>
     * </p>
     *
     * @return a 2D ASCII character matrix representing the reversed output
     */
    private char[][] runReverse() {
        Image imageAfterPadding = ImageUses.padImageToPowerOfTwo(image);
        Image[][] subImages = getSubImage(imageAfterPadding, resolution);
        char[][] result = new char[subImages.length][subImages[0].length];

        for (int i = 0; i < subImages.length; i++) {
            for (int j = 0; j < subImages[0].length; j++) {
                double brightness = ImageUses.calculateSubImageBrightness(subImages[i][j]);
                double brightnessComplete = FULL_BRIGHTNESS - brightness;

                // Clamp brightness to the matcher's range.
                if (brightnessComplete < subImgCharMatcher.getMinBrightness()) {
                    brightnessComplete = subImgCharMatcher.getMinBrightness();
                }
                if (brightnessComplete > subImgCharMatcher.getMaxBrightness()) {
                    brightnessComplete = subImgCharMatcher.getMaxBrightness();
                }

                result[i][j] = subImgCharMatcher.getCharByImageBrightness(brightnessComplete);
            }
        }
        return result;
    }

    /**
     * Handles add/remove commands in all supported formats:
     * <ul>
     *   <li>Single character: "add a" / "remove a"</li>
     *   <li>Keyword: "all" / "space"</li>
     *   <li>Range: "add a-z" / "remove a-z" (also supports reversed range a-z or z-a)</li>
     * </ul>
     *
     * <p>On invalid format, throws {@link InvalidInput} with the appropriate error message.</p>
     *
     * @param inputString normalized input, starting with "add " or "remove "
     * @throws InvalidInput if the command format is invalid
     */
    private void allCasesAddRemove(String inputString) throws InvalidInput {
        boolean beginWithAdd = checkStartsEquals(inputString, ADD_COMMAND, ADD_ONLY_COMMAND);

        // "add" or "remove" without arguments is invalid.
        if (inputString.equals(ADD_ONLY_COMMAND) || inputString.equals(REMOVE_ONLY_COMMAND)) {
            if (beginWithAdd) {
                throw new InvalidInput(ADD_INCORRECT_FORM_ERROR);
            } else {
                throw new InvalidInput(REMOVE_INCORRECT_FORM_ERROR);
            }
        }

        // Extract the argument after the command prefix.
        String arg2;
        if (beginWithAdd) {
            arg2 = inputString.substring(4); // after "add "
        } else {
            arg2 = inputString.substring(7); // after "remove "
        }

        // Case 1: single character (or char followed by extra text).
        if (arg2.length() == 1 || (arg2.length() > 1) && (arg2.charAt(1) == ' ')) {
            char c = arg2.charAt(0);
            // Only printable ASCII.
            if (c <= LAST_ASCII_VALUE && c >= FIRST_ASCII_VALUE) {
                if (beginWithAdd) {
                    this.subImgCharMatcher.addChar(c);
                } else {
                    this.subImgCharMatcher.removeChar(c);
                }
            }
        }

        // Case 2+: keywords/ranges.
        else {
            // "all"
            if (checkStartsEquals(arg2, ADD_OR_REMOVE_ALL, ADD_OR_REMOVE_ONLY_ALL)) {
                for (int i = FIRST_ASCII_VALUE; i <= LAST_ASCII_VALUE; i++) {
                    if (beginWithAdd) {
                        this.subImgCharMatcher.addChar((char) i);
                    } else {
                        this.subImgCharMatcher.removeChar((char) i);
                    }
                }
            }

            // "space"
            else if (checkStartsEquals(arg2, ADD_OR_REMOVE_SPACE, ADD_OR_REMOVE_ONLY_SPACE)) {
                if (beginWithAdd) {
                    this.subImgCharMatcher.addChar(' ');
                } else {
                    this.subImgCharMatcher.removeChar(' ');
                }
            }

            // Range form "x-y"
            else if (!checkRange(arg2).isEmpty()) {
                String rangeString = checkRange(arg2);

                char firstChar;
                char lastChar;

                // Normalize so firstChar <= lastChar.
                if (rangeString.charAt(0) < rangeString.charAt(rangeString.length() - 1)) {
                    firstChar = rangeString.charAt(0);
                    lastChar = rangeString.charAt(rangeString.length() - 1);
                } else {
                    firstChar = rangeString.charAt(rangeString.length() - 1);
                    lastChar = rangeString.charAt(0);
                }

                for (int i = firstChar; i <= lastChar; i++) {
                    if (beginWithAdd) {
                        this.subImgCharMatcher.addChar((char) i);
                    } else {
                        this.subImgCharMatcher.removeChar((char) i);
                    }
                }
            }

            // Invalid add/remove format.
            else {
                if (beginWithAdd) {
                    throw new InvalidInput(ADD_INCORRECT_FORM_ERROR);
                } else {
                    throw new InvalidInput(REMOVE_INCORRECT_FORM_ERROR);
                }
            }
        }
    }

    /**
     * Validates the range format "x-y".
     *
     * <p>This method:
     * <ul>
     *   <li>Splits by '-'</li>
     *   <li>Accepts exactly two parts</li>
     *   <li>Requires the first part to be length 1</li>
     *   <li>Requires the second part to be length 1 (or longer with a space after the first char)</li>
     * </ul>
     * </p>
     *
     * <p>If valid, returns a normalized representation "x y" (first char + space + second char).</p>
     *
     * @param range range string (e.g., "a-z")
     * @return normalized "x y" if valid, otherwise an empty string
     */
    private String checkRange(String range) {
        String[] ranges = range.split("-");
        if (ranges.length == 2) {

            if (ranges[0].length() == 1 &&
                    (ranges[1].length() == 1 ||
                            (ranges[1].length() > 1 && ranges[1].charAt(1) == ' '))) {
                return ranges[0] + " " + ranges[1].charAt(0);
            }
        }
        return "";
    }

    /**
     * Prints the current character set to stdout in ascending ASCII order.
     *
     * <p>Implementation detail:
     * This method copies the charset, sorts it using bubble sort, and prints each character
     * followed by a space.</p>
     */
    private void printCharSet() {
        char[] charset = subImgCharMatcher.getCharset();
        int charsetLength = charset.length;

        // Copy charset so we do not mutate internal ordering.
        char[] copyOfCharset = new char[charset.length];
        System.arraycopy(charset, 0, copyOfCharset, 0, charset.length);

        if (charsetLength == 0) {
            return;
        }

        // Sort using bubble sort.
        for (int i = 0; i < charsetLength - 1; i++) {
            for (int j = i + 1; j < charsetLength; j++) {
                if ((int) copyOfCharset[i] > (int) copyOfCharset[j]) {
                    char tempChar = copyOfCharset[i];
                    copyOfCharset[i] = copyOfCharset[j];
                    copyOfCharset[j] = tempChar;
                }
            }
        }

        // Print sorted charset.
        for (int i = 0; i < charsetLength; i++) {
            System.out.print(copyOfCharset[i] + " ");
        }
        System.out.println();
    }

    /**
     * Utility method for command matching.
     *
     * <p>Returns true if:
     * <ul>
     *   <li>{@code inputString.startsWith(sentence1)} (command with arguments)</li>
     *   <li>OR {@code inputString.equals(sentence2)} (command without arguments)</li>
     * </ul>
     * </p>
     *
     * @param inputString full command line
     * @param sentence1   prefix form (typically command + " ")
     * @param sentence2   exact command form (no trailing space)
     * @return true if the input matches either expected form
     */
    private boolean checkStartsEquals(String inputString, String sentence1, String sentence2) {
        return inputString.startsWith(sentence1) || inputString.equals(sentence2);
    }

    /**
     * Program entry point.
     *
     * <p>Expected usage:
     * <pre>{@code
     * java shell <image_filename>
     * }</pre>
     * </p>
     *
     * @param args command-line arguments; {@code args[0]} should be an image filename/path
     */
    public static void main(String[] args) {
        Shell shell = new Shell();
        shell.run(args[0]);
    }
}
