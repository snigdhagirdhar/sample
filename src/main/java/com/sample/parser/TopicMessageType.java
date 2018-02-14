package com.sample.parser;

import org.apache.commons.lang3.StringUtils;


public class TopicMessageType {

    public static final String EXCLUDE = "exclude";

    public static MessageType getMessageType(String topicIn) {
        if (StringUtils.isEmpty(topicIn) || topicIn.indexOf("-") == -1) {
            throw new IllegalArgumentException("No MessageType mapping for topic " + topicIn);
        }
        try {
            String prefix = topicIn.substring(0, topicIn.indexOf("-"));
            if (prefix.equals(EXCLUDE)) {
                prefix = topicIn.substring(7, topicIn.indexOf("-", 7));
            }
            return MessageType.valueOf(prefix.toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("No mapping for topic " + topicIn, e);
        }
    }

    private TopicMessageType() {
    }
}
