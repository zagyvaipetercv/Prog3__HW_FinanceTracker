package financetracker.windowing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import financetracker.models.User;
import financetracker.views.HomeView;
import financetracker.views.PanelView;

public class MainFrame extends JFrame {
    private static final String TITLE = "Finance Tracker";
    private static final Dimension DEFAULT_SIZE = new Dimension(1280, 720);

    private PanelView currentView;
    private SidePanel sidePanel;
    
    private String subTitle;
    private User user;
    
    public MainFrame(User userSignedIn) {
        super();

        subTitle = "Home";
        user = userSignedIn;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(TITLE + " - " + subTitle);
        setLayout(new BorderLayout());
        setSize(DEFAULT_SIZE);
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
}
