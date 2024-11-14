package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class FulfilledDebtCantChange extends ErrorBoxException{
    private static final String ERROR_TITLE = "CANT EDIT FULFILLED DEBTS";

    private final transient Debt debt;

    public FulfilledDebtCantChange(String message, Debt debt) {
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
