package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class DeletingPurchaseFailed extends ErrorBoxException {
    private static final String ERROR_TITLE = "DELETING PURCHASE FAILED";

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
}
