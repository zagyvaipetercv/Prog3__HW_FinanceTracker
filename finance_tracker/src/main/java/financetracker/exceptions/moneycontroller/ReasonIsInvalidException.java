package financetracker.exceptions.moneycontroller;

import financetracker.exceptions.ErrorBoxException;

public class ReasonIsInvalidException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID REASON";

    private final transient String reason;

    public ReasonIsInvalidException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
