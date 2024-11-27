package financetracker.windowing.celleditors;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import financetracker.datatypes.Category;
import financetracker.windowing.ErrorBox;

/**
 * A TableCellEditor made for the Category datatype
 * <p>
 * When a cell is about to be adited the cell will become blank
 */
public class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField textfield = new JTextField();
    private Category currentCategory;

    private final Component parnetComponent;

    public CategoryCellEditor(Component parentComponent) {
        this.parnetComponent = parentComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
            int column) {
        if (value.getClass().equals(Category.class)) {
            currentCategory = (Category) value;
            textfield.setText("");
        }

        return textfield;
    }

    @Override
    public Object getCellEditorValue() {
        try {
            currentCategory.setName(textfield.getText());
        } catch (Exception e) {
            ErrorBox.show(parnetComponent, "ERROR", e.getMessage());
        }

        return currentCategory;
    }

}
