package financetracker.models;

import financetracker.datatypes.Money;

public class SummarizedCashFlowModel {
    private Money moneyOnAccount;
    private Money sumIncomes;
    private Money sumExpenses;
    private Money sumThisMonth;
    
    public SummarizedCashFlowModel(Money moneyOnAccount, Money sumIncomes, Money sumExpenses, Money sumThisMonth) {
        this.moneyOnAccount = moneyOnAccount;
        this.sumIncomes = sumIncomes;
        this.sumExpenses = sumExpenses;
        this.sumThisMonth = sumThisMonth;
    }

    public Money getMoneyOnAccount() {
        return moneyOnAccount;
    }

    public Money getSumIncomes() {
        return sumIncomes;
    }

    public Money getSumExpenses() {
        return sumExpenses;
    }

    public Money getSumThisMonth() {
        return sumThisMonth;
    }

    
}
