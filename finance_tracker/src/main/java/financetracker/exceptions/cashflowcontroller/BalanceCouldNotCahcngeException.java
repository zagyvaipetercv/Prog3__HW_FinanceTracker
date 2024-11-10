package financetracker.exceptions.cashflowcontroller;

import financetracker.datatypes.Money;
import financetracker.exceptions.ErrorBoxException;

public class BalanceCouldNotCahcngeException extends ErrorBoxException {

    private static final String ERROR_TITLE = "BALANCE COULD NOT CHANGE";

    private final transient Money moneyNotSaved;

    public BalanceCouldNotCahcngeException(Money moneyNotSaved, String message) {
        super(message);
        this.moneyNotSaved = moneyNotSaved;
    }

    
    public Money getMoneyNotSaved() {
        return moneyNotSaved;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
