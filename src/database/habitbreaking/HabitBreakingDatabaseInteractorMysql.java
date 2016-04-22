package database.habitbreaking;

import database.habitbreaking.adt.Query;
import database.habitbreaking.adt.UserData;
import util.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class HabitBreakingDatabaseInteractorMysql implements HabitBreakingDatabaseInteractor, Runnable {

    // User table constants
    private static final String USER_TABLE_NAME = "user_table";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD = "password";

    private static HabitBreakingDatabaseInteractorMysql instance;

    private boolean interactorStatus;

    private String databaseAddress;
    private String databaseLogin;
    private String databasePassword;

    private BlockingQueue<Query> databaseQueries;

    ////

    private HabitBreakingDatabaseInteractorMysql() {
        String jdbcDiverAddress = ConfigLoader.jdbsAddress;
        databaseAddress = ConfigLoader.databaseAddress;
        databaseLogin = ConfigLoader.databaseLogin;
        databasePassword = ConfigLoader.databasePassword;

        try {
            Class.forName(jdbcDiverAddress);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Could not find JDBC driver");
            cnfe.printStackTrace();
        }

        databaseQueries = new ArrayBlockingQueue<>(1024);
        interactorStatus = true;
        new Thread(this).start();
    }

    public static HabitBreakingDatabaseInteractorMysql getInstance() {
        if (instance == null) {
            instance = new HabitBreakingDatabaseInteractorMysql();
        }
        return instance;
    }

    ////

    @Override
    public void writeNewUser(UserData userData) {
        Query query = new Query();
        String queryBody = String.format("INSERT INTO %s (%s, %s) VALUES ('%s', '%s');",
                USER_TABLE_NAME,
                COLUMN_LOGIN,
                COLUMN_PASSWORD,
                userData.getLogin(),
                userData.getPassword());

        // TODO Delete test code
        System.out.println(queryBody);

        query.setType(Query.WRITE_NEW_USER);
        query.setQuery(queryBody);

        try {
            databaseQueries.put(query);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    ////

    @Override
    public void run() {
        while (interactorStatus) {
            try {
                Query query = databaseQueries.take();
                execute(query);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    ////

    private void execute(Query query) {
        switch (query.getType()) {
            case Query.WRITE_NEW_USER:
                executeWriteNewUserQuery(query.getQuery());
                break;
        }
    }

    private void executeWriteNewUserQuery(String query) {
        Connection databaseConnection = null;
        Statement statement = null;

        try {
            System.out.println(databaseLogin + "  " + databasePassword);
            databaseConnection = DriverManager.getConnection(databaseAddress, databaseLogin, databasePassword);
            statement = databaseConnection.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            System.out.println("Error during database operation");
            e.printStackTrace();
        } finally {
            try {
                if (databaseConnection != null) databaseConnection.close();
                if (statement != null) statement.close();
            } catch (SQLException sqle) {
                System.out.println("Error during closing database objects");
                sqle.printStackTrace();
            }
        }
    }
}
