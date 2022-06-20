package com.akarakoutev.werewolves.net.mvc;

import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.exc.ExceptionUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller class, handling all requests that are aimed at the lobby mechanics. All communication is executed via
 * serialized Messages. All exception handling is executed via ExceptionUtil
 *
 * @author Alberto Karakoutev
 * @see    ServerMessage
 * @see    MessageType
 * @see    MessageUtil
 * @see    ExceptionUtil
 */
@Controller
@RequestMapping("lobby")
public class LobbyController {

    BaseService baseService;
    GameService gameService;

    @Autowired
    private SimpMessageSendingOperations sendingOperations;

    @Autowired
    public LobbyController(BaseService baseService, GameService gameService) {
        this.baseService = baseService;
        this.gameService = gameService;
    }

    /**
     * Set a user as ready to start the game. Broadcast a map of all players with their ready values to the lobby
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>username</i> - The user, which is ready
     * @return        An empty ServerMessage
     */
    @PutMapping(value = "/user/ready", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ready(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            String gameId = requestContent.get("gameId").getAsString();
            String username = requestContent.get("username").getAsString();
            gameService.setReadyToSleep(gameId, username);
            JsonArray players = baseService.getAllPlayers(gameId);
            ServerMessage readyMessage = new ServerMessage(players, MessageType.READY);
            gameService.sendToLobby(gameId, null, readyMessage);

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

}
