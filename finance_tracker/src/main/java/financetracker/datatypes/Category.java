package financetracker.datatypes;

public class Category extends Model {
    private String name;

    public Category(long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    @Override
    public String toString() {
        return name;
    }
}
