package network.server.model;

import java.nio.channels.SocketChannel;

public class ChangeChannelRequest {

    public static final int SET_WRITABLE = 1;

    private SocketChannel socketChannel;
    private int type;
    private int ops;

    ////


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOps() {
        return ops;
    }

    public void setOps(int ops) {
        this.ops = ops;
    }
}
