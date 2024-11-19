package financetracker.datatypes;

import java.time.LocalDate;

public class CashFlow extends Model {
    private User user;
    private LocalDate date;
    private Money money;

    private String reason;

    public CashFlow(long id, User user, LocalDate date, Money money, String reason) {
        super(id);
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
