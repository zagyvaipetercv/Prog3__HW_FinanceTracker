package financetracker.datatypes;

import java.time.LocalDate;

public class Payment extends Model {
    private LocalDate date;
    private Debt debt;
    private Money amount;

    public Payment(long id, LocalDate date, Debt debt, Money amount) {
        super(id);
        this.date = date;
        this.debt = debt;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Debt getDebt() {
        return debt;
    }

    public void setDebt(Debt debt) {
        this.debt = debt;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }
}
