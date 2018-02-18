package messagededupe;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

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
