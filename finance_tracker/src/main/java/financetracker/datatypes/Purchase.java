package financetracker.datatypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Purchase extends Model {
    private User user;
    private LocalDate dateOfPurchase;
    private List<BoughtItem> boughtItems;

    public Purchase(long id, User user, LocalDate dateOfPurchase, List<BoughtItem> boughtItems) {
        super(id);
        this.user = user;
        this.dateOfPurchase = dateOfPurchase;
        this.boughtItems = new ArrayList<>(boughtItems);
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
        this.boughtItems = boughtItems;
    }

}
