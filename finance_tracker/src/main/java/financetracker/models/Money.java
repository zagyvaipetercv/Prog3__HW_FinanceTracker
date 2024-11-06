package financetracker.models;

import java.util.Currency;

public class Money extends Model {
    private double amount;
    private Currency currency;
    private String reason;

    public Money(long id, double amount, Currency currency, String reason) {
        super(id);
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
