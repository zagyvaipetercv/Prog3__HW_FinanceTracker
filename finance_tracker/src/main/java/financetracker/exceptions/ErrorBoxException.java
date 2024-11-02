package financetracker.exceptions;

public abstract class ErrorBoxException extends Exception {

    protected ErrorBoxException() {
        super();
    }

    protected ErrorBoxException(String message) {
        super(message);
    }

    public abstract String getErrorTitle();
}
