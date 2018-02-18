package com.sample.messagededupe;

public interface ReceivedMessageRepository {

    void save(ReceivedMessage receivedMessage);

    boolean exists(String key);

    default void purge(){}
}
