import network.NetworkFactory;
import util.ConfigLoader;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        System.out.println("\n\n <<<< HABIT BREAKING SERVER >>>> \n\n");

        ConfigLoader configLoader = null;

        try {
            configLoader = new ConfigLoader();
            configLoader.loadConfig();
        } catch (IOException ioe) {
            System.out.println("\nConfiguration file not found");
            System.exit(0);
        }

        String serverAddress = ConfigLoader.serverAddress;
        int serverPort = ConfigLoader.serverPort;
        startServer(serverAddress, serverPort);
    }

    ////

    private static void startServer(String serverAddress, int serverPort) {
        NetworkFactory.provideSocketServer().start(serverAddress, serverPort);
    }
}
