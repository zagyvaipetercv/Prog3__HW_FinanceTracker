package financetracker.controllers;

import java.util.Currency;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.exceptions.moneycontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.moneycontroller.MoneyAmountIsInvalidException;
import financetracker.exceptions.moneycontroller.ReasonIsInvalidException;
import financetracker.models.Money;

public class MoneyController extends Controller<Money> {
    private static final String DEFAULT_FILEPATH = "saves\\money.dat";

    public MoneyController() throws CannotCreateControllerException {
        this(DEFAULT_FILEPATH);
    }

    public MoneyController(String filePath) throws CannotCreateControllerException {
        super(filePath);    
    }

    public void addMoneyToAccount(double amount, Currency currency, String reason) throws MoneyAmountIsInvalidException, ReasonIsInvalidException, BalanceCouldNotCahcngeException {
        if (amountIsInvalid(amount)) {
            throw new MoneyAmountIsInvalidException(amount, "Amount must be greater than 0.0 when adding money to account");
        }

        if (reasonIsInvalid(reason)) {
            throw new ReasonIsInvalidException(reason, "Reason can't be blank");
        }

        Money money = new Money(getNextId(), amount, currency, reason);
        try {
            appendNewData(money);
        } catch (ControllerCannotWriteException | ControllerCannotReadException e) {
            throw new BalanceCouldNotCahcngeException(money, "Couldn't add money to balance");
        }
    }

    private boolean amountIsInvalid(double amount) {
        return amount <= 0.0;
    }

    private boolean reasonIsInvalid(String reason) {
        return reason == null || reason.isBlank();
    }
}
