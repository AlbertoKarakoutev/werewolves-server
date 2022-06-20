package com.akarakoutev.werewolves.net.broadcast;

import com.akarakoutev.werewolves.net.exc.ExceptionUtil;
import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.mvc.BaseService;
import com.akarakoutev.werewolves.player.PlayerManager;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;

@Controller
public class PublicLobbyEmitter {

	BaseService baseService;
	public static final Logger logger = Logger.getLogger(PlayerManager.class.getName());

	@Autowired
	private SimpMessageSendingOperations sendingOperations;

	@Autowired
	public PublicLobbyEmitter(BaseService baseService) {
		this.baseService = baseService;
	}

	@MessageMapping("/lobby/subscribe/{gameId}")
	public void lobbySubscribePublic(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {
		try {
			logger.info("User " + message.getSender() + " has subscribed to the public channel of the lobby " + gameId+ "!");
			JsonArray playerList = baseService.getAllPlayers(gameId);
			headerAccessor.getSessionAttributes().put("public:username", message.getSender());
			ServerMessage successMessage = new ServerMessage(playerList, MessageType.CONNECT);

			sendingOperations.convertAndSend("/topic/lobby/"+gameId, MessageUtil.serialize(successMessage));
		} catch (Exception e) {
			ServerMessage errorMessage = new ServerMessage(ExceptionUtil.errorResponseJson(e), MessageType.ERROR);
			sendingOperations.convertAndSend("/topic/lobby/"+gameId+"/"+message.getSender(), MessageUtil.serialize(errorMessage));
		}
	}

	@MessageMapping("/lobby/logout/{gameId}")
	public void lobbyLogout(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {
		try {
			baseService.logout(gameId, message.getSender());
			headerAccessor.getSessionAttributes().remove("public:username", message.getSender());
			headerAccessor.getSessionAttributes().remove("private:username", message.getSender());
			JsonArray playerList = baseService.getAllPlayers(gameId);
			ServerMessage successMessage = new ServerMessage(playerList, MessageType.DISCONNECT);

			sendingOperations.convertAndSend("/topic/lobby/"+gameId, MessageUtil.serialize(successMessage));
		} catch (Exception e) {
			e.printStackTrace();
			ServerMessage errorMessage = new ServerMessage(ExceptionUtil.errorResponseJson(e), MessageType.ERROR);
			sendingOperations.convertAndSend("/topic/lobby/"+gameId+"/"+message.getSender(), MessageUtil.serialize(errorMessage));
		}
	}

}
