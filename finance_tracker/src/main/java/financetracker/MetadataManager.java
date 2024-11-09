package financetracker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import financetracker.controllers.CashFlowController;
import financetracker.controllers.Controller;
import financetracker.controllers.UserController;
import financetracker.models.Model;

// TODO: Read write metadata
/*  DATAS TO SAVE
 * 
 *      NextID of each controller
 *      SaveFilePath of each Controller
 */
public class MetadataManager {
    private static final String METADATA_SAVE_FILE_PATH = "saves\\meta.xml";

    private static final String DEFAULT_CASHFLOW = "saves\\cashflow.dat";
    private static final String DEFAULT_USERS = "saves\\users.dat";

    private static String metadataFilePath;

    private static boolean isTesting = false;

    private static Map<Class<? extends Controller<? extends Model>>, String> controllerFilePathMap;
    private static Map<Class<? extends Controller<? extends Model>>, Long> controllerNextIdMap;

    private MetadataManager() {
    }

    public static void init() {
        isTesting = false;
        metadataFilePath = METADATA_SAVE_FILE_PATH;
        File metaFile = new File(metadataFilePath);
        if (!metaFile.exists()) {
            createMetaFile();
        }

        initPaths();
        initNextIDs();
    }

    public static void setupForTest(String filePath) {
        isTesting = true;
        metadataFilePath = filePath;
        File metaFile = new File(metadataFilePath);
        if (!metaFile.exists()) {
            createMetaFile();
        }

        initPaths();
        initNextIDs();
    }

    // Change cached and write changes
    public static void setNextId(Class<? extends Controller<? extends Model>> controllerClass) {

    }

    // Change cached and write changes
    public static void setFilePath(Class<? extends Controller<? extends Model>> controllerClass) {

    }

    public static long getNextId(Class<? extends Controller> controllerClass) {
        if (isTesting) {
            return 1;
        }

        return controllerNextIdMap.get(controllerClass);
    }

    public static String getFilePath(Class<? extends Controller<? extends Model>> controllerClass) {
        return controllerFilePathMap.get(controllerClass);
    }

    private static void createMetaFile() {

    }

    private static void initPaths() {
        controllerFilePathMap = new HashMap<>();
        controllerFilePathMap.put(UserController.class, DEFAULT_USERS);
        controllerFilePathMap.put(CashFlowController.class, DEFAULT_CASHFLOW);
    }

    private static void initNextIDs() {
        controllerNextIdMap = new HashMap<>();
    }
}
