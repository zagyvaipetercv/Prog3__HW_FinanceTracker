package financetracker.views.base;

import javax.swing.JFrame;

import financetracker.windowing.MyWindowConstants;

public abstract class FrameView extends JFrame {
    @Override
    public void setTitle(String subTitle) {
        super.setTitle(MyWindowConstants.TITLE + " - " + subTitle);
    }
}
