package financetracker.datatypes;

public class User extends Model {
    private static final long serialVersionUID = 1_001L;

    private String name;
    private String password;

    public User(long id, String name, String password) {
        super(id);
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return getId() + " " + name + " " + password;
    }

    @Override 
    public boolean equals(Object o) {
        if (!o.getClass().equals(User.class)) {
            return false;    
        }

        return getId() == ((User)o).getId();
    }
}
