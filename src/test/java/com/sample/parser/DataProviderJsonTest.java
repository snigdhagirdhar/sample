package com.sample.parser;

import com.google.common.collect.ImmutableMap;
import com.sample.parser.DataItemId;
import com.sample.parser.DataProviderJson;
import com.sample.parser.MessageType;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static com.sample.parser.DataItemId.*;
import static com.sample.parser.MessageType.TYPEA;
import static com.sample.parser.MessageType.TYPEB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class DataProviderJsonTest {
    private static final String DEFAULT = "default";
    private DataProviderJson uut = new DataProviderJson();

    @Before
    public void setUp() throws Exception {
        Map<MessageType, DataProviderJson.MessagePaths> messagePathsMap = new HashMap<>();
        messagePathsMap.put(TYPEA, getMessagePaths(getAuditDataItemIdMap()));
        ReflectionTestUtils.setField(uut, "messagePaths", messagePathsMap);
    }

    @Test
    public void getData() throws Exception {
        String payload = "{ \"message\": {\"versionID\": \"1.0.0\"}, " +
                "\"payload\" : {\"id\": \"100\"," +
                " \"destination\": \"testChannelName\",  " +
                " \"unknownField\": null }" +
                "}";
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(ID, "100"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(DESTINATION, "testChannelName"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(MESSAGE_ID, null));
    }

    @Test
    public void getDataWithMissingFields() throws Exception {
        String payload = "{ \"message\": {\"versionID\": \"1.0.0\"}, " +
                "\"payload\" : {\"id\": \"100\"," +
                " \"destination\": \"testChannelName\" }" +
                "}";
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(ID, "100"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(DESTINATION, "testChannelName"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(MESSAGE_ID, null));
    }

    @Test
    public void getDataWithDifferentVersion() throws Exception {
        String payload = "{ \"message\": {\"id\": \"111\"}, " +
                "\"payload\" : {\"id\": \"100\"," +
                " \"destination\": \"testDestination\",  " +
                " \"unknownField\": \"770\" }" +
                "}";
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(ID, "100"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(DESTINATION, "testDestination"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(MESSAGE_ID, "111"));
    }

    @Test
    public void shouldGetDefaultItemsIfmessageVersionIsNull() throws Exception {
        String payload = "{ \"message\": {\"id\": \"111\"}, " +
                "\"payload\" : {\"id\": \"100\"," +
                " \"destination\": \"testDestination\",  " +
                " \"unknownField\": \"770\" }" +
                "}";
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(ID, "100"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(DESTINATION, "testDestination"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(MESSAGE_ID, "111"));
    }

    @Test
    public void shouldGetDefaultItemsIfmessageVersionIsEmpty() throws Exception {
        String payload = "{ \"message\": {\"id\": \"111\"}, " +
                "\"payload\" : {\"id\": \"100\"," +
                " \"destination\": \"testDestination\",  " +
                " \"unknownField\": \"770\" }" +
                "}";
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(ID, "100"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(DESTINATION, "testDestination"));
        assertThat(uut.getDataItems("typea-topic", payload), IsMapContaining.hasEntry(MESSAGE_ID, "111"));
    }

    @Test
    public void getJsonPathsByAuditDataItemIds() {
        Map<DataItemId, Map<String, String>> jsonPathsByAuditDataItemIds = uut.getMessagePaths().get(TYPEA).jsonPathsByAuditDataItemIds;

        assertThat(jsonPathsByAuditDataItemIds.size(), equalTo(6));
    }

    @Test
    public void getJsonPathsByAuditDataItemIdsValues() {
        HashMap<DataItemId, Map<String, String>> auditDataItemIdMap = getAuditDataItemIdMap();
        DataProviderJson.MessagePaths messagePaths = new DataProviderJson.MessagePaths(auditDataItemIdMap);

        assertThat(messagePaths.getJsonPathsByAuditDataItemIds(), equalTo(auditDataItemIdMap));
    }

    @Test
    public void setMessagePaths() {
        Map<MessageType, DataProviderJson.MessagePaths> messagePaths = ImmutableMap.of(TYPEA, getMessagePaths(getAuditDataItemIdMap()));
        uut.setMessagePaths(messagePaths);

        assertThat(uut.getMessagePaths(), equalTo(messagePaths));
    }

    @Test
    public void initialiseWithMessagePaths() {
        Map<MessageType, DataProviderJson.MessagePaths> messagePaths = ImmutableMap.of(TYPEA, getMessagePaths(getAuditDataItemIdMap()));
        uut = new DataProviderJson(messagePaths);

        assertThat(uut.getMessagePaths(), equalTo(messagePaths));
    }

    @Test
    public void getAuditItemDefaultJsonPaths() throws Exception {
        Map<MessageType, DataProviderJson.MessagePaths> messagePaths = ImmutableMap.of(TYPEA, getMessagePaths(getAuditDataItemIdMap()));
        uut = new DataProviderJson(messagePaths);

        Map<String, Map<String, String>> auditItemDefaultJsonPaths = uut.getDataItemDefaultJsonPaths(TYPEA);
        assertThat(auditItemDefaultJsonPaths, notNullValue());
        assertThat(auditItemDefaultJsonPaths.size(), equalTo(6));
        assertThat(auditItemDefaultJsonPaths.get(ID.name()).get(DEFAULT), equalTo("payload.id"));
        assertThat(auditItemDefaultJsonPaths.get(TYPE.name()).get(DEFAULT), equalTo("payload.type"));
        assertThat(auditItemDefaultJsonPaths.get(MESSAGE_ID.name()).get("1.0.1"), equalTo("message.testId"));
        assertThat(auditItemDefaultJsonPaths.get(DESTINATION.name()).get(DEFAULT), equalTo("payload.destination"));
        assertThat(auditItemDefaultJsonPaths.get(PAYLOAD.name()).get(DEFAULT), equalTo("payload"));
    }

    @Test
    public void getAuditItemDefaultJsonPaths_doesNotExistForGivenMessageType() throws Exception {
        Map<MessageType, DataProviderJson.MessagePaths> messagePaths = ImmutableMap.of(TYPEA, getMessagePaths(getAuditDataItemIdMap()));
        uut = new DataProviderJson(messagePaths);

        Map<String, Map<String, String>> auditItemDefaultJsonPaths = uut.getDataItemDefaultJsonPaths(TYPEB);
        assertThat(auditItemDefaultJsonPaths, notNullValue());
        assertThat(auditItemDefaultJsonPaths.size(), equalTo(0));
    }

    private DataProviderJson.MessagePaths getMessagePaths(HashMap<DataItemId, Map<String, String>> auditDataItemIdMap) {
        DataProviderJson.MessagePaths messagePaths = new DataProviderJson.MessagePaths();
        messagePaths.jsonPathsByAuditDataItemIds = auditDataItemIdMap;
        return messagePaths;
    }

    private HashMap<DataItemId, Map<String, String>> getAuditDataItemIdMap() {
        HashMap<DataItemId, Map<String, String>> map = new HashMap<>();
        map.put(ID, ImmutableMap.of(DEFAULT, "payload.id"));
        map.put(TYPE, ImmutableMap.of(DEFAULT, "payload.type", "1.0.1", "payload.typeValue"));
        map.put(DESTINATION, ImmutableMap.of(DEFAULT, "payload.destination"));
        map.put(MESSAGE, ImmutableMap.of(DEFAULT, "message"));
        map.put(MESSAGE_ID, ImmutableMap.of(DEFAULT, "message.id", "1.0.1", "message.testId"));
        map.put(PAYLOAD, ImmutableMap.of(DEFAULT, "payload"));
        return map;
    }
}
