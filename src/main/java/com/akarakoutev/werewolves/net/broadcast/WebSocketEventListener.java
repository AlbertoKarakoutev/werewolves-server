package com.akarakoutev.werewolves.net.broadcast;

import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

	//private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEventListener.class);

	@Autowired
	private SimpMessageSendingOperations sendingOperations;

	@EventListener
	public void handleWebsocketConnectListener(final SessionConnectedEvent event) {
		//LOGGER.info(event.toString());
	}

	@EventListener
	public void handleWebsocketDisconnectListener(final SessionDisconnectEvent event) {
		final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

		final String username = (String) headerAccessor.getSessionAttributes().get("username");

		Message message = new Message().withType(MessageType.DISCONNECT).withSender(username);

		sendingOperations.convertAndSend("/topic/public", message);
	}

}
