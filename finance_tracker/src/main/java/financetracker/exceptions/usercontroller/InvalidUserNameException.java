package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidUserNameException extends ErrorBoxException {
    public enum ErrorType {
        REGISTRATION_BLANK_OR_WHITESPACE,
        REGISTRATION_ALREADY_EXISTS,
        LOGIN_NOT_REGISTERED_USERNAME
    }

    private static final String ERROR_TITLE = "INVALID USERNAME";

    private final transient ErrorType type;
    private final transient String errorMessage;

    public InvalidUserNameException(ErrorType type) {
        this.type = type;

        switch (type) {
            case REGISTRATION_BLANK_OR_WHITESPACE:
                errorMessage = "Username cannot be blank or contain white spaces";
                break;

            case REGISTRATION_ALREADY_EXISTS:
                errorMessage = "Username already exists";
                break;

            case LOGIN_NOT_REGISTERED_USERNAME:
                errorMessage = "Username is not registered yet";
                break;

            default:
                errorMessage = "Unkonw error type";
                break;
        }
    }

    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public ErrorType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
