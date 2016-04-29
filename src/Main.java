import network.parser.JsonParserService;
import network.protocol.JsonProtocol;
import network.server.SocketServerNioService;
import storage.MysqlDatabaseService;
import util.ConfigLoader;
import util.GuavaEventBusManager;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        System.out.println("\n\n <<<< HABIT BREAKING SERVER >>>> \n\n");

        ConfigLoader configLoader;

        try {
            configLoader = new ConfigLoader();
            configLoader.loadConfig();
        } catch (IOException ioe) {
            System.out.println("\nConfiguration file not found");
            System.exit(0);
        }

        String serverAddress = ConfigLoader.serverAddress;
        int serverPort = ConfigLoader.serverPort;

        GuavaEventBusManager.initBus();
        startServerService(serverAddress, serverPort);
        startParserService();
        initProtocol();
        startStorageService();
    }

    ////

    private static void startServerService(String serverAddress, int serverPort) {
        SocketServerNioService.getInstance().start(serverAddress, serverPort);
    }

    private static void startParserService() {
        JsonParserService.getInstance().start();
    }

    private static void initProtocol() {
        JsonProtocol.init();
    }

    private static void startStorageService() {
        MysqlDatabaseService.getInstance().start();
    }
}
