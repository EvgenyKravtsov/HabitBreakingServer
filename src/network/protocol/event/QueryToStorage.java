package network.protocol.event;

import network.protocol.model.QueryType;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class QueryToStorage {

    // Keys for data map
    public static final String DATAKEY_EMAIL = "email";
    public static final String DATAKEY_GENDER = "gender";
    public static final String DATAKEY_AGE = "age";
    public static final String DATAKEY_REGISTRATION_DATE = "registration_date";
    public static final String DATAKEY_DATE = "date";
    public static final String DATAKEY_COUNT = "count";
    public static final String DATAKEY_STATISTIC_DATA = "statistic_data";

    private SelectionKey selectionKey;
    private QueryType queryType;
    private Map<String, Object> dataMap;

    ////

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
}
