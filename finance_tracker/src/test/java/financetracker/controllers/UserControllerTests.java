package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.datatypes.User;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.LoginFailedException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.exceptions.usercontroller.UserNotFound;

class UserControllerTests extends ControllerTests {

    private String validUsername1 = "ValidUsername";
    private String validPassword1 = "ValidPassword";

    private String validUsername2 = "VldUsername";
    private String validPassword2 = "12345678";

    private String invalidUsernameBlank = "";
    private String invalidUsernameHasOnlySpace = "      ";
    private String invalidUsernameHasSpace = "Invalid Username";

    private String invalidPasswordBlank = "";
    private String invalidPasswordHasOnlySpace = "     ";
    private String invalidPasswordNotEnoughChars = "1234567";

    private UserController userController;

    @BeforeEach
    public void setupController() throws ControllerWasNotCreated  {
        userController = new UserController(TEST_USER_FILE_PATH, null);
    }

    @Test
    void checkRegisterSucceds() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        boolean succes = userController.register(validUsername1, validPassword1);
        assertEquals(true, succes);
    }

    @Test
    void checkFindUser() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException, UserNotFound {
        boolean succes = userController.register(validUsername1, validPassword1);
        User user = userController.findUser(validUsername1);
        assertEquals(true, succes);
        assertEquals(validUsername1, user.getName());
        assertEquals(validPassword1, user.getPassword());
        assertEquals(4, user.getId()); // In parent test class I added 3 other users before -> this user has to be the 4th user -> id = 4 
    }

    @Test
    void checkLoginUser() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException,
            LoginFailedException {
        boolean succesRegister = userController.register(validUsername1, validPassword1);
        boolean succesLogin = userController.login(validUsername1, validPassword1);

        assertEquals(true, succesRegister);
        assertEquals(true, succesLogin);
    }

    @Test
    void checkWrongLogin() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        boolean succesReg2 = userController.register(validUsername2, validPassword2);
        boolean succesReg1 = userController.register(validUsername1, validPassword1);

        assertThrows(LoginFailedException.class, () -> userController.login(validUsername1, validPassword2)); // Login
                                                                                                                        // with
                                                                                                                        // wrong
                                                                                                                        // wrong
                                                                                                                        // password
        assertEquals(true, succesReg1);
        assertEquals(true, succesReg2);
    }

    @Test
    void checkInvalidUserName1() {
        assertThrows(InvalidUserNameException.class,
                () -> userController.register(invalidUsernameBlank, validPassword1));
    }

    @Test
    void checkInvalidUserName2() {
        assertThrows(InvalidUserNameException.class,
                () -> userController.register(invalidUsernameHasOnlySpace, validPassword1));
    }

    @Test
    void checkInvalidUserName3() {
        assertThrows(InvalidUserNameException.class,
                () -> userController.register(invalidUsernameHasSpace, validPassword1));
    }

    @Test
    void checkInvalidPassword1() {
        assertThrows(InvalidPasswordException.class,
                () -> userController.register(validUsername1, invalidPasswordBlank));
    }

    @Test
    void checkInvalidPassword2() {
        assertThrows(InvalidPasswordException.class,
                () -> userController.register(validUsername1, invalidPasswordHasOnlySpace));
    }

    @Test
    void checkInvalidPassword3() {
        assertThrows(InvalidPasswordException.class,
                () -> userController.register(validUsername1, invalidPasswordNotEnoughChars));
    }
}
