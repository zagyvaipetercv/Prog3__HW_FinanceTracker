package financetracker.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.exceptions.moneycontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.moneycontroller.MoneyAmountIsInvalidException;
import financetracker.exceptions.moneycontroller.ReasonIsInvalidException;
import financetracker.models.Money;

// TODO: Create test cases for MoneyController
// TODO: Create PanelView for Income/Expenses
// TODO: Create Query Method for Incomes/Expenses

public class MoneyController extends Controller<Money> {
    private static final String DEFAULT_FILEPATH = "saves\\money.dat";

    public MoneyController() throws CannotCreateControllerException {
        this(DEFAULT_FILEPATH);
    }

    public MoneyController(String filePath) throws CannotCreateControllerException {
        super(filePath);
    }

    public void addMoneyToAccount(LocalDate date, double amount, Currency currency, String reason)
            throws MoneyAmountIsInvalidException, ReasonIsInvalidException, BalanceCouldNotCahcngeException {
        if (amountIsInvalid(amount)) {
            throw new MoneyAmountIsInvalidException(amount,
                    "Amount must be greater than 0.0 when adding money to account");
        }

        if (reasonIsInvalid(reason)) {
            throw new ReasonIsInvalidException(reason, "Reason can't be blank");
        }

        Money money = new Money(getNextId(), date, amount, currency, reason);
        try {
            appendNewData(money);
        } catch (ControllerCannotWriteException | ControllerCannotReadException e) {
            throw new BalanceCouldNotCahcngeException(money, "Couldn't add money to balance");
        }
    }

    // TODO: Replace with a specialized exception (like CannotReadMoneyException or somehting)  
    public List<Money> getMoney(int year, Month month) throws ControllerCannotReadException {
        List<Money> saved = readAll();
        List<Money> result = new ArrayList<>();

        for (Money money : saved) {
            if (money.getDate().getYear() == year && money.getDate().getMonth().equals(month)) {
                result.add(money);
            }
        }

        return result;
    }

    public static String[] moneyToRowData(Money money) {
        String[] result = new String[4];

        result[0] = money.getDate().toString();
        result[1] = ((Double) money.getAmount()).toString();
        result[2] = money.getCurrency().getDisplayName();
        result[3] = money.getReason();

        return result;
    }

    private boolean amountIsInvalid(double amount) {
        return amount <= 0.0;
    }

    private boolean reasonIsInvalid(String reason) {
        return reason == null || reason.isBlank();
    }

    public enum CashFlowType {
        ALL,
        INCOME,
        EXPENSE
    }
}
