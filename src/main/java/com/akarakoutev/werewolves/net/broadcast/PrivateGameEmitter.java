package com.akarakoutev.werewolves.net.broadcast;

import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.mvc.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;

@Controller
public class PrivateGameEmitter {

    public static final Logger logger = Logger.getLogger(PrivateGameEmitter.class.getName());

    @Autowired
    public PrivateGameEmitter() {}

    @MessageMapping("/game/subscribe/{gameId}/{username}")
    public void gameSubscribePrivate(@Payload final Message message, @DestinationVariable String gameId, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("private:username", message.getSender());
        logger.info("User " + message.getSender() + " has subscribed to the private channel of the game " + gameId + "!");
    }

}
