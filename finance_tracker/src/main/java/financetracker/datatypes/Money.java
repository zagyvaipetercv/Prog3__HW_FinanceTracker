package financetracker.datatypes;

import java.io.Serializable;
import java.util.Currency;
import java.util.Objects;

import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.utilities.CustomMath;

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

    @Override
    public String toString() {
        return amount + " " + (currency.getCurrencyCode().equals("HUF") ? "Ft" : currency.getDisplayName());
    }

    @Override 
    public boolean equals(Object o) {
        if (!o.getClass().equals(Money.class)) {
            return false;
        }

        Money m = (Money)o;
        return CustomMath.almostEquals(amount, m.getAmount()) && m.getCurrency().equals(currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(((Double)amount), currency);
    }

    /**
     * Tries to parse a string into a valid amount value
     * 
     * @param amountString the string that will be parsed
     * @return the double value the string represents
     * @throws InvalidAmountException if string is blank or in the wrong format
     */
    public static double parseAmount(String amountString) throws InvalidAmountException {
        try {
            if (amountString.isBlank()) {
                throw new InvalidAmountException(amountString, "Amount can ot be blank");
            }
            return Double.parseDouble(amountString);
        } catch (NullPointerException | NumberFormatException e) {
            throw new InvalidAmountException(amountString, "'" + amountString
                    + "' is not a number or in the wrong format (allowed characters are numbers and '.')");
        }
    }

    /**
     * Parses a string to a Currency
     * <p>
     * Specialized for hungarian notation
     * 
     * @param currency the string representing a currency 
     * @return a currency
     */
    public static Currency parseCurrency(String currency) {
        if (currency.equals("Ft")) {
            return Currency.getInstance("HUF");
        }

        return Currency.getInstance(currency);
    }

}
