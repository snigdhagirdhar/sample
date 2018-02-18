package com.sample.messagededupe;

import messagededupe.HashMapReceivedMessageRepository;
import messagededupe.ReceivedMessage;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReceivedMessageHashMapRepositoryTest {
    private static final String MESSAGE = "messageId";
    private static final String SOURCE = "source";
    private static final String DEFAULT_SOURCE = "NO-SOURCE-PROVIDED";
    private static final int TIME_TO_LIVE = 10;

    private HashMapReceivedMessageRepository uut;

    @Before
    public void setUp() throws Exception {
        uut = new HashMapReceivedMessageRepository();
    }

    @Test
    public void save_ReceivedMessage() {
        ReceivedMessage receivedMessage = createReceivedMessage(MESSAGE, SOURCE);

        uut.save(receivedMessage);
    }

    private ReceivedMessage createReceivedMessage(String message, String source) {
        return new ReceivedMessage(message, source, ZonedDateTime.now(), TIME_TO_LIVE);
    }

    @Test
    public void exists_existingMessageForSameSource() throws Exception {
        givenExistingMessageId(MESSAGE, SOURCE);

        assertThat(uut.exists(ReceivedMessage.createKey(MESSAGE, SOURCE)), is(true));
    }

    private void givenExistingMessageId(String messageId, String source) {
        ReceivedMessage ReceivedMessage = createReceivedMessage(messageId, source);
        uut.save(ReceivedMessage);
    }

    @Test
    public void exists_existingMessageButForDifferentSource() throws Exception {
        givenExistingMessageId(MESSAGE, SOURCE);

        assertThat(uut.exists(ReceivedMessage.createKey(MESSAGE, "an-alternative-source")), is(false));
    }

    @Test
    public void exists_existingMessageWithNoSourceProvided() throws Exception {
        givenExistingMessageId(MESSAGE, null);

        assertThat(uut.exists(ReceivedMessage.createKey(MESSAGE, DEFAULT_SOURCE)), is(true));
    }

    @Test
    public void exists_messageDoesNotExist() throws Exception {
        assertThat(uut.exists(ReceivedMessage.createKey("messageId2", null)), is(false));
    }

}