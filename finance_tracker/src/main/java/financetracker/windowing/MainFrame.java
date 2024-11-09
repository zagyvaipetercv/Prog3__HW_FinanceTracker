package financetracker.windowing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import financetracker.controllers.CashFlowController;
import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.models.User;
import financetracker.views.HomeView;
import financetracker.views.base.PanelView;

public class MainFrame extends JFrame {
    private static final String TITLE = "Finance Tracker";
    private static final Dimension DEFAULT_SIZE = new Dimension(1280, 720);
    private static final Dimension MIN_SIZE = new Dimension(1000, 600);

    private PanelView currentView;
    private SidePanel sidePanel;

    private String subTitle;
    private User user;

    private transient CashFlowController cashFlowController;

    public MainFrame(User userSignedIn) {
        super();
        
        try {
            this.cashFlowController = new CashFlowController(this);
        } catch (CannotCreateControllerException | ControllerCannotReadException e) {
            ErrorBox.show("ERROR", e.getMessage());
            System.exit(-1);
        }

        subTitle = "Home";
        user = userSignedIn;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(TITLE + " - " + subTitle);
        setLayout(new BorderLayout());
        setSize(DEFAULT_SIZE);
        setMinimumSize(MIN_SIZE);
        setLocationRelativeTo(null);

        sidePanel = new SidePanel(this);
        this.add(sidePanel, BorderLayout.WEST);

        currentView = new HomeView(getUserName());
        add(currentView);

        setVisible(true);
    }

    @Override
    public void setTitle(String subTitle) {
        this.subTitle = subTitle;
        super.setTitle(TITLE + " - " + subTitle);
    }

    public void changeView(PanelView panelView) {
        getContentPane().remove(currentView);

        currentView = panelView;
        add(currentView);

        revalidate();
        repaint();
    }

    public String getUserName() {
        return user.getName();
    }

    public long getUserId() {
        return user.getId();
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

            NavButton homeButton = new NavButton("Home");
            homeButton.addActionListener(actionEvent -> {
                mainFrame.changeView(new HomeView(mainFrame.getUserName()));
                mainFrame.setTitle("Home");
            });
            add(homeButton);

            NavButton walletButton = new NavButton("Wallet");
            walletButton.addActionListener(ae -> {
                mainFrame.changeView(cashFlowController.getWalletView());
                mainFrame.setTitle("Wallet");
            });
            add(walletButton);
        }

        private class NavButton extends JButton {
            private static final int PREFFERED_HEIGHT = 50;

            public NavButton(String text) {
                super(text);
                setPreferredSize(new Dimension(PREFFERED_WIDTH, PREFFERED_HEIGHT));
            }
        }
    }
}
