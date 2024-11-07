package financetracker.controllers;

import java.awt.event.ActionEvent;
import java.util.List;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.LoginFailedException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.models.User;
import financetracker.views.LoginWindow;
import financetracker.views.bases.FrameView;
import financetracker.windowing.MainFrame;

public class UserController extends Controller<User> {
    private static final String DEFAULT_SAVE_FILE_PATH = "saves\\users.dat";

    /**
     * Initializes the UserController
     * Collects metadata
     * Opens a Login Window
     * 
     * @throws CannotCreateControllerException if intialization fails
     */
    public UserController(MainFrame mainFrame) throws CannotCreateControllerException {
        this(DEFAULT_SAVE_FILE_PATH, mainFrame);
    }

    public UserController(String filePath, MainFrame mainFrame) throws CannotCreateControllerException {
        super(filePath, mainFrame);
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
     * @throws RegistrationFailedException
     */
    public boolean register(ActionEvent event, String username, String password)
            throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        // Check for errors
        if (usernameIsInvalid(username)) {
            throw new InvalidUserNameException(InvalidUserNameException.ErrorType.REGISTRATION_ALREADY_EXISTS);
        }

        if (passwordIsInvalid(password)) {
            throw new InvalidPasswordException(InvalidPasswordException.ErrorType.NOT_ENOUGH_CHARACTERS_OR_WHITESPACE);
        }

        try {
            if (userExists(username)) {
                throw new InvalidUserNameException(InvalidUserNameException.ErrorType.REGISTRATION_ALREADY_EXISTS);
            }
        } catch (ControllerCannotReadException e) {
            throw new RegistrationFailedException("Can not check if user already exists");
        }

        // If no error -> save
        try {
            User user = new User(getNextId(), username, password);
            appendNewData(user);
            return true;
        } catch (ControllerCannotReadException | ControllerCannotWriteException e) {
            throw new RegistrationFailedException("Registration failed due to an IO error");
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
     * @throws LoginFailedException
     * @throws InvalidPasswordException
     * @throws InvalidUserNameException
     */
    public boolean login(ActionEvent event, String username, String password)
            throws LoginFailedException, InvalidPasswordException, InvalidUserNameException {
        try {
            User user = findUser(username);
            if (user == null) {
                throw new InvalidUserNameException(InvalidUserNameException.ErrorType.LOGIN_NOT_REGISTERED_USERNAME);
            }

            if (!user.getPassword().equals(password)) {
                throw new InvalidPasswordException(InvalidPasswordException.ErrorType.PASSWORDS_DO_NOT_MATCH);
            }

            mainFrame = new MainFrame(user);
            return true;

        } catch (ControllerCannotReadException e) {
            throw new LoginFailedException("Login failed to an Read Exception");
        }
    }

    public FrameView getLoginView() {
        return new LoginWindow(this);
    }

    // CHECKS
    private boolean userExists(String username) throws ControllerCannotReadException {
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

    protected User findUser(String username) throws ControllerCannotReadException {
        List<User> users = readAll();
        for (User user : users) {
            if (user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }
}
