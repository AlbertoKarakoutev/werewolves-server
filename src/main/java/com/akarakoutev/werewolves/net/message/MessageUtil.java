package com.akarakoutev.werewolves.net.message;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MessageUtil {

    private static final Gson serializer = new Gson();

    public static JsonObject deserialize(String messageStr) {
        return JsonParser.parseString(messageStr).getAsJsonObject().get("content").getAsJsonObject();
    }

    public static String serialize(Message message) {
        return serializer.toJson(message);
    }

    public static <T> T fromContent(JsonElement json, Class<T> clazz) {
        return serializer.fromJson(json, clazz);
    }

    public static JsonObject toContent(String... keyValues) {
        if (keyValues.length%2 != 0)
            throw new IllegalArgumentException();
        JsonObject content = new JsonObject();
        for (int i = 0; i < keyValues.length-1; i+=2) {
            content.addProperty(keyValues[i], keyValues[i+1]);
        }
        return content;
    }

    public static String emptyMessage() {
        return serialize(new ServerMessage(MessageType.DATA));
    }

}
