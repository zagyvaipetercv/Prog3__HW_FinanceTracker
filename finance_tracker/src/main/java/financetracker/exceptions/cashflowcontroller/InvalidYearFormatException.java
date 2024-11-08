package financetracker.exceptions.cashflowcontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidYearFormatException extends ErrorBoxException {

    private static final String ERROR_TITLE = "INVALID YEAR FORMAT";

    public InvalidYearFormatException(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
