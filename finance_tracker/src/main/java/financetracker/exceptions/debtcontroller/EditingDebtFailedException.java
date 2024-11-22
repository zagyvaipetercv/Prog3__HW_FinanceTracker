package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Debt;
import financetracker.exceptions.ErrorBoxException;

public class EditingDebtFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "DEBT WAS NOT EDITETD";

    private final transient Debt originalDebt;


    public EditingDebtFailedException(String message, Debt originalDebt) {
        super(message);
        this.originalDebt = originalDebt;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Debt getOriginalDebt() {
        return originalDebt;
    }
}