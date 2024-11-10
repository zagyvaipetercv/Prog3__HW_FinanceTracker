package financetracker.datatypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Purchase extends Model {
    private LocalDate dateOfPurchase;
    private List<BoughtItem> boughtItems;

    public Purchase(long id, LocalDate dateOfPurchase, List<BoughtItem> boughtItems) {
        super(id);
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

}
