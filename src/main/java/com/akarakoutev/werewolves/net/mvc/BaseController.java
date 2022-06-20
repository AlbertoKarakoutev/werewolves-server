package com.akarakoutev.werewolves.net.mvc;

import javax.servlet.http.HttpServletRequest;

import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.exc.ExceptionUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class, handling all requests that are aimed at the pre-game mechanics. All communication is executed via
 * serialized Messages. All exception handling is executed via ExceptionUtil
 *
 * @author Alberto Karakoutev
 * @see    ServerMessage
 * @see    MessageType
 * @see    MessageUtil
 * @see    ExceptionUtil
 */
@Controller
@RequestMapping("base")
public class BaseController {

	@Autowired
	BaseService baseService;

	private static final Gson serializer = new Gson();

	@Autowired
	public BaseController(BaseService baseService) {
		this.baseService = baseService;
	}

	/**
	 * Check that the server is live and ready
	 *
	 * @return An empty ServerMessage
	 */
	@GetMapping("/ping")
	public ResponseEntity<String> ping() {
		return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
	}

	/**
	 * Login a user with a given username
	 *
	 * @param content The JSON body content
	 * @param request The HTTP request object
	 * <br><b>[Serialized]</b> <i>gameId</i> - The game ID for the target game
	 * <br><b>[Serialized]</b> <i>username</i> - The username, with which to log in the user
	 * @return        An empty ServerMessage
	 */
	@PostMapping(value = "/user/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> login(@RequestBody String content, HttpServletRequest request) {
		try {
			JsonObject contentMap = MessageUtil.deserialize(content);
			baseService.login(contentMap.get("gameId").getAsString(), contentMap.get("username").getAsString(), request);

			return new ResponseEntity<>(MessageUtil.emptyMessage(), HttpStatus.OK);
		} catch (Exception e) {
			return ExceptionUtil.errorResponse(e);
		}
	}

	/**
	 * Create a new game
	 *
	 * @param content The JSON body content
	 * <br><b>[Serialized]</b> <i>players</i> - The number of players for the new game
	 * @return        A <u>DATA</u> type ServerMessage, containing the game id of the new game
	 */
	@PostMapping(value = "/game/create", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createGame(@RequestBody String content) {
		try {
			JsonObject requestContent = MessageUtil.deserialize(content);
			String gameId = BaseService.createGame(requestContent.get("players").getAsString());
			JsonObject responseContent = MessageUtil.toContent("gameId", gameId);
			String response = MessageUtil.serialize(new ServerMessage(responseContent, MessageType.DATA));

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			return ExceptionUtil.errorResponse(e);
		}
	}

	/**
	 * Delete an existing game
	 *
	 * @param gameId The ID of the game to be deleted
	 * @return       A <u>DATA</u> type ServerMessage, containing the game id of the new game
	 */
	@DeleteMapping("/game/{gameId}/delete")
	public ResponseEntity<String> deleteGame(@PathVariable String gameId) {
		try {
			BaseService.deleteGame(gameId);
			JsonObject responseContent = MessageUtil.toContent("gameId", gameId);
			String response = MessageUtil.serialize(new ServerMessage(responseContent, MessageType.DATA));

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			return ExceptionUtil.errorResponse(e);
		}
	}

	/**
	 * Get all active games
	 *
	 * @return A <u>DATA</u> type ServerMessage, containing a JSON array with all active game IDs
	 */
	@GetMapping("/game/all")
	public ResponseEntity<String> getAllGames(){
		try {
			JsonArray games = baseService.getAllGames();
			String response = MessageUtil.serialize(new ServerMessage(games, MessageType.DATA));

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			return ExceptionUtil.errorResponse(e);
		}
	}

	/**
	 * Get all players for a specific game
	 *
	 * @param gameId The game ID of the target game
	 * @return A <u>DATA</u> type ServerMessage, containing a JSON array with all active game IDs
	 */
	@GetMapping("/game/{gameId}/players")
	public ResponseEntity<String> getPlayers(@PathVariable String gameId) {
		try {
			JsonArray players = baseService.getAllPlayers(gameId);
			String response = MessageUtil.serialize(new ServerMessage(players, MessageType.DATA));

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			return ExceptionUtil.errorResponse(e);
		}
	}

}
