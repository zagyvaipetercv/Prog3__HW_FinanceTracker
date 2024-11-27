package financetracker.models;

import java.util.List;

import javax.swing.DefaultListModel;
import financetracker.datatypes.Debt;

/**
 * List Model for Debt datatype
 * <p>
 * Inherits the DefaultListModel specified for debt
 */
public class DebtListModel extends DefaultListModel<Debt> {
    public DebtListModel(List<Debt> debt) {
        addAll(debt);
    }
}
