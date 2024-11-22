package financetracker.exceptions.models;

import financetracker.exceptions.ErrorBoxException;

public class NoItemWasSelected extends ErrorBoxException {
    private static final String ERROR_TITLE = "NO ITEM WAS SELECTED";

    public NoItemWasSelected(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
