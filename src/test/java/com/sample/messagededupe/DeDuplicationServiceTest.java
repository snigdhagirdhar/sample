package com.sample.messagededupe;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeDuplicationServiceTest {

    private static final String TEST_MESSAGE_ID = "test-Message";
    private static final String TEST_SOURCE = "test-source";
    @Mock
    private ReceivedMessageRepository receivedMessageRepository;

    private Long staticDateTime;
    @Spy
    private Supplier<Long> dateTimeSupplier = new Supplier<Long>() {
        @Override
        public Long get() {
            return staticDateTime;
        }
    };

    @Captor
    private ArgumentCaptor<ReceivedMessage> receivedMessageCaptor;

    @InjectMocks
    private DeDuplicationService uut;

    @Test
    public void deDupe_MessageDoesNotAlreadyExistWithSource() {
        givenMessageDoesNotExist(TEST_MESSAGE_ID, TEST_SOURCE);

        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        staticDateTime = now.toInstant().toEpochMilli();

        uut.deDupe(TEST_MESSAGE_ID, TEST_SOURCE);

        verify(receivedMessageRepository).save(receivedMessageCaptor.capture());

        ReceivedMessage receivedMessage = receivedMessageCaptor.getValue();
        assertThat(receivedMessage, notNullValue());
        assertThat(receivedMessage.getMessage(), equalTo(TEST_MESSAGE_ID));
        assertThat(receivedMessage.getSource(), equalTo(TEST_SOURCE));
        assertThat(receivedMessage.getRequestedTime(), equalTo(now));
    }

    private void givenMessageDoesNotExist(String Message, String source) {
        givenMessageExists(Message, source, false);
    }

    private void givenMessageExists(String Message, String source, boolean exists) {
        given(receivedMessageRepository.exists(ReceivedMessage.createKey(Message, source)))
                .willReturn(exists);
    }

    private void givenMessageExists(String Message, String source) {
        givenMessageExists(Message, source, true);
    }

    @Test
    public void save_duplicateMessage() {
        givenMessageExists(TEST_MESSAGE_ID, TEST_SOURCE);
        //when
        try {
            uut.deDupe(TEST_MESSAGE_ID, TEST_SOURCE);
            fail();
        } catch (DuplicateMessageReceivedException ex) {
            assertThat(ex.getMessageValue(), CoreMatchers.equalTo(TEST_MESSAGE_ID));
            assertThat(ex.getSource(), CoreMatchers.equalTo(TEST_SOURCE));
            assertThat(ex.getMessage(), CoreMatchers.equalTo("Received duplicate message: " + TEST_MESSAGE_ID + " source: " + TEST_SOURCE));
        }
    }

    @Test
    public void deDupe_nullMessageProvided() {
        givenMessageDoesNotExist(TEST_MESSAGE_ID, TEST_SOURCE);
        uut.deDupe(null, null);
        verify(receivedMessageRepository, never()).save(receivedMessageCaptor.capture());
    }

    @Test
    public void purge_messagesCanBePurged() {
        uut.purge();
        verify(receivedMessageRepository).purge();
    }
}