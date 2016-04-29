package storage.event;

import storage.model.MessageFromStorageType;

import java.nio.channels.SelectionKey;
import java.util.Map;

public class MessageFromStorage {

    private SelectionKey selectionKey;
    private MessageFromStorageType type;
    private Map<String, Object> data;

    ////

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public MessageFromStorageType getType() {
        return type;
    }

    public void setType(MessageFromStorageType type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
