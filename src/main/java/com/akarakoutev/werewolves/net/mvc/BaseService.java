package com.akarakoutev.werewolves.net.mvc;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.game.GameManager;
import com.akarakoutev.werewolves.net.exc.*;
import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.player.PlayerManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Service class, handling all the business logic that is executed during the pre-game phase of the lifecycle.
 * Contains the <i>activeGames</i> object, which is the mapping of all the currently played games.
 *
 * @author Alberto Karakoutev
 * @see GameManager
 */
@Service
public class BaseService {

    /**
     * A map of all the active game IDs to their respective GameManager objects. This is the main in-memory storage
     * of the server
     *
     * @see GameManager
     * @see Game
     */
    private static Map<String, GameManager> activeGames;

    public static final Logger logger = Logger.getLogger(PlayerManager.class.getName());

    @Autowired
    public BaseService() {
        activeGames = new HashMap<>();
    }


    /**
     * Log in a player. Check for same username and same IP of already logged-in users
     *
     * @param gameID   The game ID for the target game
     * @param username The username with which to log in the user
     * @param request  The HTTP request
     * @throws         GameNotFoundException If the game can not be found
     * @throws         LoginException If the game is full, the IP address is invalid or the player is already logged-in
     */
    public void login(String gameID, String username, HttpServletRequest request) throws LoginException, GameNotFoundException {

        GameManager gameManager = getGameManager(gameID);

        if(gameManager.areAllPositionsFilled())
            throw new LoginException("This game is already full!");

        String remoteAddress = "";
        if (request != null) {
            remoteAddress = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddress == null || "".equals(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }

        if(remoteAddress == null || remoteAddress.isEmpty())
            throw new LoginException("Invalid address!");

        for(Player player : gameManager.getGame().getPlayers()) {
            if((player.name != null && player.name.equals(username)) || player.address.equals(remoteAddress))
                throw new LoginException();
        }

        gameManager.createPlayer(username, remoteAddress);

        logger.info("Logged in " + username + " from " + remoteAddress + "!");

    }

    /**
     * Log out a player
     *
     * @param gameId   The game ID for the target game
     * @param username The username with which to log in the user
     * @throws         GameNotFoundException If the game can not be found
     * @throws         PlayerNotFoundException If the player does not exist
     */
    public void logout(String gameId, String username) throws GameNotFoundException, PlayerNotFoundException {
        GameManager gameManager = getGameManager(gameId);
        Player player = gameManager.getPlayer(username);
        gameManager.removePlayer(player);
        logger.info("Logged out user " + player.getName() + ", " + player.address);
    }

    /**
     * Generate a JsonArray, containing JSON objects with the player name and ready status
     *
     * @param gameId The game ID for the target game
     * @return       A player JsonArray
     * @see          JsonArray
     * @throws       GameNotFoundException If the game can not be found
     */
    public JsonArray getAllPlayers(String gameId) throws GameNotFoundException {
        List<Player> players = getGameManager(gameId).getGame().getPlayers();
        JsonArray playerList = new JsonArray();
        for (Player player : players) {
            JsonObject playerReadyJson = new JsonObject();
            playerReadyJson.addProperty("name", player.getName());
            playerReadyJson.addProperty("ready", Boolean.toString(player.readyToSleep));
            playerList.add(playerReadyJson);
        }
        return playerList;
    }

    /**
     * Generate a JsonArray, containing JSON objects with all active game IDs and their players
     *
     * @return       A game JsonArray
     * @see          JsonArray
     */
    protected JsonArray getAllGames() {
        Iterator<String> it = activeGames
                                .keySet()
                                .stream()
                                .iterator();
        JsonArray gameList = new JsonArray();
        while(it.hasNext()) {
            String next = it.next();
            JsonObject gamePlayersJson = new JsonObject();
            gamePlayersJson.addProperty("players", activeGames.get(next).getGame().toString());
            gamePlayersJson.addProperty("id", next);
            gamePlayersJson.addProperty("started", activeGames.get(next).getGame().getStarted());

            gameList.add(gamePlayersJson);

        }
        return gameList;
    }

    // Static methods

    /**
     * Delete all active games
     */
    public static void deleteAllGames() {
        activeGames = new HashMap<>();
        logger.info("Removed all games!");
    }

    /**
     * Get all the active games
     *
     * @return The active games map object
     * @see    #activeGames
     */
    public static Map<String, GameManager> getActiveGames() {
        return activeGames;
    }

    /**
     * Get a specific game manager object
     *
     * @param gameId The game ID for the target game
     * @return       A game manager object
     * @see          #activeGames
     * @see          GameManager
     */
    private static GameManager getGameManager(String gameId) throws GameNotFoundException {
        GameManager gameManager = BaseService.getActiveGames().get(gameId);
        if(gameManager == null) throw new GameNotFoundException(gameId);

        return gameManager;
    }

    /**
     * Create a new game with a given number of empty player slots
     *
     * @param players The number of players string value
     * @return        The game ID of the newly created game
     */
    protected static String createGame(String players) {
        GameManager gameManager = new GameManager(Integer.parseInt(players));
        String gameId = gameManager.getGame().getID();
        activeGames.put(gameId, gameManager);
        logger.info("Created game " + gameId);
        return gameId;
    }

    /**
     * Delete a game
     *
     * @param gameId   The game ID for the target game
     * @throws         GameNotFoundException If the game can not be found
     */
    protected static void deleteGame(String gameId) throws GameNotFoundException {
        GameManager gameManager = getGameManager(gameId);
        activeGames.remove(gameId, gameManager);
        logger.info("Removed game " + gameId);
    }

}
