package com.akarakoutev.werewolves.net.exc;

import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class ExceptionUtil {

    public static ResponseEntity<String> errorResponse(Exception e) {
        e.printStackTrace();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof GameNotFoundException || e instanceof PlayerNotFoundException)
            status = HttpStatus.NOT_FOUND;
        if (e instanceof IOException || e instanceof LoginException)
            status = HttpStatus.FORBIDDEN;

        JsonObject content = MessageUtil.toContent("error", e.getMessage(), "code", Integer.toString(status.value()));
        String messageJson = MessageUtil.serialize(new ServerMessage(content, MessageType.ERROR));
        return new ResponseEntity<>(messageJson, status);
    }

    public static JsonObject errorResponseJson(Exception e) {
        e.printStackTrace();
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (e instanceof GameNotFoundException || e instanceof PlayerNotFoundException) {
            statusCode = HttpStatus.NOT_FOUND.value();
        } else if (e instanceof IOException || e instanceof  LoginException) {
            statusCode = HttpStatus.FORBIDDEN.value();
        }

        return MessageUtil.toContent("error", e.getMessage(), "code", Integer.toString(statusCode));
    }
}
