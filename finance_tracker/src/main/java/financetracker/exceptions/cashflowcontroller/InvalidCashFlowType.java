package financetracker.exceptions.cashflowcontroller;

import financetracker.controllers.CashFlowController.CashFlowType;

public class InvalidCashFlowType extends Exception {
    private final transient CashFlowType type;

    public InvalidCashFlowType(String message, CashFlowType type) {
        super(message);
        this.type = type;
    }

    public CashFlowType getType() {
        return type;
    }
}
