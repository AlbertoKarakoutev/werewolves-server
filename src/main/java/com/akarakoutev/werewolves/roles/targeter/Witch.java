package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;

public class Witch extends TargeterRole {

	// TESTED

	// TODO ability2Used
	public Witch() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.WITCH_H;
		canCancelTurn = true;
		ability2Used = false;
	}

	public String wake() {
		if (abilityUsed && ability2Used)
			return "The Witch does nothing on this night!";

		return super.wake();
	}

	public String target(String targets) {
		String message = "";

		String target1 = targets.split("_")[0];
		if(!target1.equals("")) {
			message += targetWitchSpecial(target1, Effect.WITCH_H);
			abilityUsed = true;
		}

		try {
			String target2 = targets.split("_")[1];		
			if(!target2.equals("")) {
				message += "\n" + targetWitchSpecial(target2, Effect.WITCH_K);
				ability2Used = true;
			}
		} catch (Exception e) {}

		return message;
	}

	String targetWitchSpecial(String targetName, Effect effect) {
		for (int i = 0; i < gameManager.getGame().getPlayers().size(); i++) {
			if (gameManager.getPlayer(i).name.equals(targetName)) {
				Player target = gameManager.getPlayer(i);
				target.effects.add(effect);
				return "Successfully added effect to " + targetName + "!";
			}
		}
		throw new IllegalCallerException();
	}

	public int getTargetCount() {
		return 2;
	}

}
