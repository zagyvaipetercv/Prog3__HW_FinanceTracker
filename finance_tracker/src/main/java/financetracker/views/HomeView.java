package financetracker.views;

import javax.swing.JLabel;

public class HomeView extends PanelView {
    public HomeView(String userName) {
        JLabel welcomeLabel = new JLabel("Welcome " + userName);
        add(welcomeLabel);
    }
}