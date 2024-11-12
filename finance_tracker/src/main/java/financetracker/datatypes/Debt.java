package financetracker.datatypes;

import java.time.LocalDate;
import java.util.List;

public class Debt extends Model {
    public enum DebtDirection {
        I_OWE,
        THEY_OWE
    }

    private User counterParty;
    private DebtDirection direction;
    private LocalDate date;
    private Money amount;
    private List<Payment> payments;
    private boolean fulfilled;

    private LocalDate deadline;
    private boolean hasDeadline;

    public Debt(long id, User counterParty, DebtDirection direction, LocalDate date, Money amount,
            List<Payment> payments, boolean fulfilled, LocalDate deadLine) {
        super(id);
        this.counterParty = counterParty;
        this.direction = direction;
        this.date = date;
        this.deadline = deadLine;
        this.amount = amount;
        this.payments = payments;
        this.fulfilled = fulfilled;

        hasDeadline = deadLine != null;
    }

    public User getCounterParty() {
        return counterParty;
    }

    public void setCounterParty(User counterParty) {
        this.counterParty = counterParty;
    }

    public DebtDirection getDirection() {
        return direction;
    }

    public void setDirection(DebtDirection direction) {
        this.direction = direction;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadLine) {
        this.deadline = deadLine;
    }

    public boolean hasDeadline() {
        return hasDeadline;
    }

    public void setHasDeadline(boolean hasDeadline) {
        this.hasDeadline = hasDeadline;
    }

    @Override
    public String toString() {
        return counterParty.getName() + " " + direction.name() + " " + date + " " + amount;
    }
}
