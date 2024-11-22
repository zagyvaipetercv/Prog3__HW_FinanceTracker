package financetracker.exceptions.purchase;

import financetracker.datatypes.Purchase;
import financetracker.exceptions.ErrorBoxException;

public class EditingPurchaseFailed extends ErrorBoxException {

    private static final String ERROR_TITLE = "EDITING FAILED";
    private final transient Purchase purchase;

    public EditingPurchaseFailed(String message, Purchase purchase) {
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
