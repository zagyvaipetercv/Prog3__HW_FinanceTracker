package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class LoginFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "LOGIN FAILED";
    
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
    public LoginFailedException(String message) {
        super(message);
    }
}
