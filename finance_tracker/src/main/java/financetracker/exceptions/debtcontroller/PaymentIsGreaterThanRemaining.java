package financetracker.exceptions.debtcontroller;

import financetracker.datatypes.Money;
import financetracker.exceptions.WarningBoxException;

public class PaymentIsGreaterThanRemaining extends WarningBoxException {
    private static final String ERROR_TITLE = "PAYMENT IS GREATER THAN REMAINING";

    private final transient Money remaining;
    private final transient Money payment;

    public PaymentIsGreaterThanRemaining(String message, Money remaining, Money payment) {
        super(message);
        this.remaining = remaining;
        this.payment = payment;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Money getRemaining() {
        return remaining;
    }

    public Money getPayment() {
        return payment;
    }
}
