package financetracker.models;

import java.time.LocalDate;
import java.util.Currency;

public class Money extends Model {
    private LocalDate date;
    private double amount;
    private Currency currency;
    private String reason;

    public Money(long id, LocalDate date, double amount, Currency currency, String reason) {
        super(id);
        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
    }
    
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
