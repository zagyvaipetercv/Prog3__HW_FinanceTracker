package financetracker.exceptions.purchase;

import financetracker.datatypes.Purchase;
import financetracker.exceptions.ErrorBoxException;

public class DeletingPurchaseFailed extends ErrorBoxException {
    private static final String ERROR_TITLE = "DELETING PURCHASE FAILED";

    private final transient Purchase purchase;

    public DeletingPurchaseFailed(String message, Purchase purchase) {
        super(message);
        this.purchase = purchase;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Purchase getPurchase() {
        return purchase;
    }
}
