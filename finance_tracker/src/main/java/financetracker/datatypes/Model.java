package financetracker.datatypes;

import java.io.Serializable;

public abstract class Model implements Serializable {
    private long id;

    protected Model(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
