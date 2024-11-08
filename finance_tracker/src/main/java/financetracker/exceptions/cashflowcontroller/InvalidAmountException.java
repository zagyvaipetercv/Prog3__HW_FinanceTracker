package financetracker.exceptions.cashflowcontroller;

import financetracker.exceptions.ErrorBoxException;

public class InvalidAmountException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID AMOUNT";

    private final transient String amountString;

    public InvalidAmountException(String amountString, String message) {
        super(message);
        this.amountString = amountString;
    }

    public String getAmountString() {
        return amountString;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
