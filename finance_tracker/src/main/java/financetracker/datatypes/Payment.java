package financetracker.datatypes;

import java.io.Serializable;
import java.time.LocalDate;

public class Payment implements Serializable {
    private LocalDate date;
    private Debt debt;
    private Money amount;

    public Payment(LocalDate date, Debt debt, Money amount)  {
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
