package network.protocol.event;

import java.nio.channels.SelectionKey;

public class BytesToSocket {

    private SelectionKey selectionKey;
    private byte[] data;

    ////

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
