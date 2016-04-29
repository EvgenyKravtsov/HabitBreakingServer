package network.protocol;

import com.google.common.eventbus.Subscribe;
import network.parser.event.ParsedToJsonMessage;
import network.protocol.event.BytesToSocket;
import network.protocol.event.QueryToStorage;
import network.protocol.model.QueryType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import storage.event.MessageFromStorage;
import util.GuavaEventBusManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonProtocol {

    // Message keys
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_EMAIL = "EMAIL";
    private static final String KEY_GENDER = "GENDER";
    private static final String KEY_AGE = "AGE";
    private static final String KEY_REGISTRATION_DATE = "REGISTRATION_DATE";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_COUNT = "COUNT";
    private static final String KEY_STAITSITC_DATA = "STATISTIC_DATA";

    // Message types
    private static final String TYPE_REGISTRATION = "REGISTRATION";
    private static final String TYPE_STATISTIC = "STATISTIC";
    private static final String TYPE_STORAGE = "STORAGE";
    private static final String TYPE_GET_STATISTIC = "GET_STATISTIC";

    private static JsonProtocol instance;

    ////

    private JsonProtocol() {}

    public static void init() {
        instance = new JsonProtocol();
        GuavaEventBusManager.getBus().register(instance);
    }

    ////

    public void handleMessage(ParsedToJsonMessage message) {
        if (message != null) {
            String type = (String) message.getJsonMessage().get(KEY_TYPE);

            switch (type) {
                case TYPE_REGISTRATION:
                    handleRegistration(message);
                    break;
                case TYPE_STATISTIC:
                    handleStatitstic(message);
                    break;
                case TYPE_GET_STATISTIC:
                    handeGetStatistic(message);
                    break;
            }
        }
    }

    ////

    private void handleRegistration(ParsedToJsonMessage message) {
        String email = (String) message.getJsonMessage().get(KEY_EMAIL);
        Long gender = (long) message.getJsonMessage().get(KEY_GENDER);
        Long age = (long) message.getJsonMessage().get(KEY_AGE);
        long registrationDate = (long) message.getJsonMessage().get(KEY_REGISTRATION_DATE);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_EMAIL, email);
        dataMap.put(QueryToStorage.DATAKEY_GENDER, gender.intValue());
        dataMap.put(QueryToStorage.DATAKEY_AGE, age.intValue());
        dataMap.put(QueryToStorage.DATAKEY_REGISTRATION_DATE, registrationDate);

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.WRITE_NEW_USER);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handleStatitstic(ParsedToJsonMessage message) {
        String email = (String) message.getJsonMessage().get(KEY_EMAIL);
        Long date = (long) message.getJsonMessage().get(KEY_DATE);
        Long count = (long) message.getJsonMessage().get(KEY_COUNT);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_EMAIL, email);
        dataMap.put(QueryToStorage.DATAKEY_DATE, date);
        dataMap.put(QueryToStorage.DATAKEY_COUNT, count.intValue());

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.WRITE_STATISTIC);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handeGetStatistic(ParsedToJsonMessage message) {
        String email = (String) message.getJsonMessage().get(KEY_EMAIL);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_EMAIL, email);

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.GET_STATISTIC);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private byte[] prepareStorageWriteSuccessMessage() {
        JSONObject message = new JSONObject();

        String messageContent = "WRITE_SUCCESS";

        message.put(TYPE_STORAGE, messageContent);
        return (message.toJSONString() + "\r\n").getBytes();
    }

    private byte[] prepareStorageDuplicateErrorMessage() {
        JSONObject message = new JSONObject();

        String messageContent = "DUPLICATE_ERROR";

        message.put(TYPE_STORAGE, messageContent);
        return (message.toJSONString() + "\r\n").getBytes();
    }

    private byte[] prepareStorageStatisticDataMessage(MessageFromStorage messageFromStorage) {
        JSONObject message = new JSONObject();

        JSONArray statisticData = new JSONArray();

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) messageFromStorage.getData().get(QueryToStorage.DATAKEY_STATISTIC_DATA);

        for (Map<String, Object> dataEntry : dataList) {
            JSONObject jsonDataEntry = new JSONObject();
            jsonDataEntry.put(KEY_DATE, dataEntry.get(QueryToStorage.DATAKEY_DATE));
            jsonDataEntry.put(KEY_COUNT, dataEntry.get(QueryToStorage.DATAKEY_COUNT));
            statisticData.add(jsonDataEntry);
        }

        message.put(KEY_STAITSITC_DATA, statisticData);
        return (message.toJSONString() + "\r\n").getBytes();
    }

    ////

    @Subscribe
    public void handleParsedToJsonMessageEvent(ParsedToJsonMessage event) {
        handleMessage(event);
    }

    @Subscribe
    public void handleMessageFromStorageEvent(MessageFromStorage event) {
        byte[] data = null;

        switch (event.getType()) {
            case WRITE_SUCCESS:
                data = prepareStorageWriteSuccessMessage();
                break;
            case DUPLICATE_ERROR:
                data = prepareStorageDuplicateErrorMessage();
                break;
            case STATISTIC_DATA:
                data = prepareStorageStatisticDataMessage(event);
                break;
        }

        BytesToSocket bytesToSocket = new BytesToSocket();
        bytesToSocket.setSelectionKey(event.getSelectionKey());
        bytesToSocket.setData(data);
        GuavaEventBusManager.getBus().post(bytesToSocket);
    }
}
