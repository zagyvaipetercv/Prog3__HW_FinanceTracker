package financetracker.exceptions.cashflowcontroller;

import financetracker.datatypes.CashFlow;
import financetracker.exceptions.ErrorBoxException;

public class DeletingCashFlowFailed extends ErrorBoxException {
    private static final String ERROR_TITLE = "DELETING CASHFLOW FAILED";

    private final transient CashFlow cashFlow;

    public DeletingCashFlowFailed(String message, CashFlow cashFlow) {
        super(message);
        this.cashFlow = cashFlow;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public CashFlow getCashFlow() {
        return cashFlow;
    }
}
