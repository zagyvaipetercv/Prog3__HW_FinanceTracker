package financetracker.exceptions.cashflowcontroller;

import financetracker.exceptions.ErrorBoxException;

public class MoneyAmountIsInvalidException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID AMOUNT";

    private final transient String amountString;

    public MoneyAmountIsInvalidException(String amountString) {
        super("Amount is a not parsable");
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
