package financetracker.windowing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import financetracker.views.HomeView;
import financetracker.views.PanelView;



public class MainFrame extends JFrame {
    private SidePanel sidePanel;
    private PanelView currentView;

    public MainFrame() {
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        sidePanel = new SidePanel();
        add(sidePanel, BorderLayout.WEST);

        currentView = new HomeView();
        add(currentView, BorderLayout.CENTER);

        setVisible(true);
    }
}
