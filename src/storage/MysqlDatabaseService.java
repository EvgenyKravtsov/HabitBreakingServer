package storage;

import com.google.common.eventbus.Subscribe;
import network.protocol.event.QueryToStorage;
import org.apache.commons.dbcp2.BasicDataSource;
import storage.event.MessageFromStorage;
import storage.model.MessageFromStorageType;
import util.ConfigLoader;
import util.GuavaEventBusManager;

import javax.sql.DataSource;
import java.nio.channels.SelectionKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MysqlDatabaseService implements Runnable {

    // Mysql error codes
    private static final int MYSQL_DUPLICATE_ERROR_CODE = 1062;

    // User table constants
    private static final String USER_TABLE_NAME = "user_table";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_REGISTRATION_DATE = "registration_date";

    // Statistic table constants
    private static final int STATISTIC_OUTPUT_LIMIT = 30;
    private static final String STATISTIC_TABLE_NAME = "statistic_table";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_COUNT = "count";

    private static MysqlDatabaseService instance;

    private boolean serviceStatus;
    private DataSource databaseConnectionPool;

    private BlockingQueue<QueryToStorage> databaseQueries;

    ////

    private MysqlDatabaseService() {
        databaseConnectionPool = setupDataSource();
        databaseQueries = new ArrayBlockingQueue<>(1024);
    }

    public static MysqlDatabaseService getInstance() {
        if (instance == null) {
            instance = new MysqlDatabaseService();
        }
        return instance;
    }

    ////

    public void start() {
        serviceStatus = true;
        GuavaEventBusManager.getBus().register(this);
        new Thread(this).start();
    }

    ////

    @Override
    public void run() {
        while (serviceStatus) {
            try {
                QueryToStorage queryToStorage = databaseQueries.take();
                execute(queryToStorage);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    ////

    private void execute(QueryToStorage queryToStorage) {
        switch (queryToStorage.getQueryType()) {
            case WRITE_NEW_USER:
                executeWriteNewUserQuery(queryToStorage);
                break;
            case WRITE_STATISTIC:
                executeWriteStatisticQuery(queryToStorage);
                break;
            case GET_STATISTIC:
                executeGetStatisticQuery(queryToStorage);
                break;
        }
    }

    private void executeWriteNewUserQuery(QueryToStorage queryToStorage) {
        String queryString = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                USER_TABLE_NAME,
                COLUMN_EMAIL,
                COLUMN_GENDER,
                COLUMN_AGE,
                COLUMN_REGISTRATION_DATE);

        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;

        Map<String, Object> dataMap = queryToStorage.getDataMap();
        String email = (String) dataMap.get(QueryToStorage.DATAKEY_EMAIL);
        int gender = (int) dataMap.get(QueryToStorage.DATAKEY_GENDER);
        int age = (int) dataMap.get(QueryToStorage.DATAKEY_AGE);
        long registrationDate = (long) dataMap.get(QueryToStorage.DATAKEY_REGISTRATION_DATE);

        try {
            databaseConnection = databaseConnectionPool.getConnection();
            preparedStatement = databaseConnection.prepareStatement(queryString);
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, gender);
            preparedStatement.setInt(3, age);
            preparedStatement.setLong(4, registrationDate);
            int result = preparedStatement.executeUpdate();

            if (result == 1) {
                emitMessageFromStorage(queryToStorage.getSelectionKey(), MessageFromStorageType.WRITE_SUCCESS);
            }

        } catch (SQLException sqle) {
            if (sqle.getErrorCode() == MYSQL_DUPLICATE_ERROR_CODE) {
                emitMessageFromStorage(queryToStorage.getSelectionKey(), MessageFromStorageType.DUPLICATE_ERROR);
            } else {
                sqle.printStackTrace();
            }
        } finally {
            try { if (preparedStatement != null) preparedStatement.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (databaseConnection != null) databaseConnection.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void executeWriteStatisticQuery(QueryToStorage queryToStorage) {
        String queryString = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                STATISTIC_TABLE_NAME,
                COLUMN_EMAIL,
                COLUMN_DATE,
                COLUMN_COUNT);

        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;

        Map<String, Object> dataMap = queryToStorage.getDataMap();
        String email = (String) dataMap.get(QueryToStorage.DATAKEY_EMAIL);
        long date = (long) dataMap.get(QueryToStorage.DATAKEY_DATE);
        int count = (int) dataMap.get(QueryToStorage.DATAKEY_COUNT);

        try {
            databaseConnection = databaseConnectionPool.getConnection();
            preparedStatement = databaseConnection.prepareStatement(queryString);
            preparedStatement.setString(1, email);
            preparedStatement.setLong(2, date);
            preparedStatement.setInt(3, count);
            int result = preparedStatement.executeUpdate();

            if (result == 1) {
                emitMessageFromStorage(queryToStorage.getSelectionKey(), MessageFromStorageType.WRITE_SUCCESS);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try { if (preparedStatement != null) preparedStatement.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (databaseConnection != null) databaseConnection.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void executeGetStatisticQuery(QueryToStorage queryToStorage) {
        String queryString = String.format("SELECT %s, %s FROM %s WHERE %s = ? ORDER BY %s ASC LIMIT %s",
                COLUMN_DATE, COLUMN_COUNT,
                STATISTIC_TABLE_NAME,
                COLUMN_EMAIL,
                COLUMN_DATE,
                STATISTIC_OUTPUT_LIMIT);

        Connection databaseConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, Object> dataMap = queryToStorage.getDataMap();
        String email = (String) dataMap.get(QueryToStorage.DATAKEY_EMAIL);

        try {
            databaseConnection = databaseConnectionPool.getConnection();
            preparedStatement = databaseConnection.prepareStatement(queryString);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            List<Map<String, Object>> resultList = new ArrayList<>();

            while (resultSet.next()) {
                Map<String, Object> resultItem = new HashMap<>();
                resultItem.put(QueryToStorage.DATAKEY_DATE, resultSet.getLong(COLUMN_DATE));
                resultItem.put(QueryToStorage.DATAKEY_COUNT, resultSet.getInt(COLUMN_COUNT));
                resultList.add(resultItem);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(QueryToStorage.DATAKEY_STATISTIC_DATA, resultList);
            emitMessageFromStorage(queryToStorage.getSelectionKey(), MessageFromStorageType.STATISTIC_DATA, resultMap);

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try { if (resultSet != null) resultSet.close(); } catch (Exception e) { e.printStackTrace(); };
            try { if (preparedStatement != null) preparedStatement.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (databaseConnection != null) databaseConnection.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void emitMessageFromStorage(SelectionKey selectionKey, MessageFromStorageType type) {
        MessageFromStorage messageFromStorage = new MessageFromStorage();
        messageFromStorage.setSelectionKey(selectionKey);
        messageFromStorage.setType(type);
        GuavaEventBusManager.getBus().post(messageFromStorage);
    }

    private void emitMessageFromStorage(SelectionKey selectionKey, MessageFromStorageType type, Map<String, Object> data) {
        MessageFromStorage messageFromStorage = new MessageFromStorage();
        messageFromStorage.setSelectionKey(selectionKey);
        messageFromStorage.setType(type);
        messageFromStorage.setData(data);
        GuavaEventBusManager.getBus().post(messageFromStorage);
    }

    private DataSource setupDataSource() {
        String jdbcDriverAddress = ConfigLoader.jdbsAddress;
        String databaseAddress = ConfigLoader.databaseAddress;
        String databaseLogin = ConfigLoader.databaseLogin;
        String databasePassword = ConfigLoader.databasePassword;

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(jdbcDriverAddress);
        basicDataSource.setUrl(databaseAddress);
        basicDataSource.setUsername(databaseLogin);
        basicDataSource.setPassword(databasePassword);

        return basicDataSource;
    }

    ////

    @Subscribe
    public void handleQueryToStorageEvent(QueryToStorage event) {
        try {
            databaseQueries.put(event);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}







































