package network.server.adt;

import java.nio.channels.SelectionKey;

public class MessageFromBuffer {

    private byte[] data;
    private int size;
    private SelectionKey selectedKey;

    ////

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SelectionKey getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(SelectionKey selectedKey) {
        this.selectedKey = selectedKey;
    }
}
