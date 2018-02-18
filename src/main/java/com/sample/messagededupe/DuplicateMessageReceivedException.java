package com.sample.messagededupe;

import java.text.MessageFormat;

public class DuplicateMessageReceivedException extends RuntimeException {
    private final String message;
    private final String source;
    private static final String ERROR_MESSAGE_TEMPLATE = "Received duplicate message: {0} source: {1}";

    public DuplicateMessageReceivedException(String message, String source) {
        super(MessageFormat.format(ERROR_MESSAGE_TEMPLATE, message, source));
        this.message = message;
        this.source = source;
    }

    public String getMessageValue() {
        return message;
    }

    public String getSource() {
        return source;
    }
}