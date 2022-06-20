package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.GameManager;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.wakeable.WakeableRole;
import com.akarakoutev.werewolves.net.exc.GameNotFoundException;
import com.akarakoutev.werewolves.net.mvc.GameService;

/**
 * A Role object, which allows for targeting functionality with all target types
 *
 * @author Alberto Karakoutev
 * @see TargetType
 * @see RoleCheckType
 * @see Effect
 */
public class TargeterRole extends WakeableRole {

	TargetType targetType;
	RoleCheckType roleCheckType;
	Effect applyingEffect;
	public boolean canCancelTurn = false;
	protected GameManager gameManager;

	enum TargetType {
		TARGET_AND_EFFECT,
		TARGET_AND_ROLE_CHECK
	}

	enum RoleCheckType {
		MYSTIC_SEER,
		AURA_SEER,
		SEER,
		SORCERESS,
		REVEALER
	}

	public enum Effect {
		HEALED,
		WOLVED,
		VAMPIRED,
		HUNTRESSED,
		HAGGED,
		SILENCED,
		WITCH_H,
		WITCH_K,
		PRIESTED,
		DOPPELGANGED,
		CULT,
		CUPIDED,
		HOODLUMED
	}

	public TargeterRole() {
		super();
	}

	public String wake() {
		return "Please select your target(s):";
	}

	public TargeterRole withGameId(String gameId) {
		this.gameId = gameId;
		try {
			gameManager = GameService.getGameManager(gameId);
			game = gameManager.getGame();
		} catch (GameNotFoundException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Apply effect to a target player
	 *
	 * @param target The target player
	 * @see   Effect
	 */
	private void targetAndEffect(Player target) {
		if (applyingEffect == null)
			throw new IllegalCallerException();
		target.effects.add(applyingEffect);
	}

	/**
	 * Target a player and perform a check based on the targeter role's check type
	 *
	 * @param target The target player
	 * @return     	 The check response
	 * @see          RoleCheckType
	 */
	private String targetAndRoleCheck(Player target) {

		if (roleCheckType == null)
			throw new IllegalCallerException();

		Role targetRole = target.roleA;

		String message = "";

		switch(roleCheckType) {
			case MYSTIC_SEER:
				message = "Your target is a " + targetRole.name.name().replaceAll("_", " ") + ".";
				break;
			case AURA_SEER:
				if (targetRole.name == RoleName.VAMPIRE || targetRole.name == RoleName.CULT_LEADER) {
					message = "Your target is a Vampire or the Cult Leader.";
				} else {
					message = "Your target is NOT a Vampire or the Cult Leader.";
				}
				break;
			case SEER:
				if (targetRole.name == RoleName.WEREWOLF || targetRole.name == RoleName.LYCAN) {
					message = "Your target IS a Werewolf.";
				} else {
					message = "Your target is NOT a Werewolf.";
				}
				break;
			case SORCERESS:
				if (targetRole.name == RoleName.SEER) {
					message = "Your target is the Seer.";
				} else {
					message = "Your target is NOT the Seer.";
				}
				break;
			case REVEALER:
				try {
					TargeterRole wolfRole = (TargeterRole) targetRole;
					if (wolfRole.owner.isWolf()) {
						message = "Your target was a Werewolf.";
						if (!wolfRole.owner.isDead()) {
							wolfRole.owner.kill();
						}
					} else {
						owner.kill();
						message = "Your target was NOT a werewolf.";
					}
				} catch (Exception e) {
					owner.kill();
					message = "Your target was NOT a werewolf.";
				}
				break;
			default:
				message = "Something went wrong...";
				break;
		}

		return message;
	}

	/**
	 * Target a player based on the targeting type of the targeter role
	 *
	 * @param targetName The name of the target player
	 * @return     	 	 The check response
	 * @see          	 TargetType
	 */
	public String target(String targetName) {
		
		Player target = null;
		try {
			target = gameManager.getPlayer(targetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		switch (targetType) {
			case TARGET_AND_EFFECT:
				targetAndEffect(target);
				return "Successfully added effect to " + target.getName() + "!";
			case TARGET_AND_ROLE_CHECK:
				return targetAndRoleCheck(target);
			default:
				return "Something went wrong...";
		}
	}

	public int getTargetCount() {
		return 1;
	}
}
