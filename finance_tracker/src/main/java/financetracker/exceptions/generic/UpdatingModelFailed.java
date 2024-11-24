package financetracker.exceptions.generic;

import financetracker.exceptions.ErrorBoxException;

public class UpdatingModelFailed extends ErrorBoxException {
    private static final String ERROR_TITLE = "UPDATING MODEL FAILED";

    public UpdatingModelFailed(String message) {
        super(message);
    }


    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
