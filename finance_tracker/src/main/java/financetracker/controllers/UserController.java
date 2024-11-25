package financetracker.controllers;

import java.util.List;

import financetracker.datatypes.User;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.LoginFailedException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.views.LoginWindow;
import financetracker.views.base.FrameView;
import financetracker.windowing.MainFrame;

public class UserController extends Controller<User> {

    private static final String DEFAULT_SAVE_PATH = "saves\\users.dat";

    /**
     * Initializes the UserController
     * Collects metadata
     * Opens a Login Window
     * 
     * @throws CannotCreateControllerException if intialization fails
     */
    public UserController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public UserController(String filePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(filePath, mainFrame);
    }

    // PUBLIC METHODS
    /**
     * Registers a user to the system with 'username' and 'password'
     * 
     * @param username username of the user
     * @param password password of the user
     * @return true if registration was succesfull, false if not
     * @throws RegistrationFailedException if registration failed due to an IO Error
     * @throws InvalidUserNameException    if username is blank, has whitespace or a
     *                                     user with the same name already exists
     * @throws InvalidPasswordException    if the psasword is blank has whitespace
     *                                     or has less than 8 characters
     */
    public boolean register(String username, String password)
            throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        // Check for errors
        if (usernameIsInvalid(username)) {
            throw new InvalidUserNameException("Username can't be blank or have whitespaces");
        }

        if (passwordIsInvalid(password)) {
            throw new InvalidPasswordException(
                    "Password must have at least 8 characters and can't be blank or have white spaces");
        }

        if (userExists(username)) {
            throw new InvalidUserNameException("Username is already taken");
        }

        // If no error -> save
        try {
            User user = new User(modelSerializer.getNextId(), username, password);
            modelSerializer.appendNewData(user);
            return true;
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new RegistrationFailedException("Registration failed due to an IO error");
        }
    }

    /**
     * Tries to log in to the user with 'username' and 'password'
     * <p>
     * If login succeds the mainFrame will be initialized and set visible
     * 
     * @param username username of the user
     * @param password password of the user
     * @return true if login was succesfull, false if not
     * @throws LoginFailedException if user with username does not exist or
     *                              passwords don't match
     */
    public boolean login(String username, String password) throws LoginFailedException {
        try {
            User user = findUser(username);
            if (!user.getPassword().equals(password)) {
                throw new LoginFailedException("Login failed - Passwords don't match");
            }

            mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);
            return true;

        } catch (UserNotFound e) {
            throw new LoginFailedException("Login failed - User was not found");
        }
    }

    /**
     * Returns a LoginWindow where the user can log in to their account or register
     * a new account
     * 
     * @return a LoginWindow
     */
    public FrameView getLoginView() {
        return new LoginWindow(this);
    }

    // CHECKS
    /**
     * Checks if the save file has a record with the username
     * 
     * @param username the username of the user
     * @return true if user exists
     */
    private boolean userExists(String username) {
        try {
            User user = findUser(username);
            return user != null;
        } catch (UserNotFound e) {
            return false;
        }
    }

    /**
     * Checks if username is invalid
     * <p>
     * A username is invalid if it's blank or contains white spaces
     * 
     * @param username the username that needs to be checked
     * @return true if username is blank or contains white spaces,
     *         false otherwise
     */
    private boolean usernameIsInvalid(String username) {
        return (username.isBlank() ||
                username.contains(" "));
    }

    /**
     * Checks if password is invalid
     * <p>
     * A password is invalid if it's blank or contains white spaces or has less than
     * 8 characters
     * 
     * @param password the password that needs to be checked
     * @return true if password is blank or contains white spaces or has less than 8
     *         characters, false otherwise
     */
    private boolean passwordIsInvalid(String password) {
        return (password.isBlank() ||
                password.contains(" ") ||
                password.length() < 8);
    }

    /**
     * Finds a user with a specific username
     * 
     * @param username the username the user has
     * @return the user with the username
     * @throws UserNotFound if user was not found due to an IO Error or user does not exist
     */
    public User findUser(String username) throws UserNotFound {
        try {
            List<User> users = modelSerializer.readAll();
            for (User user : users) {
                if (user.getName().equals(username)) {
                    return user;
                }
            }
        } catch (SerializerCannotRead e) {
            throw new UserNotFound(username, "User not found due to IO Error");
        }

        throw new UserNotFound(username, "User with specified username was not found");
    }
}
