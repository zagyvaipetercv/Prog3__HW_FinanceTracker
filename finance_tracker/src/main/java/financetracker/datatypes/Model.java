package financetracker.datatypes;

import java.io.Serializable;

/**
 * Base class for every serailizable datatype
 * The controller works with classes which derive from this class
 */
public abstract class Model implements Serializable {
    private long id;

    protected Model(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
