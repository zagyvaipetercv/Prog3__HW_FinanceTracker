package financetracker.controllers;

import java.awt.event.ActionEvent;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import financetracker.exceptions.CannotCreateControllerException;
import financetracker.models.User;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.LoginWindow;


// FIXME: Everytime an ErrorBox is opened -> throw an exception
public class UserController extends Controller<User> {
    private static final String DEFAULT_SAVE_FILE_PATH = "saves\\users.dat";

    /**
     * Initializes the UserController
     * Collects metadata
     * Opens a Login Window
     * 
     * @throws CannotCreateControllerException if intialization fails
     */
    public UserController() throws CannotCreateControllerException {
        super(DEFAULT_SAVE_FILE_PATH);
    }

    // PUBLIC METHODS
    /**
     * Registers a user to the system with 'username' and 'password'
     * If fails error then an message will appear
     * 
     * @param event    event that calls this method
     * @param username username of the user
     * @param password password of the user
     * @return true if registration was succesfull, false if not
     */
    public boolean register(ActionEvent event, String username, String password) {
        // Check for errors
        if (usernameIsInvalid(username)) {
            ErrorBox.show("INVALID USERNAME", "Username cannot be blank or contain white spaces");
            return false;
        }

        if (passwordIsInvalid(password)) {
            ErrorBox.show("INVALID PASSWORD", "Password cannot contain white spaces or have less than 8 characters");
            return false;
        }

        try {
            if (userExists(username)) {
                ErrorBox.show("INVALID USERNAME", "User with \"" + username + "\" name already exists");
                return false;
            }
        } catch (ClassNotFoundException | IOException e) {
            ErrorBox.show("REGISTRATION FAILED", "Couldn't perform registration check (user exists)");
            e.printStackTrace();
            return false;
        }

        // If no error -> save
        try {
            User user = new User(getNextId(), username, password);
            appendNewData(user);
            return true;
        } catch (IOException e) {
            ErrorBox.show("REGISTRATION FAILED", "Registration failed due to an IO error");
            return false;
        }
    }

    /**
     * Tries to log in to the user with 'username' and 'password'
     * If fails error then an message will appear
     * 
     * @param event    event that calls this method
     * @param username username of the user
     * @param password password of the user
     * @return true if login was succesfull, false if not
     */
    public boolean login(ActionEvent event, String username, String password) {
        try {
            User user = findUser(username);
            if (user == null) {
                ErrorBox.show("INVALID USERNAME", "\"" + username + "\" is not a registered username");
                return false;
            }

            if (!user.getPassword().equals(password)) {
                ErrorBox.show("PASSWORDS DO NOT MATCH", "Passwords do not match");
                return false;
            }

            // TODO: Open MainFrame with logged in user
            System.out.println("Succesfull login");
            return true;

        } catch (ClassNotFoundException | IOException e) {
            ErrorBox.show("LOGIN FAILED", "Login failed due to an IO Error");
            return false;
        }
    }

    public void showLoginView() {
        new LoginWindow(this);
    }

    // CHECKS
    private boolean userExists(String username) throws IOException, ClassNotFoundException {
        User user = findUser(username);

        return user != null;
    }

    private boolean usernameIsInvalid(String username) {
        return (username.isBlank() ||
                username.contains(" "));
    }

    private boolean passwordIsInvalid(String password) {
        return (password.isBlank() ||
                password.contains(" ") ||
                password.length() < 8);
    }

    protected User findUser(String username) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilePath()))) {
            while (true) {
                User user = (User) ois.readObject();
                if (user == null) {
                    break;
                }

                if (username.equals(user.getName())) {
                    return user;
                }
            }
        } catch (EOFException e) {
            // ois reached end of file -> close()
            // [ois implements Closable -> no need to close manually]
        }

        return null;
    }
}
