package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.usercontroller.InvalidPasswordException;
import financetracker.exceptions.usercontroller.InvalidUserNameException;
import financetracker.exceptions.usercontroller.LoginFailedException;
import financetracker.exceptions.usercontroller.RegistrationFailedException;
import financetracker.models.User;

public class UserControllerTests {
    private static final String TEST_SAVE_FILE_PATH = "test_data\\saves\\test_user.dat";
    
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
    public void setUp() throws CannotCreateControllerException {
        File testSaveFile = new File(TEST_SAVE_FILE_PATH);
        if (testSaveFile.exists()) {
            testSaveFile.delete();
        }

        userController = new UserController(TEST_SAVE_FILE_PATH);
    }

    @Test
    public void checkRegisterSucceds() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        boolean succes = userController.register(null, validUsername1, validPassword1);
        assertEquals(true, succes);
    }

    @Test
    public void checkFindUser() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException, ControllerCannotReadException  {
        boolean succes = userController.register(null, validUsername1, validPassword1);
        User user = userController.findUser(validUsername1);
        assertEquals(true, succes);
        assertEquals(validUsername1, user.getName());
        assertEquals(validPassword1, user.getPassword());
        assertEquals(1, user.getId());
    }

    @Test
    public void checkLoginUser() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException, LoginFailedException {
        boolean succesRegister = userController.register(null, validUsername1, validPassword1);
        boolean succesLogin = userController.login(null, validUsername1, validPassword1);
        
        assertEquals(true, succesRegister);
        assertEquals(true, succesLogin);
    }

    @Test
    public void checkWrongLogin() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException, LoginFailedException {
        boolean succesReg2 = userController.register(null, validUsername2, validPassword2);
        boolean succesReg1 = userController.register(null, validUsername1, validPassword1);

        assertThrows(InvalidPasswordException.class, () -> userController.login(null, validUsername1, validPassword2)); // Login with wrong wrong password
        assertEquals(true, succesReg1);
        assertEquals(true, succesReg2);
    }

    @Test 
    public void checkInvalidUserName1() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidUserNameException.class, () -> userController.register(null, invalidUsernameBlank, validPassword1));
    }


    @Test 
    public void checkInvalidUserName2() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidUserNameException.class, () -> userController.register(null, invalidUsernameHasOnlySpace, validPassword1));
    }

    @Test 
    public void checkInvalidUserName3() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidUserNameException.class, () -> userController.register(null, invalidUsernameHasSpace, validPassword1));
    }

    @Test 
    public void checkInvalidPassword1() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidPasswordException.class, () -> userController.register(null, validUsername1, invalidPasswordBlank));
    }

    @Test 
    public void checkInvalidPassword2() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidPasswordException.class, () -> userController.register(null, validUsername1, invalidPasswordHasOnlySpace));
    }

    @Test 
    public void checkInvalidPassword3() throws InvalidUserNameException, InvalidPasswordException, RegistrationFailedException {
        assertThrows(InvalidPasswordException.class, () -> userController.register(null, validUsername1, invalidPasswordNotEnoughChars));
    }
}
