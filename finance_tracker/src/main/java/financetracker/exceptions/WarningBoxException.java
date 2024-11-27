package financetracker.exceptions;

/**
 * Base class for exceptions that can be shown in a warning box
 */
public abstract class WarningBoxException extends Exception {

    protected WarningBoxException(String message) {
        super(message);
    }

    public abstract String getErrorTitle();
}
