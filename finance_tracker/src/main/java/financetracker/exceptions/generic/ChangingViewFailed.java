package financetracker.exceptions.generic;

import financetracker.exceptions.ErrorBoxException;

public class ChangingViewFailed extends ErrorBoxException {

    private static final String ERROR_TITLE = "CHANGING VIEW FAILED";

    public ChangingViewFailed(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
