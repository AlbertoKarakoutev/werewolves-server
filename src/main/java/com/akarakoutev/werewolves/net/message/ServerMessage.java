package com.akarakoutev.werewolves.net.message;

import com.google.gson.JsonElement;

public class ServerMessage extends Message {

     public ServerMessage(JsonElement content, MessageType messageType) {
         sender = "SERVER";
         this.type = messageType;
         this.content = content;
     }

     public ServerMessage(MessageType messageType) {
         sender = "SERVER";
         this.type = messageType;
     }

     public ServerMessage() {
         sender = "SERVER";
     }

}
