package Exceptions;

/**
 * Thrown to indicate that the user provided an invalid or malformed input.
 *
 * <p>This exception is used throughout the application to signal errors
 * related to command parsing, invalid arguments, illegal ranges,
 * unsupported commands, or violations of expected input format.</p>
 *
 * <p>It is a checked exception, forcing callers to either handle the error
 * explicitly or propagate it upward, which helps keep input validation
 * logic clear and centralized.</p>
 */
public class InvalidInput extends Exception {

    /**
     * Constructs a new {@code InvalidInput} exception with a detailed
     * error message describing the cause of the invalid input.
     *
     * @param message a human-readable explanation of what was wrong
     *                with the user input
     */
    public InvalidInput(String message) {
        super(message);
    }
}

