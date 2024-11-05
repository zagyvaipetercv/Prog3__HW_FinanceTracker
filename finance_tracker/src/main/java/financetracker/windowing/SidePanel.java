package financetracker.windowing;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SidePanel extends JPanel {
    private static final int PREFFERED_WIDTH = 200;

    public SidePanel() {
        setLayout(new GridLayout(10, 1));
        setPreferredSize(new Dimension(PREFFERED_WIDTH, 0));


        JButton button1 = new JButton("1");
        add(button1,0);

        JButton button2 = new JButton("2");
        add(button2,1);

        JButton button3 = new JButton("3");
        add(button3, 2);

        JButton button4 = new JButton("4");
        add(button4, 3);
    }
}
