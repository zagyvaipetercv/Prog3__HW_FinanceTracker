package financetracker.windowing;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;

public class LoginWindow extends JFrame {
    private static final String TITLE = "Finance Tracker - Login";
    private static final Dimension DEFAULT_SIZE = new Dimension(300, 150);

    private JPanel panel;

    private JButton loginButton;
    private JButton registerButton;

    private JLabel usernameLabel;
    private JLabel passwordLabel;

    private JTextField usernameTextField;
    private JTextField passwordTextField;

    private transient GroupLayout layout;

    public LoginWindow() {
        setTitle(TITLE);
        setSize(DEFAULT_SIZE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Window starts in the middle
        setResizable(false);

        // Building and adding the components
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        usernameTextField = new JTextField();
        passwordTextField = new JPasswordField();

        // Panel and Layout
        panel = new JPanel();
        layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Adding components to the panel:
        // Horizontal:
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(usernameLabel)
                .addComponent(passwordLabel)
                .addComponent(loginButton));

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(usernameTextField)
                .addComponent(passwordTextField)
                .addComponent(registerButton));

        layout.setHorizontalGroup(hGroup);

        // Vertical:
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(usernameLabel)
                .addComponent(usernameTextField));

        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(passwordLabel)
                .addComponent(passwordTextField));

        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(loginButton)
                .addComponent(registerButton));

        layout.setVerticalGroup(vGroup);

        this.add(panel);
        setVisible(true);
    }

}
