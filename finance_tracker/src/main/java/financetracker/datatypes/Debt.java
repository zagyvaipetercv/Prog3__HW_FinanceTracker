package financetracker.datatypes;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import financetracker.utilities.CustomMath;

public class Debt extends Model {
    private User debtor; // The party who owes money
    private User creditor; // The party to whom the money is obliagated
    private LocalDate date;
    private Money debtAmount;
    private List<Payment> payments;

    private LocalDate deadline;

    public Debt(long id, User debtor, User creditor, LocalDate date, Money debtAmount,
            List<Payment> payments, LocalDate deadLine) {
        super(id);
        this.debtor = debtor;
        this.creditor = creditor;
        this.date = date;
        this.deadline = deadLine;
        this.debtAmount = debtAmount;
        this.payments = payments;
    }

    public User getDebtor() {
        return debtor;
    }

    public void setDebtor(User counterParty) {
        this.debtor = counterParty;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getCreditor() {
        return creditor;
    }

    public void setCreditor(User creditor) {
        this.creditor = creditor;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Money getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(Money amount) {
        this.debtAmount = amount;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadLine) {
        this.deadline = deadLine;
    }

    @Override
    public String toString() {
        return debtor.getName() + " owes " + creditor.getName() + " " + date + " " + debtAmount;
    }

    /**
     * Returns true if debt has a deadline, false if not
     * 
     * @return true if debt has a deadline, false if not
     */
    public boolean hasDeadline() {
        return deadline != null;
    }

    /**
     * Returns if payed amount is equal to the debt amount
     * 
     * @return true if payed amount is equal to debt amount, false if not
     */
    public boolean isFulfilled() {
        return CustomMath.almostEquals(getPayedAmount().getAmount(), debtAmount.getAmount());
    }

    /**
     * Returns the summarized payed amount  
     * 
     * @return the summarized payed amount
     */
    public Money getPayedAmount() {
        double sum = 0.0;

        for (Payment payment : getPayments()) {
            sum += payment.getAmount().getAmount();
        }

        return new Money(sum, Currency.getInstance("HUF"));
    }
}
