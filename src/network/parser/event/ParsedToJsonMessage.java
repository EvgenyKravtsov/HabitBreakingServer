package network.parser.event;

import org.json.simple.JSONObject;

import java.nio.channels.SelectionKey;

public class ParsedToJsonMessage {

    private SelectionKey selectionKey;
    private JSONObject jsonMessage;

    ////

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public JSONObject getJsonMessage() {
        return jsonMessage;
    }

    public void setJsonMessage(JSONObject jsonMessage) {
        this.jsonMessage = jsonMessage;
    }
}
