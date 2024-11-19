package financetracker.exceptions.debtcontroller;

import financetracker.controllers.DebtController.DebtDirection;

public class UnknownDebtDirection extends Exception {
    private final transient DebtDirection direction;

    public UnknownDebtDirection(String message, DebtDirection direction) {
        super(message);

        this.direction = direction;
    }

    public DebtDirection getDirection() {
        return direction;
    }
}
