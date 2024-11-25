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

    /**
     * Initializes the next id by searching through the saved records finding
     * the one with the maximum id value and then setting the nextID field
     * to that value + 1
     * 
     * @throws SerializerWasNotCreated if an IO or serialization error occured
     */
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

    /**
     * Creates a save file if it did not exist yet.
     * 
     * @throws SerializerWasNotCreated if an IO error Occured
     */
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
    public String getFilePath() {
        return filePath;
    }

    public long getNextId() {
        return nextID;
    }

    // IO OPEARTIONS
    /**
     * Adds a new record to the save file.
     * <p>
     * nextID will be automatically increased in the process.
     * 
     * @param t the record that will be added to the save file
     * @throws SerializerCannotRead  if the function can't open the old records
     * @throws SerializerCannotWrite if the function can't write the modified record
     *                               set
     */
    public synchronized void appendNewData(T t) throws SerializerCannotRead, SerializerCannotWrite {
        List<T> datasSaved = readAll();
        datasSaved.add(t);
        write(datasSaved);
        nextID++;
    }

    /**
     * Returns the existing records in a List
     * 
     * @return a List of the existing records
     * @throws SerializerCannotRead if an IO Error occured or the records could not
     *                              be serialized
     */
    public synchronized List<T> readAll() throws SerializerCannotRead {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializerCannotRead(filePath, "Serializer couldn't read save file");
        }
    }

    /**
     * Changes a data record in the save files
     * <p>
     * Old and new records will be compared by their ids
     * 
     * @param t the data record with the new parameters (Id must match with the old
     *          records id)
     * @throws SerializerCannotRead  if the function can't open the old records
     * @throws SerializerCannotWrite if the function can't write the modified record
     *                               set
     */
    public synchronized void changeData(T t) throws SerializerCannotRead, SerializerCannotWrite {
        List<T> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == t.getId()) {
                all.set(i, t);
            }
        }
        write(all);
    }

    /**
     * Removes a record with a specified id from the save file
     * 
     * @param id the id of the record that will be removed
     * @throws SerializerCannotRead  if the function can't open the old records
     * @throws SerializerCannotWrite if the function can't write the modified record
     *                               set
     */
    public synchronized void removeData(long id) throws SerializerCannotRead, SerializerCannotWrite {
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

    /**
     * Writes out a list of data to the save file
     * 
     * @param datas List of data that will be written
     * @throws SerializerCannotWrite if writing the data failed
     */
    private void write(List<T> datas) throws SerializerCannotWrite {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(datas);
        } catch (IOException e) {
            throw new SerializerCannotWrite(filePath, "Serializer could'n write save file");
        }
    }

    // QUERIES
    /**
     * Finds and returns the id of the first record using a comparator
     * 
     * @param data       the data the records will be compared with
     * @param comparator the comparator method
     * @return the id of the first record where the comparator found a match
     * @throws SerializerCannotRead if the function can't open the old records
     * @throws IdNotFoundException  if the id was not found by the comparator
     */
    public long findId(T data, Comparator<T> comparator) throws SerializerCannotRead, IdNotFoundException {
        List<T> savedData = readAll();
        for (T t : savedData) {
            if (comparator.compare(t, data) == 0) {
                return t.getId();
            }
        }

        throw new IdNotFoundException(data, data.toString() + " model was not found in save file");
    }

    /**
     * Returns the number records are in the save file.
     * 
     * @return number of records in the save file.
     * @throws SerializerCannotRead if the records could not be read
     */
    public int recordsSize() throws SerializerCannotRead {
        return readAll().size();
    }
}
