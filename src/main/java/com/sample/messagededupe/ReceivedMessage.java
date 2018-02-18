package com.sample.messagededupe;

import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
public class ReceivedMessage {
    private static final String NO_SOURCE_PROVIDED = "NO-SOURCE-PROVIDED";
    private final int timeToLive;
    private String message;
    private String source;
    private ZonedDateTime requestedTime;
    private ZonedDateTime purgeTime;
    private String id;


    public ReceivedMessage(String message, String source, ZonedDateTime requestedTime, int timeToLive) {
        this.message = message;
        this.source = (source != null ? source : NO_SOURCE_PROVIDED);
        this.requestedTime = requestedTime;
        this.timeToLive = timeToLive;
        this.purgeTime = calculatePurgeTime();
        this.id = createKey(message, this.source);
    }

    private ZonedDateTime calculatePurgeTime() {
        return this.requestedTime.plusDays(this.timeToLive);
    }

    public ReceivedMessage(String message, String source, Long requestedTimeMillis, int timeToLive) {
        this(message, source, getZonedDateTime(requestedTimeMillis), timeToLive);
    }

    private static ZonedDateTime getZonedDateTime(Long requestedTimeMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(requestedTimeMillis), ZoneId.of("UTC"));
    }

    public static String createKey(String message, String source) {
        return Strings.isNullOrEmpty(source) ? message : message + "_" + source;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public ZonedDateTime getRequestedTime() {
        return requestedTime;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ZonedDateTime getPurgeTime() {
        return purgeTime;
    }

    public String getId() {
        return id;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ReceivedMessage rhs = (ReceivedMessage) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }
}
