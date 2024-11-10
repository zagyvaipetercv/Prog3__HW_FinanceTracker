package financetracker.controllers;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;


public abstract class ControllerTests {
    protected static final String TEST_SAVE_FILE_PATH = "test_data\\saves\\test.dat";
    
    @BeforeEach
    public void deleteSaveFile() {
        File testSaveFile = new File(TEST_SAVE_FILE_PATH);
        if (testSaveFile.exists()) {
            testSaveFile.delete();
        }
    }
}
