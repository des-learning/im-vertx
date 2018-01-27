package com.desdulianto.learning.imvertx.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TextMessage extends Message {
    private String message;

    @JsonCreator
    public TextMessage(@JsonProperty("message") final String message) {
        super();
        this.message = message;
    }

    public TextMessage() {
        this("");
    }

    public String getMessage() {
        return message;
    }
}
