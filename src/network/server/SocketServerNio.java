package network.server;

import network.NetworkFactory;
import network.ioparserworker.IoParserWorker;
import network.server.adt.ChangeChannelRequest;
import network.server.adt.MessageFromBuffer;

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

public class SocketServerNio implements SocketServer, Runnable {

    // TODO Hndle variety of disconnect events

    private static SocketServerNio instance;

    // Module dependencies
    private IoParserWorker ioParserWorker;

    private boolean serverStatus;
    private String serverAddress;
    private int serverPort;
    private ByteBuffer readBuffer;
    private Selector selector;
    private BlockingQueue<ChangeChannelRequest> changeChannelRequests;
    private final Map<SocketChannel, List<ByteBuffer>> dataToSend;

    ////

    private SocketServerNio() {
        changeChannelRequests = new ArrayBlockingQueue<>(1024);
        dataToSend = new HashMap<>();
    }

    public static SocketServerNio getInstance() {
        if (instance == null) {
            instance = new SocketServerNio();
        }
        return instance;
    }

    ////

    @Override
    public void start(String serverAddress, int serverPort) {
        serverStatus = true;

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        readBuffer = ByteBuffer.allocate(8192);
        selector = initSelector();

        Thread serverThread = new Thread(this);
        serverThread.start();

        ioParserWorker = NetworkFactory.provideIoParserWorker();
        ioParserWorker.start();
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

    @Override
    public void sendToClient(SelectionKey selectedKey, byte[] data) {
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
        } catch (IOException ioe) {
            System.out.println("\nError during connection accrept");
        }
    }

    private void read(SelectionKey selectedKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
        readBuffer.clear();

        int readingResult = 0;
        MessageFromBuffer messageFromBuffer = new MessageFromBuffer();

        try {
            readingResult = socketChannel.read(readBuffer);
            messageFromBuffer.setData(readBuffer.array());
            messageFromBuffer.setSize(readingResult);
            messageFromBuffer.setSelectedKey(selectedKey);
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


        ioParserWorker.putToParseQueue(messageFromBuffer);
    }

    private void write(SelectionKey selectedKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

        synchronized (dataToSend) {
            List<ByteBuffer> messages = dataToSend.get(socketChannel);

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
}
