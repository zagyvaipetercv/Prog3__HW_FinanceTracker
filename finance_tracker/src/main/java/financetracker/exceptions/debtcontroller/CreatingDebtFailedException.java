package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class CreatingDebtFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "CREATEING DEBT FAILED";
    private final transient Debt debt;

    public CreatingDebtFailedException(Debt debt, String message) {
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
