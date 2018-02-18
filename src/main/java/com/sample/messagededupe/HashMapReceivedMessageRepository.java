package com.sample.messagededupe;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory message repository. This is used keeping all messages in memory.
 * The memory is cleared when the instance of the application is stopped.
 */
@Repository
public class HashMapReceivedMessageRepository implements ReceivedMessageRepository {
    private Map<String, ReceivedMessage> receivedMessages = new HashMap<>();

    @Override
    public void save(ReceivedMessage receivedMessage) {
        receivedMessages.put(receivedMessage.getId(), receivedMessage);
    }

    @Override
    public boolean exists(String key) {
        return receivedMessages.get(key) != null;
    }
}
