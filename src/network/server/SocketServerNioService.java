package network.server;

import com.google.common.eventbus.Subscribe;
import network.protocol.event.BytesToSocket;
import network.server.event.BytesFromSocket;
import network.server.model.ChangeChannelRequest;
import util.GuavaEventBusManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SocketServerNioService implements Runnable {

    // TODO Handle variety of disconnect events

    private static SocketServerNioService instance;

    private boolean serverStatus;
    private String serverAddress;
    private int serverPort;
    private ByteBuffer readBuffer;
    private Selector selector;
    private BlockingQueue<ChangeChannelRequest> changeChannelRequests;
    private final Map<SocketChannel, List<ByteBuffer>> dataToSend;

    ////

    private SocketServerNioService() {
        changeChannelRequests = new ArrayBlockingQueue<>(1024);
        dataToSend = new HashMap<>();
    }

    public static SocketServerNioService getInstance() {
        if (instance == null) {
            instance = new SocketServerNioService();
        }
        return instance;
    }

    ////

    public void start(String serverAddress, int serverPort) {
        serverStatus = true;

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        readBuffer = ByteBuffer.allocate(8192);
        selector = initSelector();

        GuavaEventBusManager.getBus().register(this);

        Thread serverThread = new Thread(this);
        serverThread.start();
    }

    ////

    @Override
    public void run() {
        System.out.println("\nServer launched");
        while (serverStatus) {
            try {
                checkDataToSend();
                selector.select();
                readSelector(selector);
            } catch (IOException ioe) {
                System.out.println("\nError during server main routine");
            }
        }
    }

    ////

    private Selector initSelector() {
        try {
            Selector socketSelector = SelectorProvider.provider().openSelector();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverAddress, serverPort);
            serverSocketChannel.socket().bind(inetSocketAddress);
            serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
            return  socketSelector;
        } catch (IOException ioe) {
            System.out.println("\nError during server initialization");
            ioe.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    private void checkDataToSend() {

        for (ChangeChannelRequest changeChannelRequest : changeChannelRequests) {
            switch (changeChannelRequest.getType()) {
                case ChangeChannelRequest.SET_WRITABLE:
                    SelectionKey selectionKey = changeChannelRequest.getSocketChannel().keyFor(selector);
                    //noinspection MagicConstant
                    selectionKey.interestOps(changeChannelRequest.getOps());
            }
        }
        changeChannelRequests.clear();
    }

    private void readSelector(Selector selector) {
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

        while (selectedKeys.hasNext()) {
            SelectionKey selectedKey = selectedKeys.next();
            selectedKeys.remove();
            processKey(selectedKey);
        }
    }

    private void processKey(SelectionKey selectedKey) {
        if (!selectedKey.isValid()) {
            return;
        }

        if (selectedKey.isAcceptable()) {
            acceptConnection(selectedKey);
        } else if (selectedKey.isReadable()) {
            try {
                read(selectedKey);
            } catch (IOException ioe) {
                System.out.println("\nCould not read from socket");
            }
        } else if (selectedKey.isWritable()) {
            try {
                write(selectedKey);
            } catch (IOException ioe) {
                System.out.println("\nCould not write to socket");

                // TODO Handle disconnect
            }
        }
    }

    private void acceptConnection(SelectionKey selectedKey) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectedKey.channel();

        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);

            // TODO Delete test code
            System.out.println("\nConnection accepted");

        } catch (IOException ioe) {
            System.out.println("\nError during connection accept");
        }
    }

    private void read(SelectionKey selectedKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
        readBuffer.clear();

        int readingResult = 0;
        BytesFromSocket event = new BytesFromSocket();

        try {
            readingResult = socketChannel.read(readBuffer);
            event.setSelectionKey(selectedKey);
            event.setData(readBuffer.array());
            event.setSize(readingResult);
        } catch (IOException ioe) {
            selectedKey.channel().close();
            selectedKey.cancel();
            System.out.println("\nConnection closed due reading error");
            ioe.printStackTrace();
        }

        if (readingResult == -1) {
            selectedKey.channel().close();
            selectedKey.cancel();
            System.out.println("\nConnection closed on the client side");
            return;
        }

        GuavaEventBusManager.getBus().post(event);
    }

    private void write(SelectionKey selectedKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

        synchronized (dataToSend) {
            List<ByteBuffer> messages = dataToSend.get(socketChannel);

            if (messages == null) return;

            while (!messages.isEmpty()) {
                ByteBuffer byteBuffer = messages.get(0);
                socketChannel.write(byteBuffer);

                if (byteBuffer.remaining() > 0) {
                    break;
                }

                messages.remove(0);
            }

            if (messages.isEmpty()) {
                selectedKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void sendToClient(SelectionKey selectedKey, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

        ChangeChannelRequest changeChannelRequest = new ChangeChannelRequest();
        changeChannelRequest.setSocketChannel(socketChannel);
        changeChannelRequest.setType(ChangeChannelRequest.SET_WRITABLE);
        changeChannelRequest.setOps(SelectionKey.OP_WRITE);

        try {
            changeChannelRequests.put(changeChannelRequest);

            synchronized (dataToSend) {
                List<ByteBuffer> messages = dataToSend.get(socketChannel);

                if (messages == null) {
                    messages = new ArrayList<>();
                    dataToSend.put(socketChannel, messages);
                }

                messages.add(ByteBuffer.wrap(data));
            }

            selector.wakeup();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }


    }

    ////

    @Subscribe
    public void handleBytesToSocketEvent(BytesToSocket event) {
        sendToClient(event.getSelectionKey(), event.getData());
    }
}
































