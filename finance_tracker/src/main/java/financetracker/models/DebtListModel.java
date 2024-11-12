package financetracker.models;

import java.util.List;

import javax.swing.DefaultListModel;
import financetracker.datatypes.Debt;

public class DebtListModel extends DefaultListModel<Debt> {
    public DebtListModel(List<Debt> debt) {
        addAll(debt);
    }
}
