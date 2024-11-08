package financetracker.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.WindowConstants;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.exceptions.controller.IdNotFoundException;
import financetracker.models.Model;
import financetracker.views.base.FrameView;
import financetracker.windowing.MainFrame;

public abstract class Controller<T extends Model> {
    private String filePath;
    private long nextID;

    protected MainFrame mainFrame;

    // INITIALZATION
    protected Controller(String filePath, MainFrame mainFrame) throws CannotCreateControllerException {
        this.filePath = filePath;
        this.mainFrame = mainFrame;

        createSaveFile();
        initNextId();
    }

    private void initNextId() throws CannotCreateControllerException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilePath()))) {
            List<T> savedData = (List<T>) ois.readObject();

            if (savedData.isEmpty()) {
                nextID = 1;
                return;
            }

            nextID = Collections.max(
                    savedData,
                    (model1, model2) -> ((Long) model1.getId()).compareTo(model2.getId()))
                    .getId() + 1;

        } catch (IOException | ClassNotFoundException e) {
            throw new CannotCreateControllerException(this.getClass(), "nextId was not initialized");
        }
    }

    protected void createSaveFile() throws CannotCreateControllerException {
        File saveFile = new File(filePath);

        try {
            if (!saveFile.exists()) {
                File dirPath = saveFile.getParentFile();
                dirPath.mkdirs();
                boolean fileSucces = saveFile.createNewFile();
                if (!fileSucces) {
                    throw new CannotCreateControllerException(this.getClass(), "Save file exists when it shouldn't");
                }

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
                oos.writeObject(new ArrayList<T>());
                oos.close();
            }
        } catch (IOException e) {
            throw new CannotCreateControllerException(this.getClass(), filePath);
        }

    }

    // METADATA
    protected void setSaveFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    protected long getNextId() {
        return nextID;
    }

    protected void increaseNextId() {
        nextID++;
    }

    // IO OPEARTIONS
    protected void appendNewData(T t) throws ControllerCannotWriteException, ControllerCannotReadException {
        List<T> datasSaved = readAll();
        datasSaved.add(t);
        write(datasSaved);
        increaseNextId();
    }

    protected void appendNewDatas(List<T> tList) throws ControllerCannotReadException, ControllerCannotWriteException {
        List<T> dataSaved = readAll();
        dataSaved.addAll(tList);
        write(dataSaved);
    }

    protected List<T> readAll() throws ControllerCannotReadException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ControllerCannotReadException(this);
        }

    }

    protected void write(List<T> datas) throws ControllerCannotWriteException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(datas);
        } catch (IOException e) {
            throw new ControllerCannotWriteException(this);
        }
    }

    protected void removeData(long id) throws ControllerCannotWriteException, ControllerCannotReadException {
        List<T> datasSaved = readAll();

        Iterator<T> iter = datasSaved.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId() == id) {
                iter.remove();
                break;
            }
        }

        write(datasSaved);
    }

    // QUERIES
    protected long findId(T data, Comparator<T> comparator) throws ControllerCannotReadException, IdNotFoundException {
        List<T> savedData = readAll();
        for (T t : savedData) {
            if (comparator.compare(t, data) == 0) {
                return t.getId();
            }
        }

        throw new IdNotFoundException(data);
    }

    protected int recordsSize() throws ControllerCannotReadException {
        return readAll().size();
    }

    public void closeFrameView(FrameView fv) {
        fv.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        fv.dispose();
    }
}
