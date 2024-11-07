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
import financetracker.models.CashFlow;
import financetracker.models.Money;

// TODO: Create test cases for MoneyController
// TODO: Create PanelView for Income/Expenses
// TODO: Create Query Method for Incomes/Expenses

public class CashFlowController extends Controller<CashFlow> {
    private static final String DEFAULT_FILEPATH = "saves\\cash_flow.dat";

    public CashFlowController() throws CannotCreateControllerException {
        this(DEFAULT_FILEPATH);
    }

    public CashFlowController(String filePath) throws CannotCreateControllerException {
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

        Money money = new Money(amount, currency);
        CashFlow cashFlow = new CashFlow(
                getNextId(),
                date,
                money,
                reason);
        try {
            appendNewData(cashFlow);
        } catch (ControllerCannotWriteException | ControllerCannotReadException e) {
            throw new BalanceCouldNotCahcngeException(money, "Couldn't add money to balance");
        }
    }

    public void addMoneyToAccount(List<CashFlow> cashFlowList)
            throws MoneyAmountIsInvalidException, ReasonIsInvalidException, BalanceCouldNotCahcngeException {
        for (CashFlow cashFlow : cashFlowList) {
            double amount = cashFlow.getMoney().getAmount();
            String reason = cashFlow.getReason();

            if (amountIsInvalid(amount)) {
                throw new MoneyAmountIsInvalidException(amount,
                        "Amount must be greater than 0.0 when adding money to account");
            }

            if (reasonIsInvalid(reason)) {
                throw new ReasonIsInvalidException(reason, "Reason can't be blank");
            }
        }

        try {
            appendNewDatas(cashFlowList);
        } catch (ControllerCannotReadException | ControllerCannotWriteException e) {
            throw new BalanceCouldNotCahcngeException(null, "Couldn't add money to balance");
        }
    }

    // TODO: Replace with a specialized exception (like CannotReadMoneyException or
    // somehting)
    public List<CashFlow> getCashFlows(int year, Month month) throws ControllerCannotReadException {
        List<CashFlow> saved = readAll();
        List<CashFlow> result = new ArrayList<>();

        for (CashFlow cashFlow : saved) {
            if (cashFlow.getDate().getYear() == year && cashFlow.getDate().getMonth().equals(month)) {
                result.add(cashFlow);
            }
        }

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
