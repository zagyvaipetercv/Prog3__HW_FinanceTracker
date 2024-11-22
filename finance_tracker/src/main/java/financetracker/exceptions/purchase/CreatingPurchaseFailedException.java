package financetracker.exceptions.purchase;

import financetracker.datatypes.Purchase;
import financetracker.exceptions.ErrorBoxException;

public class CreatingPurchaseFailedException extends ErrorBoxException {
    private static final String ERROR_TITLE = "CREATING PURCHASE FAILED";

    private final transient Purchase purchase;

    public CreatingPurchaseFailedException(String message, Purchase purchase) {
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
