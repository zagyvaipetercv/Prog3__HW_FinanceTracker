package financetracker.exceptions;

public abstract class WarningBoxException extends Exception {

    protected WarningBoxException(String message) {
        super(message);
    }

    public abstract String getErrorTitle();
}
