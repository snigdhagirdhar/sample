package com.sample.parser;

public enum DataItemId {

    PAYLOAD(null),
    ID(MatchValueType.STRING),
    MESSAGE(null),
    MESSAGE_ID(MatchValueType.STRING),
    TYPE(MatchValueType.STRING),
    DESTINATION(MatchValueType.STRING),
    ;

    private final MatchValueType type;

    DataItemId(MatchValueType matchValueType) {
        this.type = matchValueType;
    }

    public MatchValueType getType(){
        return type;
    }
}
