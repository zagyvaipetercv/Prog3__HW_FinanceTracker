package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class UpdatingPurchasedItemModelFailed extends ErrorBoxException {

    private static final String ERROR_TITLE = "UPADTING PURCHASED ITEMS FAILED";

    public UpdatingPurchasedItemModelFailed(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
