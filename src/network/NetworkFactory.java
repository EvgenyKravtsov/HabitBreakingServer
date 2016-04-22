package network;

import network.ioparserworker.IoParserWorker;
import network.ioparserworker.JsonParserSingleThread;
import network.server.SocketServer;
import network.server.SocketServerNio;

public class NetworkFactory {

    public static SocketServer provideSocketServer() {
        return SocketServerNio.getInstance();
    }

    public static IoParserWorker provideIoParserWorker() {
        return new JsonParserSingleThread();
    }
}
