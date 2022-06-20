package com.akarakoutev.werewolves.net.mvc;

import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.exc.ExceptionUtil;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class, handling all requests that are aimed at in-game mechanics. All communication is executed via
 * serialized Messages. All exception handling is executed via ExceptionUtil
 *
 * @author Alberto Karakoutev
 * @see    ServerMessage
 * @see    MessageType
 * @see    MessageUtil
 * @see    ExceptionUtil
 */
@Controller
@RequestMapping("game")
public class GameController {

    GameService gameService;
    BaseService baseService;

    @Autowired
    public GameController(GameService gameService, BaseService baseService) {
        this.gameService = gameService;
        this.baseService = baseService;
    }

    /**
     * Notify the server that a player is logged in
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>username</i> - The username of the logged-in user
     * @return        An empty ServerMessage
     */
    @PostMapping(value = "/user/loggedIn", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loggedIn(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            String gameId = requestContent.get("gameId").getAsString();
            gameService.addPlayerToGameRoom(gameId, requestContent.get("username").getAsString());

            if (gameService.gameIsReady(gameId)) {
                gameService.setPlayerRoles(gameId);
                gameService.applyNight(gameId);
            }

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Set the target for a role's move
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>roleName</i> - The name of the role, which is doing the targeting
     * <br><b>[Serialized]</b> <i>targetName</i> - The name of the player, which is being targeted
     * @return        A <u>NOTIFY</u> type ServerMessage
     */
    @PostMapping(value = "/target", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> target(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            gameService.target(
                    requestContent.get("gameId").getAsString(),
                    requestContent.get("roleName").getAsString(),
                    requestContent.get("targetName").getAsString());

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Mark a player as "ready to sleep"
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>username</i> - The username of the sleeping player
     * @return        An empty ServerMessage
     */
    @PutMapping(value = "/sleep", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sleep(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            String gameId = requestContent.get("gameId").getAsString();
            String username = requestContent.get("username").getAsString();
            gameService.setReadyToSleep(gameId, username);

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Answer a question, regarding the players actions
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>roleName</i> - The role, for which the question is being answered
     * <br><b>[Serialized]</b> <i>username</i> - The username, with which to log in the user
     * <br><b>[Serialized]</b> <i>answer</i> - The answer of the question, yes or no
     * @return        An empty ServerMessage
     */
    @PostMapping(value = "/setAnswer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> answer(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            gameService.wakeRoleWithAnswer(
                    requestContent.get("gameId").getAsString(),
                    requestContent.get("roleName").getAsString(),
                    requestContent.get("username").getAsString(),
                    requestContent.get("answer").getAsBoolean());

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Send a 'woken up' message
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>awokenRole</i> - The awoken role
     * @return        An empty ServerMessage
     */
    @PostMapping(value = "/wokenUp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> wokenUp(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            gameService.wokenUp(requestContent.get("gameId").getAsString(), requestContent.get("role").getAsString());

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Send a chat message
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>chatId</i> - The chat, which the message is sent to
     * <br><b>[Serialized]</b> <i>message</i> - The message text
     * <br><b>[Serialized]</b> <i>username</i> - The user which sent the message
     * @return        An empty ServerMessage
     */
    @PutMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addChatMessage(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            String gameId = requestContent.get("gameId").getAsString();
            gameService.addChatMessage(
                    gameId,
                    requestContent.get("chatId").getAsString(),
                    requestContent.get("message").getAsString(),
                    requestContent.get("username").getAsString());

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

    /**
     * Send a vote
     *
     * @param content The JSON body content
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
     * <br><b>[Serialized]</b> <i>voteId</i> - The vote, which the voting is in
     * <br><b>[Serialized]</b> <i>voter</i> - The player, who is supplying the vote
     * <br><b>[Serialized]</b> <i>votee</i> - The player, who is being voted for
     * @return        An empty ServerMessage
     */
    @PutMapping(value = "/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> vote(@RequestBody String content) {
        try {
            JsonObject requestContent = MessageUtil.deserialize(content);
            String gameId = requestContent.get("gameId").getAsString();
            gameService.vote(
                    gameId,
                    requestContent.get("voteId").getAsString(),
                    requestContent.get("voter").getAsString(),
                    requestContent.get("votee").getAsString());

            return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return ExceptionUtil.errorResponse(e);
        }
    }

}
