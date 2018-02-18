package com.sample.messagededupe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Supplier;

/**
 * Jdbc Message Repository, this is used for saving and purging the messages in the database.
 */
@Repository
public class JdbcReceivedMessageRepository implements ReceivedMessageRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReceivedMessageRepository.class);
    private static final String SAVE_RECEIVED_MESSAGE_ID_SQL = "INSERT INTO RECEIVED_MESSAGE_ID ( " +
            "MESSAGE_ID_SOURCE, " +
            "MESSAGE_ID, SOURCE, " +
            "REQUESTED_TIME, " +
            "PURGE_TIME, " +
            "TIME_TO_LIVE) " +
            "VALUES(?,?,?,?,?,?)";

    private JdbcTemplate jdbcTemplate;
    private Supplier<Long> dateTimeSupplier;

    @Autowired
    public JdbcReceivedMessageRepository(@Qualifier("activitiJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, System::currentTimeMillis);
    }

    public JdbcReceivedMessageRepository(@Qualifier("activitiJdbcTemplate") JdbcTemplate jdbcTemplate, Supplier<Long> dateTimeSupplier) {
        this.dateTimeSupplier = dateTimeSupplier;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ReceivedMessage receivedMessage) {
        try {
            jdbcTemplate.update(SAVE_RECEIVED_MESSAGE_ID_SQL,
                    receivedMessage.getId(),
                    receivedMessage.getMessage(),
                    receivedMessage.getSource(),
                    receivedMessage.getRequestedTime().toInstant().toEpochMilli(),
                    receivedMessage.getPurgeTime().toInstant().toEpochMilli(),
                    receivedMessage.getTimeToLive());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        List<String> result = jdbcTemplate.queryForList("SELECT MESSAGE_ID_SOURCE FROM RECEIVED_MESSAGE_ID WHERE MESSAGE_ID_SOURCE = ?", String.class, key);
        return !result.isEmpty();
    }

    @Override
    public void purge() {
        try {
            int numberOfRecordsPurged = jdbcTemplate.update("DELETE FROM RECEIVED_MESSAGE_ID WHERE PURGE_TIME < ?", dateTimeSupplier.get());
            log.info("DeDupe: Purged {} record(s)", numberOfRecordsPurged);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }
}
