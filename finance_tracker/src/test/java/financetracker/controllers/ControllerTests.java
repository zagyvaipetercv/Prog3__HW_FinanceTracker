package financetracker.controllers;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;

import financetracker.datatypes.User;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.windowing.MainFrame;

public abstract class ControllerTests {
    protected static final String TEST_USER_FILE_PATH = "test_data\\user_test.dat";
    protected static final String TEST_CASHFLOW_FILE_PATH = "test_data\\cashlfow_test.dat";
    protected static final String TEST_CATEGORY_FILE_PATH = "test_data\\category_test.dat";
    protected static final String TEST_DEBT_FILE_PATH = "test_data\\debt_test.dat";
    protected static final String TEST_PURCHASE_FILE_PATH = "test_data\\purchase_test.dat";

    protected final String userName1 = "test_user_1";
    protected final String userName2 = "test_user_2";
    protected final String userName3 = "test_user_3";

    protected final String defaultPassword = "12345678";

    protected MainFrame mainFrame;

    protected User userSignedIn;

    protected static String[] SAVE_FILE_PATHS = {
            TEST_USER_FILE_PATH,
            TEST_CASHFLOW_FILE_PATH,
            TEST_CATEGORY_FILE_PATH,
            TEST_DEBT_FILE_PATH,
            TEST_PURCHASE_FILE_PATH
    };

    @BeforeEach
    public void setupBase() throws ControllerWasNotCreated, InvalidUserNameException, InvalidPasswordException,
            RegistrationFailedException, UserNotFound {
        // Make parent direcotries
        for (String filePath : SAVE_FILE_PATHS) {
            File saveFile = new File(filePath);
            if (!saveFile.exists()) {
                File parentDir = saveFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) { // If should have a parent directory but does not exist
                    parentDir.mkdirs(); // Make parent dirs
                }
            }
            else { // Delete save file
                saveFile.delete();
            }
        }

        UserController userController = new UserController(TEST_USER_FILE_PATH, null);
        userController.register(userName1, defaultPassword);
        userController.register(userName2, defaultPassword);
        userController.register(userName3, defaultPassword);

        userSignedIn = userController.findUser(userName1);

        mainFrame = new MainFrame(
            userSignedIn,
            TEST_USER_FILE_PATH,
            TEST_CASHFLOW_FILE_PATH,
            TEST_DEBT_FILE_PATH,
            TEST_PURCHASE_FILE_PATH,
            TEST_CATEGORY_FILE_PATH);
    }

}
