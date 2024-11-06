package financetracker.models;

public class BoughtItem extends Model {
    private Category category;
    private String name;
    private Money pricePerUnit;
    private double amount;

    public BoughtItem(long id, Category category, String name, Money pricePerUnit, double amount) {
        super(id);

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
}
