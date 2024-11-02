package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidPasswordException extends ErrorBoxException {
    public enum ErrorType {
        NOT_ENOUGH_CHARACTERS_OR_WHITESPACE,
        PASSWORDS_DO_NOT_MATCH
    }

    private static final String ERROR_TITLE = "INVALID PASSWORD";

    private final transient ErrorType type;
    private final transient String errorMessage;

    public InvalidPasswordException(ErrorType type) {
        this.type = type;

        switch (type) {
            case NOT_ENOUGH_CHARACTERS_OR_WHITESPACE:
                errorMessage = "Password cannot contain white spaces or have less than 8 characters";
                break;

            case PASSWORDS_DO_NOT_MATCH:
                errorMessage = "Passwords do not match";
                break;

            default:
                errorMessage = "Unkown error type";
                break;
        }

    }

    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
