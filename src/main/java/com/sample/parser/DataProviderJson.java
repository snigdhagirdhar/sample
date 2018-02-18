package com.sample.parser;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.persistence.EnumType;
import javax.persistence.MapKeyEnumerated;
import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "messageTypeMappings")
public class DataProviderJson implements DataProvider {

    private static final String PATH_VERSION = "header.versionID";
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviderJson.class);

    @MapKeyEnumerated(EnumType.STRING)
    private Map<MessageType, MessagePaths> messagePaths = new EnumMap<>(MessageType.class);

    public Map<MessageType, MessagePaths> getMessagePaths() {
        return messagePaths;
    }

    public void setMessagePaths(Map<MessageType, MessagePaths> messagePaths) {
        this.messagePaths = messagePaths;
    }

    public DataProviderJson(Map<MessageType, MessagePaths> messagePaths) {
        this.messagePaths = messagePaths;
    }

    public DataProviderJson() {
    }

    @Override
    public Map<DataItemId, Object> getDataItems(String sourceTopic, String payload) {
        return getDataItems(payload, this.messagePaths.get(TopicMessageType.getMessageType(sourceTopic)).jsonPathsByAuditDataItemIds);
    }

    @Override
    public Map<String, Map<String, String>> getDataItemDefaultJsonPaths(MessageType messageType) {
        DataProviderJson.MessagePaths messageJsonPaths = this.messagePaths.get(messageType);
        if (messageJsonPaths == null) {
            return Maps.newHashMap();
        }
        Map<DataItemId, Map<String, String>> jsonPathsByAuditDataItemIds = messageJsonPaths.getJsonPathsByAuditDataItemIds();
        Map<String, Map<String, String>> dataItemsWithPaths = Maps.newHashMap();
        jsonPathsByAuditDataItemIds
                .entrySet()
                .forEach(auditDataItemIdMapEntry ->
                        dataItemsWithPaths.put(auditDataItemIdMapEntry.getKey().name(), auditDataItemIdMapEntry.getValue()));

        return dataItemsWithPaths;
    }

    protected Map<DataItemId, Object> getDataItems(String message, Map<DataItemId, Map<String, String>> jsonPathsByAuditDataItemIds) {

        Map<DataItemId, Object> data = new EnumMap<>(DataItemId.class);
        DocumentContext documentContext = JsonPath.parse(message);

        String payloadVersion = null;

        try {
            payloadVersion = documentContext.read(PATH_VERSION);
        } catch (PathNotFoundException ex) {
            LOGGER.trace("Header Version Id [{}] does not exist in given message, proceeding with default paths", PATH_VERSION, ex);
        }

        final String finalPayloadVersion = payloadVersion;

        jsonPathsByAuditDataItemIds.keySet().forEach(dataItemId -> {
            String dataItemJsonPath = getDataItemJsonPathForVersion(finalPayloadVersion, jsonPathsByAuditDataItemIds.get(dataItemId));
            try {
                data.put(dataItemId, documentContext.read(dataItemJsonPath));
            } catch (PathNotFoundException ex) {
                LOGGER.trace("Json path [{}] not found in the given payload, exception received", dataItemJsonPath, ex);
                data.put(dataItemId, null);
            }
        });

        return data;
    }

    private String getDataItemJsonPathForVersion(String payloadVersion, Map<String, String> jsonPathsByVersion) {
        return Strings.isNullOrEmpty(payloadVersion) || Strings.isNullOrEmpty(jsonPathsByVersion.get(payloadVersion)) ? jsonPathsByVersion.get("default") : jsonPathsByVersion.get(payloadVersion);
    }

    public static class MessagePaths {
        Map<DataItemId, Map<String, String>> jsonPathsByAuditDataItemIds = new EnumMap<>(DataItemId.class);

        public MessagePaths() {
        }

        public MessagePaths(Map<DataItemId, Map<String, String>> jsonPathsByAuditDataItemIds) {
            this.jsonPathsByAuditDataItemIds = jsonPathsByAuditDataItemIds;
        }

        public Map<DataItemId, Map<String, String>> getJsonPathsByAuditDataItemIds() {
            return jsonPathsByAuditDataItemIds;
        }
    }
}
