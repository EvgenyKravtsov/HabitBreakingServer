package network.server;

import java.nio.channels.SelectionKey;

public interface SocketServer {

    void start(String serverAddress, int serverPort);

    void sendToClient(SelectionKey selectedKey, byte[] data);
}
