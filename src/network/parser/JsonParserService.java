package network.parser;

import com.google.common.eventbus.Subscribe;
import network.parser.event.ParsedToJsonMessage;
import network.server.event.BytesFromSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.GuavaEventBusManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JsonParserService implements Runnable {

    private static JsonParserService instance;

    private boolean parserStatus;
    private BlockingQueue<BytesFromSocket> incomingMessagesQueue;

    ////

    private JsonParserService() {
        incomingMessagesQueue = new ArrayBlockingQueue<>(1024);
    }

    public static JsonParserService getInstance() {
        if (instance == null) {
            instance = new JsonParserService();
        }

        return instance;
    }

    ////

    public void start() {
        parserStatus = true;
        GuavaEventBusManager.getBus().register(this);
        Thread parserThread = new Thread(this);
        parserThread.start();
    }

    ////

    @Override
    public void run() {
        while (parserStatus) {
            try {
                List<ParsedToJsonMessage> jsonMessages = parseIncomingMessageToJson(incomingMessagesQueue.take());

                for (ParsedToJsonMessage jsonMessage : jsonMessages) {
                    GuavaEventBusManager.getBus().post(jsonMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ////

    private List<ParsedToJsonMessage> parseIncomingMessageToJson(BytesFromSocket bytesFromSocket) {
        JSONParser jsonParser = new JSONParser();

        int size = bytesFromSocket.getSize();
        byte[] data = bytesFromSocket.getData();

        byte[] dataCopy = new byte[size];
        System.arraycopy(data, 0, dataCopy, 0, size);

        String[] jsons = new String(dataCopy).split("\n");

        List<ParsedToJsonMessage> jsonMessages = new ArrayList<>();
        for (String jsonString : jsons) {
            try {
                ParsedToJsonMessage jsonMessage = new ParsedToJsonMessage();
                jsonMessage.setSelectionKey(bytesFromSocket.getSelectionKey());
                jsonMessage.setJsonMessage((JSONObject) jsonParser.parse(jsonString));
                jsonMessages.add(jsonMessage);
            } catch (ParseException pe) {
                System.out.println("\nError formatting incoming message to json:  " + new String(bytesFromSocket.getData()));
            }
        }

        return jsonMessages;
    }

    private void putToParseQueue(BytesFromSocket bytesFromSocket) {
        try {
            incomingMessagesQueue.put(bytesFromSocket);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    ////

    @Subscribe
    public void handleBytesReadFromSocketEvent(BytesFromSocket event) {
        putToParseQueue(event);
    }
}
