package financetracker.exceptions.modelserailizer;

public class SerializerCannotWrite extends Exception {
    private final transient String filePath;

    public SerializerCannotWrite(String message, String filePath) {
        super(message);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
