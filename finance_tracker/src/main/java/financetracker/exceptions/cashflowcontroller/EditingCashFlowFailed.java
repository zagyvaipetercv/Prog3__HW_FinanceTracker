package financetracker.exceptions.cashflowcontroller;

import financetracker.datatypes.CashFlow;
import financetracker.exceptions.ErrorBoxException;

public class EditingCashFlowFailed extends ErrorBoxException {

    private static final String ERROR_TITLE = "EDITING CASHFLOW FAILED";

    private final transient CashFlow cashFlow;

    public EditingCashFlowFailed(String message, CashFlow cashFlow) {
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
