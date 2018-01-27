package com.desdulianto.learning.imvertx.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// class untuk menampung format json
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class Message {
    private final UUID id;

    public UUID getId() {
        return id;
    }

    @JsonCreator
    public Message() {
        this.id = UUID.randomUUID();
    }

    @JsonCreator
    public Message(@JsonProperty("id") final UUID id) {
        this.id = id;
    }
}
