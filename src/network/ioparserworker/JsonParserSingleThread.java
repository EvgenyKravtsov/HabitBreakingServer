package network.ioparserworker;

import network.NetworkFactory;
import network.protocols.HabitBreakingJsonProtocol;
import network.server.adt.MessageFromBuffer;
import network.server.SocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JsonParserSingleThread implements IoParserWorker, Runnable {

    // Module dependencies
    private SocketServer socketServer;

    private boolean parserStatus;
    private BlockingQueue<MessageFromBuffer> incomingMessagesQueue;

    ////

    public JsonParserSingleThread() {
        socketServer = NetworkFactory.provideSocketServer();
        incomingMessagesQueue = new ArrayBlockingQueue<>(1024);
    }

    ////

    @Override
    public void start() {
        parserStatus = true;
        Thread parserThread = new Thread(this);
        parserThread.start();
    }

    @Override
    public void putToParseQueue(MessageFromBuffer messageFromBuffer) {
        try {
            incomingMessagesQueue.put(messageFromBuffer);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    ////

    @Override
    public void run() {
        while (parserStatus) {
            try {
                JSONObject jsonMessage = parseIncomingMessageToJsonAndConfirm(incomingMessagesQueue.take());
                new HabitBreakingJsonProtocol().handleMessage(jsonMessage);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    ////

    private JSONObject parseIncomingMessageToJsonAndConfirm(MessageFromBuffer messageFromBuffer) {
        int size = messageFromBuffer.getSize();
        byte[] data = messageFromBuffer.getData();

        byte[] dataCopy = new byte[size];
        System.arraycopy(data, 0, dataCopy, 0, size);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonMessage = null;

        try {
            jsonMessage = (JSONObject) jsonParser.parse(new String(dataCopy));
            dataReceiveConfirmationForClient(messageFromBuffer.getSelectedKey());
        } catch (ParseException pe) {
            System.out.println("Error formatting incoming message to json");
        }

        return jsonMessage;
    }

    private void dataReceiveConfirmationForClient(SelectionKey selectedKey) {
        socketServer.sendToClient(selectedKey, "RECEIVED\r\n".getBytes());
    }
}
