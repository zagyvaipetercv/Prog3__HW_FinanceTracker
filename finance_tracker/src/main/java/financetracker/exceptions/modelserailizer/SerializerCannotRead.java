package financetracker.exceptions.modelserailizer;

public class SerializerCannotRead extends Exception {

    private final transient String filePath;
    
    public SerializerCannotRead(String filePath, String message) {
        super(message);

        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
