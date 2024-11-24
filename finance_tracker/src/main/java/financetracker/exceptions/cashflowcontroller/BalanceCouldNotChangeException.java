package financetracker.exceptions.cashflowcontroller;

import financetracker.exceptions.ErrorBoxException;

public class BalanceCouldNotChangeException extends ErrorBoxException {

    private static final String ERROR_TITLE = "BALANCE COULD NOT CHANGE";

    public BalanceCouldNotChangeException(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
