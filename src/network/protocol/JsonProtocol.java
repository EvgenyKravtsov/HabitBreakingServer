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
import util.StringProcessor;

import javax.rmi.CORBA.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonProtocol {

    // Message keys
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_GENDER = "GENDER";
    private static final String KEY_DATE_OF_BIRTH = "DATE_OF_BIRTH";
    private static final String KEY_SECRET_QUESTION = "SECRET_QUESTION";
    private static final String KEY_SECRET_QUESTION_ANSWER = "SECRET_QUESTION_ANSWER";
    private static final String KEY_REGISTRATION_DATE = "REGISTRATION_DATE";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_COUNT = "COUNT";
    private static final String KEY_STAITSITC_DATA = "STATISTIC_DATA";

    // Message types
    private static final String TYPE_REGISTRATION = "REGISTRATION";
    private static final String TYPE_RESTORATION = "RESTORATION";
    private static final String TYPE_STATISTIC = "STATISTIC";
    private static final String TYPE_STORAGE = "STORAGE";
    private static final String TYPE_GET_STATISTIC = "GET_STATISTIC";
    private static final String TYPE_DELETE_USER = "DELETE_USER";

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
                case TYPE_RESTORATION:
                    handleRestoration(message);
                    break;
                case TYPE_STATISTIC:
                    handleStatitstic(message);
                    break;
                case TYPE_GET_STATISTIC:
                    handeGetStatistic(message);
                    break;
                case TYPE_DELETE_USER:
                    handleDeleteUser(message);
                    break;
            }
        }
    }

    ////

    private void handleRegistration(ParsedToJsonMessage message) {
        String name = (String) message.getJsonMessage().get(KEY_NAME);
        Long gender = (long) message.getJsonMessage().get(KEY_GENDER);
        Long dateOfBirth = (long) message.getJsonMessage().get(KEY_DATE_OF_BIRTH);
        String secretQuestion = (String) message.getJsonMessage().get(KEY_SECRET_QUESTION);
        String secretQuestionAnswer = (String) message.getJsonMessage().get(KEY_SECRET_QUESTION_ANSWER);
        long registrationDate = (long) message.getJsonMessage().get(KEY_REGISTRATION_DATE);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_NAME, StringProcessor.encodeStringToUtf8(name));
        dataMap.put(QueryToStorage.DATAKEY_GENDER, gender.intValue());
        dataMap.put(QueryToStorage.DATAKEY_DATE_OF_BIRTH, dateOfBirth);
        dataMap.put(QueryToStorage.DATAKEY_SECRET_QUESTION, StringProcessor.encodeStringToUtf8(secretQuestion));
        dataMap.put(QueryToStorage.DATAKEY_SECRET_QUESTION_ANSWER, StringProcessor.encodeStringToUtf8(secretQuestionAnswer));
        dataMap.put(QueryToStorage.DATAKEY_REGISTRATION_DATE, registrationDate);

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.WRITE_NEW_USER);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handleRestoration(ParsedToJsonMessage message) {
        String name = (String) message.getJsonMessage().get(KEY_NAME);
        Long gender = (long) message.getJsonMessage().get(KEY_GENDER);
        Long dateOfBirth = (long) message.getJsonMessage().get(KEY_DATE_OF_BIRTH);
        String secretQuestion = (String) message.getJsonMessage().get(KEY_SECRET_QUESTION);
        String secretQuestionAnswer = (String) message.getJsonMessage().get(KEY_SECRET_QUESTION_ANSWER);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_NAME, StringProcessor.encodeStringToUtf8(name));
        dataMap.put(QueryToStorage.DATAKEY_GENDER, gender.intValue());
        dataMap.put(QueryToStorage.DATAKEY_DATE_OF_BIRTH, dateOfBirth);
        dataMap.put(QueryToStorage.DATAKEY_SECRET_QUESTION, StringProcessor.encodeStringToUtf8(secretQuestion));
        dataMap.put(QueryToStorage.DATAKEY_SECRET_QUESTION_ANSWER, StringProcessor.encodeStringToUtf8(secretQuestionAnswer));

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.RESTORE_USER);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handleStatitstic(ParsedToJsonMessage message) {
        Long registrationDate = (long) message.getJsonMessage().get(KEY_REGISTRATION_DATE);
        Long date = (long) message.getJsonMessage().get(KEY_DATE);
        Long count = (long) message.getJsonMessage().get(KEY_COUNT);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_REGISTRATION_DATE, registrationDate);
        dataMap.put(QueryToStorage.DATAKEY_DATE, date);
        dataMap.put(QueryToStorage.DATAKEY_COUNT, count.intValue());

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.WRITE_STATISTIC);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handeGetStatistic(ParsedToJsonMessage message) {
        long registrationDate = (long) message.getJsonMessage().get(KEY_REGISTRATION_DATE);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_REGISTRATION_DATE, registrationDate);

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.GET_STATISTIC);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private void handleDeleteUser(ParsedToJsonMessage message) {
        long registrationDate = (long) message.getJsonMessage().get(KEY_REGISTRATION_DATE);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(QueryToStorage.DATAKEY_REGISTRATION_DATE, registrationDate);

        QueryToStorage queryToStorage = new QueryToStorage();
        queryToStorage.setSelectionKey(message.getSelectionKey());
        queryToStorage.setQueryType(QueryType.DELETE_USER);
        queryToStorage.setDataMap(dataMap);

        GuavaEventBusManager.getBus().post(queryToStorage);
    }

    private byte[] prepareStorageWriteSuccessMessage() {
        JSONObject message = new JSONObject();

        String messageContent = "WRITE_SUCCESS";

        message.put(TYPE_STORAGE, messageContent);
        return (message.toJSONString() + "\r\n").getBytes();
    }

    private byte[] prepareUserFoundMessage(MessageFromStorage messageFromStorage) {
        JSONObject message = new JSONObject();

        String messageContent = "USER_FOUND";

        message.put(TYPE_STORAGE, messageContent);
        message.put(KEY_NAME, messageFromStorage.getData().get(QueryToStorage.DATAKEY_NAME));
        message.put(KEY_REGISTRATION_DATE, messageFromStorage.getData().get(QueryToStorage.DATAKEY_REGISTRATION_DATE));

        return (message.toJSONString() + "\r\n").getBytes();
    }

    private byte[] prepareUserNotFoundMessage() {
        JSONObject message = new JSONObject();

        String messageContent = "USER_NOT_FOUND";

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

    private byte[] prepareUserDeletedMessage() {
        JSONObject message = new JSONObject();

        String messageContent = "USER_DELETED";

        message.put(TYPE_STORAGE, messageContent);
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
            case USER_FOUND:
                data = prepareUserFoundMessage(event);
                break;
            case USER_NOT_FOUND:
                data = prepareUserNotFoundMessage();
                break;
            case DUPLICATE_ERROR:
                data = prepareStorageDuplicateErrorMessage();
                break;
            case STATISTIC_DATA:
                data = prepareStorageStatisticDataMessage(event);
                break;
            case USER_DELETED:
                data = prepareUserDeletedMessage();
                break;
        }

        BytesToSocket bytesToSocket = new BytesToSocket();
        bytesToSocket.setSelectionKey(event.getSelectionKey());
        bytesToSocket.setData(data);
        GuavaEventBusManager.getBus().post(bytesToSocket);
    }
}
