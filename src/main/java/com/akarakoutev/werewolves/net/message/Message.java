package com.akarakoutev.werewolves.net.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Message {

	String sender;
	MessageType type;
	JsonElement content;

	public Message(JsonObject content, MessageType messageType) {
		this.type = messageType;
		this.content = content;
	}

	public Message(JsonObject content) {
		this.content = content;
	}

	public Message() {
	}

	public MessageType getType() {
		return type;
	}
	public String getSender() {
		return sender;
	}
	public JsonElement getContent() {
		return content;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public Message withType(MessageType type) {
		this.type = type;
		return this;
	}
	public Message withSender(String sender) {
		this.sender = sender;
		return this;
	}

}