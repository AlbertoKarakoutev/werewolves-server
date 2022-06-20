package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.game.GameManager;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.net.exc.GameNotFoundException;
import com.akarakoutev.werewolves.net.mvc.GameService;

/**
 * A Role object, which allows for waking functionality, as well as question answering
 *
 * @author Alberto Karakoutev
 * @see Role
 * @see QA
 */
public class WakeableRole extends Role {

	public Game game;
	public boolean abilityUsed = false;
	public Boolean ability2Used = null;
	public State state;

	public enum State {
		UNAWOKEN,
		WAKING,
		AWOKEN
	}

	public QA question;
	public static class QA {
		public String q;
		public Boolean a;

		public QA(String q) {
			this.q = q;
		}
	}

	public WakeableRole() {
		super();
	}

	public String wake() {
		return "";
	}

	/**
	 * Add a game ID to the role. Set the game based on the ID.
	 *
	 * @param gameId The id of the target game
	 * @return     	 The wakeable role
	 * @see          GameManager
	 */
	public WakeableRole withGameId(String gameId) {
		this.gameId = gameId;
		try {
			game = GameService.getGameManager(gameId).getGame();
		} catch (GameNotFoundException e) {
			e.printStackTrace();
		}
		return this;
	}

	public boolean hasQuestion() {
		return question != null;
	}
	public void resetQuestion() {
		question.a = null;
	}
	public boolean canQuestionBeAsked() {
		return !abilityUsed && question.a == null;
	}
	public boolean isAnswerFalse() {
		return question.a != null && !question.a;
	}

}
