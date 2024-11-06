package financetracker.windowing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import financetracker.views.HomeView;
import financetracker.views.WalletView;

public class SidePanel extends JPanel {
    private static final int PREFFERED_WIDTH = 250;
    private static final Color BACKGROUND_COLOR = new Color(100,100,100);

    public SidePanel(MainFrame mainFrame) {
        super();
        
        setPreferredSize(new Dimension(PREFFERED_WIDTH, 0));
        FlowLayout layout = new FlowLayout();
        layout.setAlignOnBaseline(true);
        layout.setHgap(0);
        layout.setVgap(0);
        setLayout(layout);

        setBackground(BACKGROUND_COLOR);
        
        NavButton homeButton = new NavButton("Home");
        homeButton.addActionListener(actionEvent -> {
            mainFrame.changeView(new HomeView(mainFrame.getUserName()));
            mainFrame.setTitle("Home");
        });
        add(homeButton);

        NavButton walletButton = new NavButton("Wallet");
        walletButton.addActionListener(ae -> {
            mainFrame.changeView(new WalletView());
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
