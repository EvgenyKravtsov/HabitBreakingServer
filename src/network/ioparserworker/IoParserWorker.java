package network.ioparserworker;

import network.server.adt.MessageFromBuffer;

public interface IoParserWorker {

    void start();

    void putToParseQueue(MessageFromBuffer messageFromBuffer);
}
