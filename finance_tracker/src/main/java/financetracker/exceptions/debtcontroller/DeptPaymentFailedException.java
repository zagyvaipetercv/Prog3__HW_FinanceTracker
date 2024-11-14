package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class DeptPaymentFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "DEPT PAYMENT FAILED";

    private final transient Debt debt;
    private final transient double amount;

    public DeptPaymentFailedException(String message, Debt debt, double amount) {
        super(message);
        this.debt = debt;
        this.amount = amount;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Debt getDebt() {
        return debt;
    }

    public double getAmount() {
        return amount;
    }

}
