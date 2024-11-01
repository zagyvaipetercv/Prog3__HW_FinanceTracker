package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.exceptions.CannotCreateControllerException;
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

        userController = new UserController();
    }

    @Test
    public void checkRegisterSucceds() {
        boolean succes = userController.register(null, validUsername1, validPassword1);
        assertEquals(true, succes);
    }

    @Test
    public void checkFindUser() throws ClassNotFoundException, IOException {
        boolean succes = userController.register(null, validUsername1, validPassword1);
        User user = userController.findUser(validUsername1);
        assertEquals(true, succes);
        assertEquals(validUsername1, user.getName());
        assertEquals(validPassword1, user.getPassword());
        assertEquals(1, user.getId());
    }

    @Test
    public void checkLoginUser() {
        boolean succesRegister = userController.register(null, validUsername1, validPassword1);
        boolean succesLogin = userController.login(null, validUsername1, validPassword1);
        
        assertEquals(true, succesRegister);
        assertEquals(true, succesLogin);
    }

    @Test
    public void checkWrongLogin() {
        boolean succesReg2 = userController.register(null, validUsername2, validPassword2);
        boolean succesReg1 = userController.register(null, validUsername1, validPassword1);

        boolean succesLogin = userController.login(null, validUsername1, validPassword2); // Login with wrong wrong password

        assertEquals(true, succesReg1);
        assertEquals(true, succesReg2);
        assertEquals(false, succesLogin);
    }

    @Test 
    public void checkInvalidUserName1() {
        boolean succes = userController.register(null, invalidUsernameBlank, validPassword1);
        assertEquals(false, succes);
    }


    @Test 
    public void checkInvalidUserName2() {
        boolean succes = userController.register(null, invalidUsernameHasOnlySpace, validPassword1);
        assertEquals(false, succes);
    }

    @Test 
    public void checkInvalidUserName3() {
        boolean succes = userController.register(null, invalidUsernameHasSpace, validPassword1);
        assertEquals(false, succes);
    }

    @Test 
    public void checkInvalidPassword1() {
        boolean succes = userController.register(null, validUsername1, invalidPasswordBlank);
        assertEquals(false, succes);
    }

    @Test 
    public void checkInvalidPassword2() {
        boolean succes = userController.register(null, validUsername1, invalidPasswordHasOnlySpace);
        assertEquals(false, succes);
    }

    @Test 
    public void checkInvalidPassword3() {
        boolean succes = userController.register(null, validUsername1, invalidPasswordNotEnoughChars);
        assertEquals(false, succes);
    }
}
