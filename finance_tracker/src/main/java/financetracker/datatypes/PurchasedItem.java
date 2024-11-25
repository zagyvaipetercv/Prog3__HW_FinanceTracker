package financetracker.datatypes;

import java.io.Serializable;

public class PurchasedItem implements Serializable {
    private Purchase purchase;
    private Category category;
    private String name;
    private Money pricePerUnit;
    private double amount;

    public PurchasedItem(Purchase purchase, Category category, String name, Money pricePerUnit, double amount) {
        this.purchase = purchase;
        this.category = category;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.amount = amount;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Money pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Calculates and returns the sum price of the item
     * <p>
     * sumPrice = amount * pricePerUnit
     * 
     * @return amount * pricePerUnit
     */
    public Money getSumPrice() {
        if (pricePerUnit == null) {
            return null;
        }
        return new Money(amount * pricePerUnit.getAmount(), pricePerUnit.getCurrency());
    }

    /**
     * Sets the pricePerUnit value based on the amount and the sum price in the parameter
     * 
     * @param sumPrice the sumPrice of the item
     */
    public void setSumPrice(Money sumPrice) {
        this.pricePerUnit = new Money(sumPrice.getAmount() / amount, sumPrice.getCurrency());
    }

}
