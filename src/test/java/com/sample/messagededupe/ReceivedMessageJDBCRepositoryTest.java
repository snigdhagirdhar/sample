package com.sample.messagededupe;

import messagededupe.JdbcReceivedMessageRepository;
import messagededupe.ReceivedMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ReceivedMessageJDBCRepositoryTest {
    private static final String EXPECTED_LOOKUP_SQL = "SELECT MESSAGE_ID_SOURCE FROM RECEIVED_MESSAGE_ID WHERE MESSAGE_ID_SOURCE = ?";
    private static final String EXPECTED_SAVE_SQL = "INSERT INTO RECEIVED_MESSAGE_ID ( " +
            "MESSAGE_ID_SOURCE, " +
            "MESSAGE_ID, SOURCE, " +
            "REQUESTED_TIME, " +
            "PURGE_TIME, " +
            "TIME_TO_LIVE) " +
            "VALUES(?,?,?,?,?,?)";
    private static final String EXPECTED_DELETE_SQL = "DELETE FROM RECEIVED_MESSAGE_ID WHERE PURGE_TIME < ?";
    private JdbcTemplate jdbcTemplate;
    private static final String MESSAGE_ID = "messageId";
    private static final String SOURCE = "source";
    private static final int TIME_TO_LIVE = 10;
    private Long staticDateTime;
    @Spy
    private Supplier<Long> dateTimeSupplier = new Supplier<Long>() {
        @Override
        public Long get() {
            return staticDateTime;
        }
    };

    private long getStaticTime() {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        return now.toInstant().toEpochMilli();
    }

    @Before
    public void createMocks() {
        staticDateTime = getStaticTime();
        jdbcTemplate = mock(JdbcTemplate.class);
    }

    @Test
    public void save_receivedMessageId() {
        ReceivedMessage receivedMessageId = createReceivedMessageId(MESSAGE_ID, SOURCE);
        givenMessageDoesNotExist(receivedMessageId);

        JdbcReceivedMessageRepository uut = new JdbcReceivedMessageRepository(jdbcTemplate);
        uut.save(receivedMessageId);

        verifyTheExpectedUpdateSQLCommandWasExecuted(receivedMessageId);
    }

    private void verifyTheExpectedUpdateSQLCommandWasExecuted(ReceivedMessage receivedMessageId) {
        verify(jdbcTemplate).update(EXPECTED_SAVE_SQL,
                receivedMessageId.getId(),
                receivedMessageId.getMessage(),
                receivedMessageId.getSource(),
                receivedMessageId.getRequestedTime().toInstant().toEpochMilli(),
                receivedMessageId.getPurgeTime().toInstant().toEpochMilli(),
                receivedMessageId.getTimeToLive());
    }

    private void givenMessageDoesNotExist(ReceivedMessage receivedMessageId) {
        when(jdbcTemplate.queryForList(EXPECTED_LOOKUP_SQL, String.class, receivedMessageId.getId()))
                .thenReturn(new ArrayList<>());
    }

    private ReceivedMessage createReceivedMessageId(String messageId, String source) {
        return new ReceivedMessage(messageId, source, ZonedDateTime.now(), TIME_TO_LIVE);
    }

    @Test
    public void exists_existingReceivedMessageId() throws Exception {
        ReceivedMessage receivedMessage = createReceivedMessageId(MESSAGE_ID, SOURCE);
        givenMessageDoesExist(receivedMessage);

        JdbcReceivedMessageRepository uut = new JdbcReceivedMessageRepository(jdbcTemplate);

        //When
        boolean result = uut.exists(receivedMessage.getId());

        //Then
        assertThat(result, is(equalTo(true)));
    }
    private void givenMessageDoesExist(ReceivedMessage receivedMessageId) {
        when(jdbcTemplate.queryForList(EXPECTED_LOOKUP_SQL, String.class, receivedMessageId.getId()))
                .thenReturn(asList(receivedMessageId.getId()));
    }

    @Test
    public void exists_receivedMessageIdDoesNotExist() throws Exception {
        ReceivedMessage receivedMessage = createReceivedMessageId(MESSAGE_ID, SOURCE);
        givenMessageDoesNotExist(receivedMessage);

        JdbcReceivedMessageRepository uut = new JdbcReceivedMessageRepository(jdbcTemplate);

        //When
        boolean result = uut.exists(receivedMessage.getId());

        //Then
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void purge_receivedMessageId() {
        JdbcReceivedMessageRepository uut = new JdbcReceivedMessageRepository(jdbcTemplate,dateTimeSupplier);
        uut.purge();
        verifyTheExpectedDeleteSQLCommandWasExecuted();
    }
    private void verifyTheExpectedDeleteSQLCommandWasExecuted() {
        verify(jdbcTemplate).update(EXPECTED_DELETE_SQL, staticDateTime);
    }



}