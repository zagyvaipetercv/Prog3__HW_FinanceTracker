package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class DeletingDebtFailedException extends ErrorBoxException {
    private static final String ERROR_TITLE = "DELETING DEBT FAILED";

    private final transient Debt debt;

    public DeletingDebtFailedException(String message, Debt debt) {
        super(message);
        this.debt = debt;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Debt getDebt() {
        return debt;
    } 
}
