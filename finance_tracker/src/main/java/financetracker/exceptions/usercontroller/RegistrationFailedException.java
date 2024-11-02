package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class RegistrationFailedException extends ErrorBoxException {
    private static final String ERROR_TITLE = "REGISTRATION FAILED";
    
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public RegistrationFailedException(String message) {
        super(message);
    }
}
