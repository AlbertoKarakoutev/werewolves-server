package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Werewolf extends TargeterRole {

	// TESTED

	public Werewolf() {
		super();
		team = Game.Team.WEREWOLVES;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.WOLVED;
	}

	public String target(String targets) {
		String message = "";

		String target1 = targets.split("_")[0];
		if(!target1.equals("")) {
			message += super.target(target1);
		}

		try {
			String target2 = targets.split("_")[1];		
			if(!target2.equals("")) {
				message += "\n" + super.target(target2);
			}
		} catch (Exception e) {}

		return message;
	}

}
