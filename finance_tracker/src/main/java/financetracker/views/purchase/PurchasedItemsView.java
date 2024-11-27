package financetracker.views.purchase;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.PurchaseController;
import financetracker.datatypes.Category;
import financetracker.exceptions.generic.ChangingViewFailed;
import financetracker.exceptions.generic.FilteringFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.models.DetailedPurchaseTableModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.customtables.PurchasedItemsTable;

/**
 * PanelView where the user can see detailed information about their purchases
 */
public class PurchasedItemsView extends PanelView {
    private DetailedPurchaseTableModel detailedPurchaseTableModel;
    private PurchaseController purchaseController;

    private LocalDate startDate;
    private LocalDate endDate;
    private Category category;

    private ArrayList<String> categoryNames;

    public PurchasedItemsView(PurchaseController purchaseController,
            DetailedPurchaseTableModel detailedPurchaseTableModel,
            LocalDate startDate, LocalDate endDate, Category category,
            List<String> categoryNames) {

        this.detailedPurchaseTableModel = detailedPurchaseTableModel;
        this.purchaseController = purchaseController;

        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.categoryNames = new ArrayList<>(categoryNames);
        this.categoryNames.add(0, "");

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        PurchasedItemsTable table = new PurchasedItemsTable(detailedPurchaseTableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(new FilterPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class FilterPanel extends JPanel {
        private DatePicker startDatePicker;
        private DatePicker endDatePicker;
        private JComboBox<String> categoryComboBox;
        private JButton filterButton;

        public FilterPanel() {
            initComponents();
            addButtonListeners();
        }

        private void addButtonListeners() {
            filterButton.addActionListener(
                    ae -> {
                            try {
                                purchaseController.filterPurchasedItems(startDatePicker.getDate(), endDatePicker.getDate(),
                                        (String) categoryComboBox.getSelectedItem());
                                purchaseController.refreshPurchasedItemsView();
                            } catch (FilteringFailed | UpdatingModelFailed | ChangingViewFailed e) {
                                ErrorBox.show(this, e);
                            }
                    });
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(true);
            panel.setLayout(layout);

            JLabel startDateLabel = new JLabel("From:");
            JLabel endDateLabel = new JLabel("To:");
            JLabel categoryLabel = new JLabel("Category:");

            startDatePicker = new DatePicker();
            startDatePicker.setDate(startDate);

            endDatePicker = new DatePicker();
            endDatePicker.setDate(endDate);

            categoryComboBox = new JComboBox<>(categoryNames.toArray(new String[0]));
            if (category != null) {
                categoryComboBox.setSelectedItem(category.getName());
            }

            filterButton = new JButton("Fitler");

            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(startDateLabel)
                    .addComponent(startDatePicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(endDateLabel)
                    .addComponent(endDatePicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(categoryLabel)
                    .addComponent(categoryComboBox));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(filterButton));

            layout.setHorizontalGroup(hGroup);

            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(startDateLabel)
                    .addComponent(endDateLabel)
                    .addComponent(categoryLabel));

            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(startDatePicker)
                    .addComponent(endDatePicker)
                    .addComponent(categoryComboBox)
                    .addComponent(filterButton));

            layout.setVerticalGroup(vGroup);

            add(panel, BorderLayout.CENTER);
        }
    }

}
