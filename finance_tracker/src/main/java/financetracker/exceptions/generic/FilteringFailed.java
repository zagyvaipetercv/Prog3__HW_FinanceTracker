package financetracker.exceptions.generic;

import financetracker.exceptions.ErrorBoxException;

public class FilteringFailed extends ErrorBoxException {

    private static String ERROR_TITLE = "FILTERING FAILED";

    public FilteringFailed(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
