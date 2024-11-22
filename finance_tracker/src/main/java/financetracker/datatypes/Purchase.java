package financetracker.datatypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

public class Purchase extends Model {
    private User user;
    private LocalDate dateOfPurchase;
    private List<BoughtItem> boughtItems;
    private CashFlow cashFlow;

    public Purchase(long id, User user, LocalDate dateOfPurchase, List<BoughtItem> boughtItems, CashFlow cashFlow) {
        super(id);
        this.user = user;
        this.dateOfPurchase = dateOfPurchase;
        this.boughtItems = new ArrayList<>(boughtItems);
        this.cashFlow = cashFlow;
    }

    public List<BoughtItem> getBoughtItems() {
        return boughtItems;
    }

    public List<BoughtItem> getBoughtItemsUnmodifiable() {
        return Collections.unmodifiableList(boughtItems);
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setDateOfPurchase(LocalDate dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBoughtItems(List<BoughtItem> boughtItems) {
        this.boughtItems = new ArrayList<>(boughtItems);
    }

    public Money getSumPrice() {
        double sum = 0.0;
        for (BoughtItem boughtItem : boughtItems) {
            sum += boughtItem.getSumPrice().getAmount();
        }

        return new Money(sum, Currency.getInstance("HUF"));
    }

    public CashFlow getCashFlow() {
        return cashFlow;
    }

    @Override 
    public String toString() {
        return getId() + " " + user.getName() + " " + dateOfPurchase.toString() + " " + getSumPrice().toString();
    }

    @Override 
    public boolean equals(Object o) {
        if (o.getClass() != Purchase.class) {
            return false;
        }

        return ((Purchase)o).getId() == getId();
    }

}
