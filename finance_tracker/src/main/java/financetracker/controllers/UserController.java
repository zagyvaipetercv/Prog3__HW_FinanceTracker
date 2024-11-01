package financetracker.controllers;

import java.awt.event.ActionEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import financetracker.exceptions.CannotCreateUserControllerException;
import financetracker.models.User;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.LoginWindow;

public class UserController implements Controller {
    private static final String FILE_PATH = "saves\\users.dat";
    private static long nextID = 1;

    /**
     * Initializes the UserController
     * Collects metadata
     * Opens a Login Window
     * 
     * @throws CannotCreateUserControllerException if intialization fails
     */
    public UserController() throws CannotCreateUserControllerException {
        try {
            createSaveFile();
            initNextId();
        } catch (IOException | ClassNotFoundException e) {
            throw new CannotCreateUserControllerException(UserController.class, "IO Exception occured");
        }
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
            createAndSaveUser(username, password);
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

    private User findUser(String username) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
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

    // SERAILIZATION
    private void createAndSaveUser(String name, String password) throws IOException {
        User user = new User(nextID, name, password);
        UserController.increaseNextId();
        
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH));
        oos.writeObject(user);
        oos.close();

    }

    private void createSaveFile() throws IOException, CannotCreateUserControllerException {
        File saveFile = new File(FILE_PATH);

        if (!saveFile.exists()) {
            boolean succes = saveFile.createNewFile();
            if (!succes) {
                throw new CannotCreateUserControllerException(UserController.class, "Save file exists when it shouldn't");
            }
        }
    }



    // INITIALIZATION
    private void initNextId() throws IOException, ClassNotFoundException {
        UserController.setDefaultNextId();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            while (true) {
                ois.readObject();
                UserController.increaseNextId();
            }
        } catch (EOFException e) {
            // ois reached end of file -> close()
            // [ois implements Closable -> no need to close manually]
        }
    }

    private static void increaseNextId() {
        nextID++;
    }

    private static void setDefaultNextId() {
        nextID = 1;
    }
}
