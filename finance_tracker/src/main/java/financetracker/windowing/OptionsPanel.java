package financetracker.windowing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OptionsPanel extends JPanel {
    public OptionsPanel() {
        setBorder(BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));
        setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, 0));
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        layout.setVgap(0);
        layout.setAlignOnBaseline(true);
        setLayout(layout);
    }

    public void addOptionButton(String text, ActionListener actionListener) {
        add(new OptionButton(text, actionListener));
    }

    private class OptionButton extends JButton {
        private static final int BUTTON_HEIGHT = 30;

        public OptionButton(String text, ActionListener actionListener) {
            super(text);
            setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, BUTTON_HEIGHT));
            addActionListener(actionListener);
        }
    }
}
