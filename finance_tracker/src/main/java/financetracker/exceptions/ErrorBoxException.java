package financetracker.exceptions;

/**
 * Base class for exceptions that can be shown in an error box
 */
public abstract class ErrorBoxException extends Exception {

    protected ErrorBoxException() {
        super();
    }

    protected ErrorBoxException(String message) {
        super(message);
    }

    public abstract String getErrorTitle();
}
