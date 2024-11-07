package financetracker.models;

import java.time.LocalDate;

public class CashFlow extends Model {
    private LocalDate date;
    private Money money;

    private String reason;

    public CashFlow(long id, LocalDate date, Money money, String reason) {
        super(id);
        this.date = date;
        this.money = money;
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Money getMoney() {
        return money;
    }

    public void setMoney(Money money) {
        this.money = money;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
