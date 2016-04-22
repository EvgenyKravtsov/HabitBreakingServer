package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_KEY_SERVER_ADDRESS = "SERVER_ADDRESS";
    private static final String CONFIG_KEY_SERVER_PROT = "SERVER_PORT";
    private static final String CONFIG_KEY_JDBC_DRIVER_ADDRESS = "JDBC_DRIVER_ADDRESS";
    private static final String CONFIG_KEY_MYSQL_DATABASE_ADDRESS = "MYSQL_DATABASE_ADDRESS";
    private static final String CONFIG_KEY_MYSQL_DATABASE_LOGIN = "MYSQL_DATABASE_LOGIN";
    private static final String CONFIG_KEY_MYSQL_DATABASE_PASSWORD = "MYSQL_DATABASE_PASSWORD";

    public static String serverAddress;
    public static int serverPort;
    public static String jdbsAddress;
    public static String databaseAddress;
    public static String databaseLogin;
    public static String databasePassword;

    private Properties config;

    ////

    public ConfigLoader() throws IOException {
        config = new Properties();
        config.load(new FileInputStream(new File("./config.ini")));
    }

    ////

    public void loadConfig() {
        serverAddress = readServerAddress();
        serverPort = readServerPort();
        jdbsAddress = readJdbcDriverAddress();
        databaseAddress = readMysqlDatabaseAddress();
        databaseLogin = readMysqlDatabaseLogin();
        databasePassword = readMysqlDatabasePassword();
    }

    ////

    private String readServerAddress() {
        try {
            return config.getProperty(CONFIG_KEY_SERVER_ADDRESS);
        } catch (NumberFormatException nfe) {
            System.out.println("No value for SERVER_ADDRESS - Server shut down");
            System.exit(0);
            return null;
        }
    }

    private int readServerPort() {
        try {
            return Integer.valueOf(config.getProperty(CONFIG_KEY_SERVER_PROT));
        } catch (NumberFormatException nfe) {
            System.out.println("No value for SERVER_PORT - Server shut down");
            System.exit(0);
            return 0;
        }
    }

    private String readJdbcDriverAddress() {
        try {
            return config.getProperty(CONFIG_KEY_JDBC_DRIVER_ADDRESS);
        } catch (NumberFormatException nfe) {
            System.out.println("No value for JDBC_DRIVER_ADDRESS - Server shut down");
            System.exit(0);
            return null;
        }
    }

    private String readMysqlDatabaseAddress() {
        try {
            return config.getProperty(CONFIG_KEY_MYSQL_DATABASE_ADDRESS);
        } catch (NumberFormatException nfe) {
            System.out.println("No value for MYSQL_DATABASE_ADDRESS - Server shut down");
            System.exit(0);
            return null;
        }
    }

    private String readMysqlDatabaseLogin() {
        try {
            return config.getProperty(CONFIG_KEY_MYSQL_DATABASE_LOGIN);
        } catch (NumberFormatException nfe) {
            System.out.println("No value for MYSQL_DATABASE_LOGIN - Server shut down");
            System.exit(0);
            return null;
        }
    }

    private String readMysqlDatabasePassword() {
        try {
            return config.getProperty(CONFIG_KEY_MYSQL_DATABASE_PASSWORD);
        } catch (NumberFormatException nfe) {
            System.out.println("No value for MYSQL_DATABASE_PASSWORD - Server shut down");
            System.exit(0);
            return null;
        }
    }
}
