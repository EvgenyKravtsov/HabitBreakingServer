package database.habitbreaking.adt;

public class Query {

    public static final String WRITE_NEW_USER = "write_new_user";

    private String type;
    private String query;

    ////


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
