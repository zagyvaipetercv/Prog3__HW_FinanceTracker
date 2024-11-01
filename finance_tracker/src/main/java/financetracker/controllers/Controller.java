package financetracker.controllers;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import financetracker.exceptions.CannotCreateControllerException;
import financetracker.exceptions.IdNotFoundException;
import financetracker.models.Model;

public abstract class Controller<T extends Model> {
    private String filePath; 
    private long nextID;

    protected Controller(String filePath) throws CannotCreateControllerException {
        this.filePath = filePath;

        try {
            createSaveFile();
            initNextId();
        } catch (IOException | ClassNotFoundException e) {
            throw new CannotCreateControllerException(this.getClass(), "IO Exception occured");
        }
    }
    
    // TODO: implement this function
    protected void appendNewData(T t) throws IOException {
        
    }

    // TODO: implement this function
    protected void removeData(long id) {

    }

    protected long findId(T data) throws IOException, ClassNotFoundException, IdNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            while (true) {
                T t = (T) ois.readObject();
                if (t == null) {
                    break;
                }

                if (t.equals(data)) {
                    return t.getId();
                }
            }
        } catch (EOFException e) {
            // Reached end of File no need to throw IO exception -> throw IdNotFoundException instead
        }

        throw new IdNotFoundException(data);
    }

    // TODO: implement this function
    protected int recordsSize() {
        throw new RuntimeException("Not impleneted yet");
    }

    protected void createSaveFile() throws IOException, CannotCreateControllerException {
        File saveFile = new File(filePath);
        
        if (!saveFile.exists()) {
            File dirPath = saveFile.getParentFile();
            dirPath.mkdirs();
            boolean fileSucces = saveFile.createNewFile();
            if (!fileSucces) {
                throw new CannotCreateControllerException(this.getClass(), "Save file exists when it shouldn't");
            }
        }
    }

    private void initNextId() throws IOException, ClassNotFoundException {
        nextID = 1;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilePath()))) {
            while (true) {
                ois.readObject();
                increaseNextId();
            }
        } catch (EOFException e) {
            // ois reached end of file -> close()
            // [ois implements Closable -> no need to close manually]
        }
    }

    protected void setSaveFilePath(String filePath) {
        this.filePath = filePath;
    }

    protected String getFilePath() {
        return filePath;
    }

    protected long getNextId() {
        return nextID;
    }

    protected void increaseNextId() {
        nextID++;
    }
} 
