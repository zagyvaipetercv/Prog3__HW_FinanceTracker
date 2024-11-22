package financetracker.windowing.celleditors;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

public class CustomDoubleCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField textfield = new JTextField();
    private Double currentDouble;

    private final Component pernatComponent;

    public CustomDoubleCellEditor(Component pernatComponent) {
        this.pernatComponent = pernatComponent;
    }

    @Override
    public Object getCellEditorValue() {
        try {
            currentDouble = Double.parseDouble(textfield.getText());
        } catch (Exception e) {
            ErrorBox.show(pernatComponent, "ERROR", "'" + textfield.getText() + "' is not a valid amount");
            currentDouble = 0.0;
        }

        return currentDouble;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
            int column) {

        textfield.setText("");

        return textfield;
    }
}
