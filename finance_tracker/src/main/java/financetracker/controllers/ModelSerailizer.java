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

import financetracker.datatypes.Model;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.modelserailizer.SerializerWasNotCreated;

public class ModelSerailizer<T extends Model> {

    private String filePath;
    private long nextID;

    // INITIALZATION
    public ModelSerailizer(String filePath) throws SerializerWasNotCreated {
        this.filePath = filePath;

        createSaveFile();
        initNextId();
    }

    private synchronized void initNextId() throws SerializerWasNotCreated {
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
            throw new SerializerWasNotCreated("Next Id couldn't be initialized");
        }
    }

    private synchronized void createSaveFile() throws SerializerWasNotCreated {
        File saveFile = new File(filePath);

        try {
            if (!saveFile.exists()) {
                File dirPath = saveFile.getParentFile();
                dirPath.mkdirs();
                boolean fileSucces = saveFile.createNewFile();
                if (!fileSucces) {
                    throw new SerializerWasNotCreated("Save file was not created");
                }

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
                oos.writeObject(new ArrayList<T>());
                oos.close();
            }
        } catch (IOException e) {
            throw new SerializerWasNotCreated("Save file was not created");
        }

    }

    // METADATA
    public synchronized void setSaveFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getNextId() {
        return nextID;
    }

    // IO OPEARTIONS
    public synchronized void appendNewData(T t) throws SerializerCannotRead, SerializerCannotWrite {
        List<T> datasSaved = readAll();
        datasSaved.add(t);
        write(datasSaved);
        nextID++;
    }

    public synchronized void appendNewDatas(List<T> tList) throws SerializerCannotRead, SerializerCannotWrite {
        List<T> dataSaved = readAll();
        dataSaved.addAll(tList);
        write(dataSaved);
    }

    public synchronized List<T> readAll() throws SerializerCannotRead {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializerCannotRead(filePath, "Serializer couldn't read save file");
        }
    }

    public synchronized void changeData(T t) throws SerializerCannotRead, SerializerCannotWrite {
        List<T> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == t.getId()) {
                all.set(i, t);
            }
        }
        write(all);
    }

    private void write(List<T> datas) throws SerializerCannotWrite {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(datas);
        } catch (IOException e) {
            throw new SerializerCannotWrite(filePath, "Serializer could'n write save file");
        }
    }

    public void removeData(long id) throws SerializerCannotRead, SerializerCannotWrite {
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
    public long findId(T data, Comparator<T> comparator) throws SerializerCannotRead, IdNotFoundException {
        List<T> savedData = readAll();
        for (T t : savedData) {
            if (comparator.compare(t, data) == 0) {
                return t.getId();
            }
        }

        throw new IdNotFoundException(data, data.toString() + " model was not found in save file");
    }

    public int recordsSize() throws SerializerCannotRead {
        return readAll().size();
    }
}
