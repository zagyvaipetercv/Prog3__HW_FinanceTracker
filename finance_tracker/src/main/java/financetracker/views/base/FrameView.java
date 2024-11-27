package financetracker.views.base;

import javax.swing.JFrame;

import financetracker.windowing.MyWindowConstants;

/**
 * Base class for every view that will be displayed in a JFrame
 */
public abstract class FrameView extends JFrame {

    /**
     * Overrides JFrames setTitle()
     * <p>
     * It will always start with "Finance Tracker - "
     * @param subTitle the subTitle of the window. It can be seen after "Finance Tracker - " 
     */
    @Override
    public void setTitle(String subTitle) {
        super.setTitle(MyWindowConstants.TITLE + " - " + subTitle);
    }
}
