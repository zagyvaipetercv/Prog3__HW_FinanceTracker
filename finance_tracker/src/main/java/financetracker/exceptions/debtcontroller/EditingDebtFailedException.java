package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class EditingDebtFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "DEBT WAS NOT EDITETD";

    private final transient Debt originalDebt;
    private final transient Debt changedDebt;


    public EditingDebtFailedException(String message, Debt originalDebt, Debt changedDebt) {
        super(message);
        this.originalDebt = originalDebt;
        this.changedDebt = changedDebt;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Debt getOriginalDebt() {
        return originalDebt;
    }

    public Debt getChangedDebt() {
        return changedDebt;
    }
}
