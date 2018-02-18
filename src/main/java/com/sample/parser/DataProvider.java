package com.sample.parser;

import java.util.Map;

public interface DataProvider {
    Map<DataItemId, Object> getDataItems(String sourceTopic, String payload);

    Map<String, Map<String, String>> getDataItemDefaultJsonPaths(MessageType messageType);
}
