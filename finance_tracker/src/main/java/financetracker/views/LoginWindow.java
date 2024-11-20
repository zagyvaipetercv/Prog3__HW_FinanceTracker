package financetracker.views;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import financetracker.controllers.UserController;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.LoginFailedException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

import javax.swing.GroupLayout.Alignment;

public class LoginWindow extends FrameView {
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
        private transient UserController userController;

        public LoginWindow(UserController userController) {
                this.userController = userController;

                setTitle(TITLE);
                setSize(DEFAULT_SIZE);
                setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                setLocationRelativeTo(null); // Window starts in the middle
                setResizable(false);

                // Building and adding the components
                usernameLabel = new JLabel("Username:");
                passwordLabel = new JLabel("Password:");

                usernameTextField = new JTextField();
                passwordTextField = new JPasswordField();

                loginButton = new JButton("Login");
                loginButton.addActionListener(event -> {
                        try {
                                this.userController.login(
                                                event,
                                                usernameTextField.getText(),
                                                passwordTextField.getText());
                                
                                userController.closeFrameView(this);
                        } catch (LoginFailedException | InvalidPasswordException | InvalidUserNameException e) {
                                ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
                        }
                });

                registerButton = new JButton("Register");
                registerButton.addActionListener(event -> {
                        try {
                                this.userController.register(
                                                event,
                                                usernameTextField.getText(),
                                                passwordTextField.getText());
                        } catch (InvalidUserNameException | InvalidPasswordException | RegistrationFailedException e) {
                                ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
                        }
                });

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
