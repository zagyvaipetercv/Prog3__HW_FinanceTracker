package financetracker.datatypes;

import java.io.Serializable;

public class BoughtItem implements Serializable {
    private Category category;
    private String name;
    private Money pricePerUnit;
    private double amount;

    public BoughtItem(Category category, String name, Money pricePerUnit, double amount) {
        this.category = category;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.amount = amount;
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

    public Money getSumPrice() {
        if (pricePerUnit == null) {
            return null;
        }
        return new Money(amount * pricePerUnit.getAmount(), pricePerUnit.getCurrency());
    }

    public void setSumPrice(Money sumPrice) {
        this.pricePerUnit = new Money(sumPrice.getAmount() / amount, sumPrice.getCurrency());
    }
}
