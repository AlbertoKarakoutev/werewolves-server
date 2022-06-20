package com.akarakoutev.werewolves.net.mvc;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.akarakoutev.werewolves.chat.Chat;
import com.akarakoutev.werewolves.game.GameManager;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.targeter.CultLeader;
import com.akarakoutev.werewolves.roles.targeter.Vampire;
import com.akarakoutev.werewolves.roles.targeter.Werewolf;
import com.akarakoutev.werewolves.roles.wakeable.WakeableRole;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;
import com.akarakoutev.werewolves.net.exc.*;
import com.akarakoutev.werewolves.net.message.MessageType;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.vote.Vote;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.akarakoutev.werewolves.player.Player;

/**
 * A Service class, handling all the business logic that is executed during the in-game phase of the lifecycle.
 *
 * @author Alberto Karakoutev
 * @see GameManager
 * @see SimpMessageSendingOperations
 */
@Service
public class GameService {

	@Autowired
	private SimpMessageSendingOperations sendingOperations;

	public static final Logger logger = Logger.getLogger(GameService.class.getName());

	@Autowired
	public GameService() {
	}


	/**
	 * Get a specific game manager object
	 *
	 * @param gameId The game ID for the target game
	 * @return       A game manager object
	 * @see          GameManager
	 */
	public static GameManager getGameManager(String gameId) throws GameNotFoundException {
		GameManager gameManager = BaseService.getActiveGames().get(gameId);
		if(gameManager == null) throw new GameNotFoundException(gameId);

		return gameManager;
	}

	/**
	 * Generate a summary of the previous night. Initialize a chat and a lynching vote. Execute the <i>applyDay()</i>
	 * method from the game manager. Broadcast the day message to everyone in the game
	 *
	 * @param gameManager The game manager of an active game
	 * <br><b>[Serialized]</b> <i>chat</i> - The current day chat
	 * <br><b>[Serialized]</b> <i>vote</i> - The initialized lynch vote
	 * <br><b>[Serialized]</b> <i>cycle</i> - The current day-night cycle
	 * <br><b>[Serialized]</b> <i>players</i> - A JsonArray of all remaining players
	 * <br><b>[Serialized]</b> <i>summary</i> - The summary object from the previous night
	 * <br><b>[Serialized]</b> <i>message</i> - A text message
	 * @see          	  GameManager#applyDay()
	 * @see          	  #sendToGame(String, String, ServerMessage)
	 * @see          	  JsonArray
	 */
	private void day(GameManager gameManager) {
		JsonObject summary = new JsonObject();
		JsonArray deadList = new JsonArray();

		String gameOver = gameManager.applyDay();
		if (gameOver != null) {
			JsonObject content = MessageUtil.toContent("message", gameOver);
			ServerMessage gameOverMessage = new ServerMessage(content, MessageType.NOTIFY);
			sendToGame(gameManager.getGame().getID(), null, gameOverMessage);
		}

		Chat chat = gameManager.createChat(Chat.Type.DAY, UUID.randomUUID());
		Vote vote = gameManager.createVote(Vote.Type.LYNCH, UUID.randomUUID());

		for (Player p : gameManager.getGame().getPlayers()) {
			if (p.isDead()) {
				deadList.add(p.getName());
				continue;
			}
			if (p.is(TargeterRole.Effect.HAGGED)) {
				summary.addProperty("hagged", p.getName());
			}
			if (p.is(TargeterRole.Effect.SILENCED)) {
				summary.addProperty("silenced", p.getName());
			}
		}

		gameManager.removeDeadPlayers();

		summary.add("dead", deadList);
		summary.addProperty("troublemaker", (gameManager.getGame().getTroublemakerNight() != null
				&& gameManager.getGame().getTroublemakerNight() == gameManager.getGame().getNights()));

		JsonObject content = new JsonObject();
		JsonArray players = new JsonArray();
		gameManager.getGame().getPlayers().forEach(player -> players.add(player.getName()));
		content.add("players", players);
		content.add("summary", summary);
		content.add("vote", vote.serialize());
		content.addProperty("cycle", chat.getCycle());
		content.addProperty("chat", chat.getId().toString());
		content.addProperty("message", "It is now day time!");

		ServerMessage dayMessage = new ServerMessage(content, MessageType.DAY);
		sendToGame(gameManager.getGame().getID(), null, dayMessage);
	}

	/**
	 * Call day if all roles have been awoken, wake a role otherwise.
	 *
	 * @param gameManager The game manager of an active game
	 * @see          	  #day(GameManager)
	 */
	private void checkDay(GameManager gameManager) {
		if (gameManager.shouldAwake()) {
			if (wakeWerewolves(gameManager))
				return;
			if (wakeVampires(gameManager))
				return;

			WakeableRole randomRole = gameManager.getRandomRole();
			if (randomRole == null)
				return;

			randomRole.state = WakeableRole.State.WAKING;

			if (checkCult(gameManager, randomRole)) {
				return;
			}

			if (randomRole.hasQuestion()) {
				if (randomRole.canQuestionBeAsked()) {
					randomRole.state = WakeableRole.State.WAKING;
					JsonObject content = MessageUtil.toContent(
							"question", randomRole.question.q,
							"active", Boolean.toString(randomRole == randomRole.owner.roleA));
					ServerMessage questionMessage = new ServerMessage(content, MessageType.QUESTION);
					sendToGame(gameManager.getGame().getID(), randomRole.owner.getName(), questionMessage);
					return;
				}
			}

			if (!(randomRole instanceof TargeterRole)) {
				wakeWakeableRole(gameManager, randomRole);
			} else {
				wakeTargeterRole(gameManager, (TargeterRole) randomRole);
			}
		} else {
			day(gameManager);
		}
	}

	/**
	 * Broadcast a message to the <b>/game</b> topic subscribers. Send the message to a private topic if the username
	 * parameter is not null.
	 *
	 * @param gameId   The game ID for the target game
	 * @param username The username of the receiver of the message
	 * @param message  The message object that is sent
	 * @see            ServerMessage
	 */
	private void sendToGame(String gameId, String username, ServerMessage message) {
		if(username == null) {
			username = "";
		} else {
			username = "/" + username;
		}
		sendingOperations.convertAndSend("/topic/game/"+gameId+username, MessageUtil.serialize(message));
	}

	/**
	 * Broadcast a message to the <b>/lobby</b> topic subscribers. Send the message to a private topic if the username
	 * parameter is not null.
	 *
	 * @param gameId   The game ID for the target game
	 * @param username The username of the receiver of the message
	 * @param message  The message object that is sent
	 * @see            ServerMessage
	 */
	public void sendToLobby(String gameId, String username, ServerMessage message) {
		if(username == null) {
			username = "";
		} else {
			username = "/" + username;
		}
		sendingOperations.convertAndSend("/topic/lobby/"+gameId+username, MessageUtil.serialize(message));
	}


	/**
	 * Broadcast a <u>WAKE_MULTIPLE</u> type message to all werewolf roles.
	 *
	 * @param gameManager The game manager of an active game
	 * <br><b>[Serialized]</b> <i>chat</i> - A serialized chat object
	 * <br><b>[Serialized]</b> <i>vote</i> - A serialized vote object
	 * <br><b>[Serialized]</b> <i>voter</i> - The player, which will execute the vote
	 * <br><b>[Serialized]</b> <i>message</i> - The message text
	 * <br><b>[Serialized]</b> <i>targetCount</i> - The number of targets to be selected
	 * @see               Werewolf
	 * @see            	  Chat
	 * @see				  Vote
	 */
	private boolean wakeWerewolves(GameManager gameManager) {
		if (gameManager.getGame().getPlayers().stream().noneMatch(Player::isWolf))
			return false;
		if (gameManager.getGame().getPlayers().stream().filter(Player::isWolf).anyMatch(player -> ((WakeableRole) player.roleA).state == WakeableRole.State.AWOKEN))
			return false;

		JsonObject content = new JsonObject();
		if (gameManager.getGame().getNights() > 1
				&& gameManager.getLastLynched() != null
				&& gameManager.getLastLynched().roleA.name.equals(Role.RoleName.WOLF_CUB)) {
			content.addProperty("targetCount", 2);
		} else {
			content.addProperty("targetCount", 1);
		}

		List<Player> wolves = gameManager.getGame().getPlayers().stream().filter(Player::isWolf).collect(Collectors.toList());

		JsonArray wolvesJson = new JsonArray();
		wolves.forEach(wolf -> wolvesJson.add(wolf.getName()));
		content.add("team", wolvesJson);

		Player firstWolf = wolves.get(0);
		content.addProperty("voter", firstWolf.getName());

		Chat wolfChat = gameManager.createChat(Chat.Type.WEREWOLVES, UUID.randomUUID());
		Vote wolfVote = gameManager.createVote(Vote.Type.WEREWOLVES, UUID.randomUUID(), firstWolf);
		content.add("chat", wolfChat.serialize());
		content.add("vote", wolfVote.serialize());

		for (Player wolf :  wolves) {
			WakeableRole wolfRole = (WakeableRole) wolf.roleA;
			wolfRole.state = WakeableRole.State.WAKING;
			if (checkCult(gameManager, wolfRole))
				continue;

			logger.info("Waking Targeter role: " +  wolfRole.name);
			String message =  wolfRole.wake();
			content.addProperty("message", message);
			ServerMessage wakeMessage = new ServerMessage(content, MessageType.WAKE_MULTIPLE);
			sendToGame(gameManager.getGame().getID(), wolfRole.owner.getName(), wakeMessage);
		}

		return true;
	}

	/**
	 * Broadcast a <u>WAKE_MULTIPLE</u> type message to all vampire roles.
	 *
	 * @param gameManager The game manager of an active game
	 * <br><b>[Serialized]</b> <i>chat</i> - A serialized chat object
	 * <br><b>[Serialized]</b> <i>vote</i> - A serialized vote object
	 * <br><b>[Serialized]</b> <i>voter</i> - The player, which will execute the vote
	 * <br><b>[Serialized]</b> <i>message</i> - The message text
	 * <br><b>[Serialized]</b> <i>targetCount</i> - The number of targets to be selected (1)
	 * @see               Vampire
	 * @see            	  Chat
	 * @see				  Vote
	 */
	private boolean wakeVampires(GameManager gameManager) {
		if (gameManager.getGame().getPlayers().stream().noneMatch(Player::isVamp))
			return false;
		if (gameManager.getGame().getPlayers().stream().filter(Player::isVamp).anyMatch(player -> ((WakeableRole) player.roleA).state == WakeableRole.State.AWOKEN))
			return false;

		JsonObject content = new JsonObject();
		content.addProperty("targetCount", 1);

		List<Player> vamps = gameManager.getGame().getPlayers().stream().filter(Player::isVamp).collect(Collectors.toList());

		JsonArray vampsJson = new JsonArray();
		vamps.forEach(vamp -> vampsJson.add(vamp.getName()));
		content.add("team", vampsJson);

		Player firstVamp = vamps.get(0);
		content.addProperty("voter", firstVamp.getName());

		Chat vampChat = gameManager.createChat(Chat.Type.VAMPIRES, UUID.randomUUID());
		Vote vampVote = gameManager.createVote(Vote.Type.VAMPIRES, UUID.randomUUID(), firstVamp);

		content.add("chat", vampChat.serialize());
		content.add("vote", vampVote.serialize());

		for (Player vamp : vamps) {
			WakeableRole vampRole = (WakeableRole) vamp.roleA;
			vampRole.state = WakeableRole.State.WAKING;
			if (checkCult(gameManager, vampRole))
				continue;

			logger.info("Waking Targeter role: " +  vampRole.name);
			String message =  vampRole.wake();
			content.addProperty("message", message);
			ServerMessage wakeMessage = new ServerMessage(content, MessageType.WAKE_MULTIPLE);
			sendToGame(gameManager.getGame().getID(), vampRole.owner.getName(), wakeMessage);
		}

		return true;
	}

	/**
	 * Broadcast a <u>WAKE</u> type message to a specific targeter role.
	 *
	 * @param gameManager The game manager of an active game
	 * @param role 		  The targeter role to be awoken
	 * <br><b>[Serialized]</b> <i>list</i> - A JsonArray of all active players in the game players
	 * <br><b>[Serialized]</b> <i>role</i> - The awoken role type
	 * <br><b>[Serialized]</b> <i>message</i> - The message text
	 * <br><b>[Serialized]</b> <i>activeRole</i> - Whether the awoken role is active
	 * <br><b>[Serialized]</b> <i>targetCount</i> - The number of targets to be selected
	 * @see            	  TargeterRole
	 * @see            	  JsonArray
	 */
	private void wakeTargeterRole(GameManager gameManager, TargeterRole role) {
		// Check if the role has used up its ability
		if (role.ability2Used == null) {
			if (role.abilityUsed) {
				wakeWakeableRole(gameManager, role);
				return;
			}
		} else {
			if (role.abilityUsed && role.ability2Used) {
				wakeWakeableRole(gameManager, role);
				return;
			}
		}

		logger.info("Waking Targeter role: " + role.name);
		String message = role.wake();
		JsonObject content = new JsonObject();
		JsonArray list = new JsonArray();
		gameManager
				.getGame()
				.getPlayers()
				.stream()
				//.filter(player -> player.getName().equals(role.owner.getName()))
				.forEach(player -> list.add(player.getName()));
		content.add("list", list);
		content.addProperty("message", message);
		content.addProperty("role", "targeter");
		content.addProperty("targetCount", role.getTargetCount());
		content.addProperty("awokenRole", role.name.name());
		content.addProperty("canCancelTurn", role.canCancelTurn);
		ServerMessage wakeMessage = new ServerMessage(content, MessageType.WAKE);
		sendToGame(gameManager.getGame().getID(), role.owner.getName(), wakeMessage);
	}

	/**
	 * Broadcast a <u>WAKE</u> type message to a specific wakeable role.
	 *
	 * @param gameManager The game manager of an active game
	 * @param role 		  The wakeable role to be awoken
	 * <br><b>[Serialized]</b> <i>role</i> - The awoken role type
	 * <br><b>[Serialized]</b> <i>message</i> - The message text
	 * @see            	  WakeableRole
	 * @see            	  MessageUtil
	 */
	private void wakeWakeableRole(GameManager gameManager, WakeableRole role) {
		logger.info("Waking Wakeable role: " + role.name);
		Player player = role.owner;
		boolean activeAwoken = role == player.roleA;
		String message = role.wake();
		JsonObject content = MessageUtil.toContent(
				"message", message,
				"role", "wakeable",
				"awokenRole", (activeAwoken) ? player.roleA.name.name() : player.roleP.name.name());
		ServerMessage wakeMessage = new ServerMessage(content, MessageType.WAKE);
		sendToGame(gameManager.getGame().getID(), role.owner.getName(), wakeMessage);
	}

	/**
	 * Check if a role is part of the cult. If it is, notify the player.
	 *
	 * @param gameManager The game manager of an active game
	 * @param role 		  The wakeable role to be awoken
	 * @see          CultLeader
	 */
	private boolean checkCult(GameManager gameManager, WakeableRole role) {
		if (role.owner.is(TargeterRole.Effect.CULT)) {
			if (role == role.owner.roleA) {
				String message = "You are part of the cult and can not use your roles!";
				JsonObject content = MessageUtil.toContent("message", message, "awokenRole", role.name.name());
				ServerMessage cultMessage = new ServerMessage(content, MessageType.WAKE);
				sendToGame(gameManager.getGame().getID(), role.owner.getName(), cultMessage);
				return true;
			}
		}

		return false;
	}

	/**
	 * Cast a vote for a specific vote instance. If a lynch vote is complete, calculate the results and broadcast them using
	 * a <U>LYNCH</U> type message
	 *
	 * @param gameId   The game ID for the target game
	 * @param idStr    The string ID of the specific vote
	 * @param voterStr The name of the player, casting the vote
	 * @param voteeStr The name of the player, who is being voted for
	 * <br><b>[Serialized]</b> <i>id</i> - The string ID of the specific vote
	 * <br><b>[Serialized]</b> <i>lynched</i> - The player who is lynched on that day
	 * @throws 		   PlayerNotFoundException If the voter or votee do not exist
	 * @throws 		   GameNotFoundException   If the game does not exist
	 * @see            Vote
	 */
	protected void vote(String gameId, String idStr, String voterStr, String voteeStr) throws PlayerNotFoundException, GameNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		Player voter = gameManager.getPlayer(voterStr);
		UUID id = UUID.fromString(idStr);

		Vote vote = gameManager.getGame().getVotes().get(id);

		if (vote.getType() == Vote.Type.LYNCH) {
			Player votee0 = gameManager.getPlayer(voteeStr.split("_")[0]);
			if (voteeStr.contains("_")) {
				Player votee1 = gameManager.getPlayer(voteeStr.split("_")[1]);
				vote.setVote(voter, List.of(votee0, votee1));
			} else {
				vote.setVote(voter, List.of(votee0));
			}
			if (vote.isComplete()) {
				Player mostVoted = vote.getMostVoted();
				if (mostVoted.roleP.name == Role.RoleName.PRINCE) {
					JsonObject content = MessageUtil.toContent("message", mostVoted.getName() + " is the Prince and can not be lynched!");
					ServerMessage princeMessage = new ServerMessage(content, MessageType.NOTIFY);
					sendToGame(gameId, null, princeMessage);
					return;
				}
				Player vampedVotee = vote.getVampedVotee();
				if (vampedVotee != null) {
					JsonObject content = MessageUtil.toContent("lynched", vampedVotee.getName());
					ServerMessage vampedLynchMessage = new ServerMessage(content, MessageType.LYNCH);
					sendToGame(gameId, null, vampedLynchMessage);
					gameManager.removePlayer(vampedVotee);
				}

				gameManager.addLynchedPlayer(mostVoted);
				JsonObject content = MessageUtil.toContent("lynched", mostVoted.getName());
				ServerMessage lynchMessage = new ServerMessage(content, MessageType.LYNCH);
				sendToGame(gameId, null, lynchMessage);
				gameManager.removePlayer(mostVoted);
			}
		} else if (vote.getType() == Vote.Type.WEREWOLVES){
			ServerMessage message = new ServerMessage(MessageType.HIDE_WAKE_MULTIPLE);
			for (Player wolf : gameManager.getGame().getPlayers().stream().filter(Player::isWolf).collect(Collectors.toList())) {
				sendToGame(gameId, wolf.getName(), message);
			}
			target(gameId, voter.roleA.name.name(), voteeStr);
		} else if (vote.getType() == Vote.Type.VAMPIRES){
			ServerMessage message = new ServerMessage(MessageType.HIDE_WAKE_MULTIPLE);
			for (Player vamp : gameManager.getGame().getPlayers().stream().filter(Player::isVamp).collect(Collectors.toList())) {
				sendToGame(gameId, vamp.getName(), message);
			}
			target(gameId, Role.RoleName.VAMPIRE.name(), voteeStr);
		}

	}

	/**
	 * Trigger a day check
	 *
	 * @param gameId The game ID for the target game
	 * @throws 		 GameNotFoundException   If the game does not exist
	 * @see          #checkDay(GameManager)
	 */
	protected void wokenUp(String gameId, String awokenRole) throws GameNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		Role role = gameManager.getRole(Role.RoleName.from(awokenRole));
		((WakeableRole) role).state = WakeableRole.State.AWOKEN;
		if (!gameManager.getGame().getDay()) {
			checkDay(gameManager);
		}
	}

	/**
	 * Set nighttime. Broadcast a <u>NIGHT</u> type message to everyone. Call the <i>applyNight()</i> method from the
	 * game manager. Broadcast the roles to each player in case of any changes with a <u>ROLES</u> type message. Begin
	 * night actions.
	 *
	 * @param gameId The game ID for the target game
	 * <br><b>[Serialized]</b> <i>id</i> - The string ID of the specific vote
	 * <br><b>[Serialized]</b> <i>lynched</i> - The player who is lynched on that day
	 * @throws 		 PlayerNotFoundException If a player does not exist
	 * @throws 		 GameNotFoundException   If the game does not exist
	 * @throws 		 IOException   		     If a resource does not exist
	 * @see          GameManager#applyNight()
	 * @see          #getRoles(String, String)
	 */
	protected void applyNight(String gameId) throws GameNotFoundException, PlayerNotFoundException, IOException {
		GameManager gameManager = getGameManager(gameId);

		sendToGame(gameId,
				null,
				new ServerMessage(MessageUtil.toContent("message", "It is now night time!"), MessageType.NIGHT));

		gameManager.applyNight();

		for (Player player : gameManager.getGame().getPlayers()) {
			ServerMessage rolesMessage = new ServerMessage(getRoles(gameId, player.getName()), MessageType.ROLES);
			sendToGame(gameId, player.getName(), rolesMessage);
		}

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}

		checkDay(gameManager);
	}

	/**
	 * Set a player's roles to random two roles
	 *
	 * @param gameId The game ID for the target game
	 * @throws 		 GameNotFoundException   If the game does not exist
	 */
	protected void setPlayerRoles(String gameId) throws GameNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		gameManager.setPlayerRoles();
		gameManager.getGame().setStarted(true);
	}

	/**
	 * Add a chat message to a specific chat. Broadcast the updated chat with a <u>CHAT</u> type message.
	 *
	 * @param gameId    The game ID for the target game
	 * @param chatIdStr The ID of the specific chat
	 * @param message   The message
	 * @param senderStr The name of the sender
	 * @throws 		    PlayerNotFoundException If a player does not exist
	 * @throws 		    GameNotFoundException   If the game does not exist
	 * @see             GameManager#addChatMessage(UUID, LocalTime, Player, String)
	 * @see             Chat
	 */
	protected void addChatMessage(String gameId, String chatIdStr, String message, String senderStr) throws GameNotFoundException, PlayerNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		LocalTime timestamp = LocalTime.now();
		Player sender = gameManager.getPlayer(senderStr);
		UUID chatId = UUID.fromString(chatIdStr);
		gameManager.addChatMessage(chatId, timestamp, sender, message);

		ServerMessage chatUpdateMessage = new ServerMessage(gameManager.getGame().getChats().get(chatId).serialize(), MessageType.CHAT);
		sendToGame(gameId, null, chatUpdateMessage);
	}

	/**
	 * Set a player as 'ready to sleep'. If everyone is ready, broadcast a <u>NIGHT</u> type message.
	 *
	 * @param gameId   The game ID for the target game
	 * @param username The name of the player that is ready
	 * @throws 		   PlayerNotFoundException If a player does not exist
	 * @throws 		   GameNotFoundException   If the game does not exist
	 * @throws 		   IOException 		    If a resource does not exist
	 * @see            #applyNight(String)
	 */
	protected void setReadyToSleep(String gameId, String username) throws GameNotFoundException, PlayerNotFoundException, IOException {
		GameManager gameManager = getGameManager(gameId);

		Player player = gameManager.getPlayer(username);
		gameManager.setReadyToSleep(player);

		if(gameManager.areAllReadyToSleep()) {
			if (gameManager.getGame().getNights() == 0) {
				ServerMessage gameBeginMessage = new ServerMessage(MessageUtil.toContent("message", "It is now night time!"), MessageType.GAME_BEGIN);
				logger.info("Starting game " + gameId + " ...");
				sendToLobby(gameId, null, gameBeginMessage);
			} else {
				logger.info("Setting night (Night " + gameManager.getGame().getNights() + ")");
				applyNight(gameId);
			}
		}
	}

	/**
	 * Add a player to an in-game room
	 *
	 * @param gameId   The game ID for the target game
	 * @param username The name of the player that is added
	 * @throws 		   PlayerNotFoundException If a player does not exist
	 * @throws 		   GameNotFoundException   If the game does not exist
	 * @see            GameManager#addInGamePlayer(Player)
	 */
	protected void addPlayerToGameRoom(String gameId, String username) throws GameNotFoundException, PlayerNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		gameManager.addInGamePlayer(gameManager.getPlayer(username));
		logger.info("Player " + username + " has entered the game room " + gameId);
	}

	/**
	 * Wake a specific role with a specific answer to a pre-wake question
	 *
	 * @param gameId   The game ID for the target game
	 * @param roleName The name of the awoken role
	 * @param username The name of the awoken player
	 * @param answer   The answer to the yes/no question
	 * @throws 		   PlayerNotFoundException If a player does not exist
	 * @throws 		   GameNotFoundException   If the game does not exist
	 * @see            WakeableRole.QA
	 */
	protected void wakeRoleWithAnswer(String gameId, String roleName, String username, boolean answer) throws GameNotFoundException, PlayerNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		Player player = gameManager.getPlayer(username);

		WakeableRole role = (player.roleA.name == Role.RoleName.from(roleName))
				? (WakeableRole) player.roleA
				: (WakeableRole) player.roleP;

		role.question.a = answer;

		if (!(role instanceof TargeterRole) || !answer) {
			wakeWakeableRole(gameManager, role);
			return;
		}

		wakeTargeterRole(gameManager, (TargeterRole) role);
	}

	/**
	 * Check if a game is ready to start
	 *
	 * @param gameId The game ID for the target game
	 * @throws 		 GameNotFoundException   If the game does not exist
	 * @see          GameManager#isGameReadyToStart()
	 */
	public boolean gameIsReady(String gameId) throws GameNotFoundException {
		logger.info("Game " + gameId + " is ready to start");
		return getGameManager(gameId).isGameReadyToStart();
	}


	/**
	 * Get the player's roles. Serialize them and put them inside a JsonObject.
	 *
	 * @param gameId   The game ID for the target game
	 * @param username The name of the player with the roles
	 * <br><b>[Serialized]</b> <i>active</i> - The active role
	 * <br><b>[Serialized]</b> <i>passive</i> - The passive role
	 * @return 		   A JsonObject containing the roles
	 * @throws 		   PlayerNotFoundException If a player does not exist
	 * @throws 		   GameNotFoundException   If the game does not exist
	 * @throws 		   IOException 		       If a resource does not exist
	 * @see            #applyNight(String)
	 * @see            GameManager#serializeRole(Role)
	 * @see            Role
	 */
	private JsonObject getRoles(String gameId, String username) throws GameNotFoundException, IOException, PlayerNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		Player player = gameManager.getPlayer(username);

		return MessageUtil.toContent(
				"active", gameManager.serializeRole(player.roleA), "passive", gameManager.serializeRole(player.roleP));

	}

	/**
	 * Target a player by a targeter role
	 *
	 * @param gameId     The game ID for the target game
	 * @param roleName   The name of the targeter role
	 * @param targetName The name of the player who is targeter
	 * <br><b>[Serialized]</b> <i>message</i> - The message to the targeter
	 * @throws 		   GameNotFoundException If the game does not exist
	 * @see            #checkDay(GameManager)
	 * @see            GameManager#getRole(com.akarakoutev.werewolves.roles.generic.Role.RoleName)
	 * @see            TargeterRole
	 */
	protected void target(String gameId, String roleName, String targetName) throws GameNotFoundException {
		GameManager gameManager = getGameManager(gameId);
		TargeterRole role = (TargeterRole) gameManager.getRole(Role.RoleName.from(roleName));
		gameManager.log(role.owner.getName() + "(" + roleName + ") has targeted " + targetName);
		String targetResponse = role.target(targetName);
		ServerMessage targetMessage = new ServerMessage(MessageUtil.toContent("message", targetResponse), MessageType.NOTIFY);
		sendToGame(gameId, role.owner.getName(), targetMessage);
		role.state = WakeableRole.State.AWOKEN;
		if (role.name == Role.RoleName.WEREWOLF || role.name == Role.RoleName.LONE_WOLF || role.name == Role.RoleName.WOLF_CUB) {
			gameManager.getGame().getPlayers().stream()
			.filter(Player::isWolf)
			.forEach(player -> ((WakeableRole) player.roleA).state = WakeableRole.State.AWOKEN);
		} else if (role.name == Role.RoleName.VAMPIRE) {
			gameManager.getGame().getPlayers().stream()
			.filter(Player::isVamp)
			.forEach(player -> ((WakeableRole) player.roleA).state = WakeableRole.State.AWOKEN);
		} else if (role.name == Role.RoleName.REVEALER) {
			if (!role.owner.isDead()) {
				role.state = WakeableRole.State.UNAWOKEN;
				role.abilityUsed = false;
			}
		}
		if (!gameManager.getGame().getDay()) {
			checkDay(gameManager);
		}
	}


}
