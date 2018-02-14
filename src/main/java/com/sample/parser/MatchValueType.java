package com.sample.parser;

import org.apache.commons.lang3.StringUtils;

/**
 * Type specific matching.
 */
public enum MatchValueType {
    STRING{
        @Override
        <T> boolean areEqual(T messageValue, String matchValue) {
            return messageValue.equals(matchValue);
        }

        @Override
        <T> boolean notEmpty(T messageValue) {
            return StringUtils.isNotEmpty((String)messageValue);
        }
    },
    INT {
        @Override
        <T> boolean areEqual(T messageValue, String matchValue) {
            return messageValue.equals(Integer.valueOf(matchValue));
        }

        @Override
        <T> boolean notEmpty(T messageValue) {
            return messageValue != null;
        }
    };

    abstract <T> boolean areEqual(T messageValue, String matchValue);
    abstract <T> boolean notEmpty(T messageValue);
}
