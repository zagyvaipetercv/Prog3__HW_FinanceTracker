package financetracker.windowing;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SidePanel extends JPanel {
    private static final int PREFFERED_WIDTH = 200;

    public SidePanel() {
        setPreferredSize(new Dimension(PREFFERED_WIDTH, 0));
    }


    private class NavButton extends JButton {

    }
}
