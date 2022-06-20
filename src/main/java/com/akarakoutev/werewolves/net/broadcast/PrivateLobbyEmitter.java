package com.akarakoutev.werewolves.net.broadcast;

import com.akarakoutev.werewolves.net.message.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;

@Controller
public class PrivateLobbyEmitter {

	public static final Logger logger = Logger.getLogger(PrivateLobbyEmitter.class.getName());

	@MessageMapping("/lobby/subscribe/{gameId}/{username}")
	public void lobbySubscribePrivate(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("private:username", message.getSender());
		logger.info("User " + message.getSender() + " has subscribed to the private channel of the lobby " + gameId + "!");
	}
	
}
