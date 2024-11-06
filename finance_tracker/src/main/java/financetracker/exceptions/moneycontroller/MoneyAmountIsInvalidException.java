package financetracker.exceptions.moneycontroller;

import financetracker.exceptions.ErrorBoxException;

public class MoneyAmountIsInvalidException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID AMOUNT";

    private final transient double amount;

    public MoneyAmountIsInvalidException(double amount, String message) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
