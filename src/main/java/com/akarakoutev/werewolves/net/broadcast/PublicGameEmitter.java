package com.akarakoutev.werewolves.net.broadcast;

import com.akarakoutev.werewolves.net.exc.ExceptionUtil;
import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.mvc.BaseService;
import com.akarakoutev.werewolves.player.PlayerManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.akarakoutev.werewolves.net.mvc.GameService;

import java.util.logging.Logger;

@Controller
public class PublicGameEmitter {

    @Autowired
    BaseService baseService;

    public static final Logger logger = Logger.getLogger(PublicGameEmitter.class.getName());

    @Autowired
    private SimpMessageSendingOperations sendingOperations;

    @MessageMapping("/game/subscribe/{gameId}")
    public void gameSubscribePublic(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {

        logger.info("User " + message.getSender() + " has subscribed to the public channel of the game " + gameId+ "!");
        headerAccessor.getSessionAttributes().put("public:username", message.getSender());

    }

    @MessageMapping("/game/users/logout/{gameId}")
    public void gameLogout(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            baseService.logout(gameId, message.getSender());
            headerAccessor.getSessionAttributes().remove("public:username", message.getSender());
            headerAccessor.getSessionAttributes().remove("private:username", message.getSender());
            JsonObject content = MessageUtil.toContent("player", message.getSender());
            ServerMessage dcMessage = new ServerMessage(content, MessageType.DISCONNECT);

            sendingOperations.convertAndSend("/topic/game/"+gameId, MessageUtil.serialize(dcMessage));
        } catch (Exception e) {
            ServerMessage errorMessage = new ServerMessage(ExceptionUtil.errorResponseJson(e), MessageType.ERROR);
            sendingOperations.convertAndSend("/topic/game/"+gameId+"/"+message.getSender(), MessageUtil.serialize(errorMessage));
        }
    }


}
