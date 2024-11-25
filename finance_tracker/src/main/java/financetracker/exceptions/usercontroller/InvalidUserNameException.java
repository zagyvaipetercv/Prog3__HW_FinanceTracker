package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidUserNameException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID USERNAME";


    public InvalidUserNameException(String message) {
        super(message);
    }

    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
