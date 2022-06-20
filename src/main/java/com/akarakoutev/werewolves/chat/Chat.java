package com.akarakoutev.werewolves.chat;

import com.akarakoutev.werewolves.player.Player;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.LocalTime;
import java.util.*;

public class Chat {

    private final List<Message> messages;
    private final Type type;
    private final int cycle;

    private final UUID id;

    public enum Type {
        WEREWOLVES("ww"),
        VAMPIRES("vamp"),
        DAY("day");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class Message {

        LocalTime timestamp;
        Player sender;
        String message;

        public Message(LocalTime timestamp, Player sender, String message) {
            this.timestamp = timestamp;
            this.sender = sender;
            this.message = message;
        }

        public LocalTime getTimestamp() {
            return timestamp;
        }

        public Player getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

    }

    public Chat(UUID id, Type type, int cycle) {
        messages = new ArrayList<>();
        this.type = type;
        this.cycle = cycle;
        this.id = id;
    }

    public void addMessage(LocalTime timestamp, Player sender, String message) {
        messages.add(new Message(timestamp, sender, message));
    }

    public Type getType() {
        return type;
    }

    public int getCycle() {
        return cycle;
    }

    public UUID getId() {
        return id;
    }

    public JsonObject serialize() {
        JsonObject serializedChat = new JsonObject();
        JsonArray serializedMessages = new JsonArray();
        for (Message message : messages) {
            JsonObject messageJson = new JsonObject();
            messageJson.addProperty("ts", message.getTimestamp().toString().split("[.]")[0]);
            messageJson.addProperty("sender", message.getSender().getName());
            messageJson.addProperty("message", message.getMessage());
            serializedMessages.add(messageJson);
        }
        serializedChat.add("messages", serializedMessages);
        serializedChat.addProperty("type", type.getValue());
        serializedChat.addProperty("cycle", Integer.toString(cycle));
        serializedChat.addProperty("id", id.toString());
        return serializedChat;
    }

}
