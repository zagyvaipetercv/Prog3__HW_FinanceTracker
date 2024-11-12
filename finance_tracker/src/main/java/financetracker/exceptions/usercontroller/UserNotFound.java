package financetracker.exceptions.usercontroller;

import financetracker.exceptions.ErrorBoxException;

public class UserNotFound extends ErrorBoxException {
    private static final String ERROR_TITLE = "USER NOT FOUND";

    private final transient String username;

    public UserNotFound(String username, String message) {
        super(message);
        this.username = username;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public String getUsername() {
        return username;
    }
}
