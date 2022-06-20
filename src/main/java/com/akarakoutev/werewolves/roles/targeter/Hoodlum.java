package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Hoodlum extends TargeterRole {

	public Hoodlum() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.HOODLUMED;
	}

	public String target(String targets) {
		String target1 = targets.split("_")[0];
		String target2 = targets.split("_")[1];

		String response1 = super.target(target1);
		String response2 = super.target(target2);

		abilityUsed = true;
		return response1 + "\n" + response2;
	}

	public int getTargetCount() {
		return 2;
	}

}
