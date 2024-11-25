package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidPasswordException extends ErrorBoxException {

    private static final String ERROR_TITLE = "INVALID PASSWORD";


    public InvalidPasswordException(String message) {
        super(message);
    }


    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
