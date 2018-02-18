package com.sample.messagededupe;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class ReceivedMessageTest {

    private static final String MESSAGE_ID = "1234";
    private static final String SOURCE = "Graphite";
    private static final int TIME_TO_LIVE = 10;
    private static final long AUGUST_14TH_2017 = 1502710696000L;

    @Test
    public void create_ReceivedMessage() {
        ReceivedMessage ReceivedMessage = new ReceivedMessage(MESSAGE_ID, SOURCE, AUGUST_14TH_2017, TIME_TO_LIVE);

        assertThat(ReceivedMessage.getId(), equalTo("1234_Graphite"));
        assertThat(ReceivedMessage.getMessage(), equalTo("1234"));
        assertThat(ReceivedMessage.getSource(), equalTo("Graphite"));
        assertThat(ReceivedMessage.getRequestedTime(), equalTo(getExpectedRequestedTime()));
        assertThat(ReceivedMessage.getPurgeTime(), equalTo(getExpectedPurgeTime()));
        assertThat(ReceivedMessage.getTimeToLive(), equalTo(TIME_TO_LIVE));
    }

    private ZonedDateTime getExpectedRequestedTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(AUGUST_14TH_2017), ZoneId.of("UTC"));
    }

    private ZonedDateTime getExpectedPurgeTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(AUGUST_14TH_2017), ZoneId.of("UTC"));
        return zdt.plusDays(TIME_TO_LIVE);
    }

    @Test
    public void create_ReceivedMessage_should_have_default_source() {
        ReceivedMessage ReceivedMessage = new ReceivedMessage(MESSAGE_ID, null, AUGUST_14TH_2017, TIME_TO_LIVE);

        assertThat(ReceivedMessage.getId(), equalTo("1234_NO-SOURCE-PROVIDED"));
    }

    @Test
    public void equality_ReceivedMessage_should_be_equal_on_messageId_and_source() {
        ReceivedMessage ReceivedMessageOne = new ReceivedMessage(MESSAGE_ID, null, AUGUST_14TH_2017, TIME_TO_LIVE);
        ReceivedMessage ReceivedMessageTwo = new ReceivedMessage(MESSAGE_ID, null, AUGUST_14TH_2017, TIME_TO_LIVE);
        ReceivedMessage ReceivedMessageThree = new ReceivedMessage(MESSAGE_ID, "Cheese", AUGUST_14TH_2017, TIME_TO_LIVE);

        assertThat(ReceivedMessageOne, equalTo(ReceivedMessageTwo));
        assertThat(ReceivedMessageOne.hashCode(), equalTo(ReceivedMessageTwo.hashCode()));
        assertThat(ReceivedMessageOne, not(equalTo(ReceivedMessageThree)));
    }

}