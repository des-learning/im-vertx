package com.desdulianto.learning.imvertx.packet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// class untuk menampung format json
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class ChatMessage {
    private final UUID id;

    public UUID getId() {
        return id;
    }

    @JsonCreator
    public ChatMessage() {
        this.id = UUID.randomUUID();
    }

    @JsonCreator
    public ChatMessage(@JsonProperty("id") final UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                '}';
    }
}
