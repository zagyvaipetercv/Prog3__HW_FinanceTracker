package financetracker.models;

import java.io.Serializable;
import java.util.Currency;

public class Money implements Serializable {
    private double amount;
    private Currency currency;
    
    public Money(double amount, Currency currency) {
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
