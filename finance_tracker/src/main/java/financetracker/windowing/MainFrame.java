package financetracker.windowing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import financetracker.controllers.CashFlowController;
import financetracker.controllers.CategoryController;
import financetracker.controllers.DebtController;
import financetracker.controllers.PurchaseController;
import financetracker.controllers.UserController;
import financetracker.datatypes.User;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.generic.ChangingViewFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.views.base.PanelView;

/**
 * The main frame of the application that will be opened after succesfull login
 */
public class MainFrame extends JFrame {
    private static final Dimension DEFAULT_SIZE = new Dimension(1280, 720);
    private static final Dimension MIN_SIZE = new Dimension(1000, 600);

    private PanelView currentView;
    private SidePanel sidePanel;

    private String subTitle;
    private User user;

    private transient UserController userController;
    private transient CashFlowController cashFlowController;
    private transient DebtController debtController;
    private transient PurchaseController purchaseController;
    private transient CategoryController categoryController;

    // FOR-TESTING
    public MainFrame(
            User userSignedIn,
            String userFilePath,
            String cashFLowFilePath,
            String debtFielPath,
            String purchaseFilePath,
            String categoryFilePath) {
        user = userSignedIn;

        try {
            userController = new UserController(userFilePath, this);
            cashFlowController = new CashFlowController(cashFLowFilePath, this);
            categoryController = new CategoryController(categoryFilePath, this);
            debtController = new DebtController(debtFielPath, this);
            purchaseController = new PurchaseController(purchaseFilePath, this);
        } catch (ControllerWasNotCreated e) {
            ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
            System.exit(-1);
        }

        subTitle = "Home";

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(subTitle);
        setLayout(new BorderLayout());
        setSize(DEFAULT_SIZE);
        setMinimumSize(MIN_SIZE);
        setLocationRelativeTo(null);

        sidePanel = new SidePanel(this);
        this.add(sidePanel, BorderLayout.WEST);

        try {
            currentView = cashFlowController.getWalletView();
        } catch (UpdatingModelFailed e) {
            ErrorBox.show(this, e);
        }
        add(currentView);
    }

    public MainFrame(User userSignedIn) {
        super();
        user = userSignedIn;

        try {
            userController = new UserController(this);
            cashFlowController = new CashFlowController(this);
            categoryController = new CategoryController(this);
            debtController = new DebtController(this);
            purchaseController = new PurchaseController(this);
        } catch (ControllerWasNotCreated e) {
            ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
            System.exit(-1);
        }

        subTitle = "Wallet";

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(subTitle);
        setLayout(new BorderLayout());
        setSize(DEFAULT_SIZE);
        setMinimumSize(MIN_SIZE);
        setLocationRelativeTo(null);

        sidePanel = new SidePanel(this);
        this.add(sidePanel, BorderLayout.WEST);

        try {
            currentView = cashFlowController.getWalletView();
        } catch (UpdatingModelFailed e) {
            ErrorBox.show(this, e);
        }
        add(currentView);

        setVisible(true);
    }

    @Override
    public void setTitle(String subTitle) {
        this.subTitle = subTitle;
        super.setTitle(MyWindowConstants.TITLE + " - " + subTitle);
    }

    public void changeView(PanelView panelView) {
        getContentPane().remove(currentView);

        currentView = panelView;
        add(currentView);

        revalidate();
        repaint();
    }

    private class SidePanel extends JPanel {
        private static final int PREFFERED_WIDTH = 250;

        public SidePanel(MainFrame mainFrame) {
            super();

            setPreferredSize(new Dimension(PREFFERED_WIDTH, 0));
            FlowLayout layout = new FlowLayout();
            layout.setAlignOnBaseline(true);
            layout.setHgap(0);
            layout.setVgap(0);
            setLayout(layout);

            setBackground(getBackground().darker());

            NavButton walletButton = new NavButton("Wallet");
            walletButton.addActionListener(ae -> {
                try {
                    mainFrame.changeView(cashFlowController.getWalletView());
                } catch (UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
                mainFrame.setTitle("Wallet");
            });
            add(walletButton);

            NavButton debtsButton = new NavButton("Debts");
            debtsButton.addActionListener(ae -> {
                try {
                    mainFrame.changeView(debtController.getDebtView());
                } catch (UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
                mainFrame.setTitle("Debts");
            });
            add(debtsButton);

            NavButton purchasesButton = new NavButton("Purchases");
            purchasesButton.addActionListener(ae -> {
                try {
                    mainFrame.changeView(purchaseController.getPurchaseView());
                } catch (UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
                mainFrame.setTitle("Purchases");
            });
            add(purchasesButton);

            NavButton purchasedItemsButton = new NavButton("Purchased Items");
            purchasedItemsButton.addActionListener(ae -> {
                try {
                    mainFrame.changeView(purchaseController.getPurchsedItemsView());
                } catch (ChangingViewFailed | UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
                mainFrame.setTitle("Purchasd Items");
            });
            add(purchasedItemsButton);
        }

        private class NavButton extends JButton {
            private static final int PREFFERED_HEIGHT = 50;

            public NavButton(String text) {
                super(text);
                setPreferredSize(new Dimension(PREFFERED_WIDTH, PREFFERED_HEIGHT));
            }
        }
    }

    /**
     * Returns the user that is signed in
     * 
     * @return the user that is signed in
     */
    public User getUserLogedIn() {
        return user;
    }

    /**
     * Returns the UserController used by the program
     * 
     * @return the UserController used by the program
     */
    public UserController getUserController() {
        return userController;
    }

    /**
     * Returns the DebtController used by the program
     * 
     * @return the DebtController used by the program
     */
    public DebtController getDebtController() {
        return debtController;
    }

    /**
     * Returns the CashFlowController used by the program
     * 
     * @return the CashFlowController used by the program
     */
    public CashFlowController getCashFlowController() {
        return cashFlowController;
    }

    /**
     * Returns the CategoryController used by the program
     * 
     * @return the CategoryController used by the program
     */
    public CategoryController getCategoryController() {
        return categoryController;
    }

    
    /**
     * Returns the PurchaseController used by the program
     * 
     * @return the PurchaseController used by the program
     */
    public PurchaseController getPurchaseController() {
        return purchaseController;
    }
}
