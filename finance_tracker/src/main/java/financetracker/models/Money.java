package financetracker.models;

import java.util.Currency;

public class Money extends Model {
    private double amount;
    private Currency currency;

    public Money(long id, double amount, Currency currency) {
        super(id);
        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

}
