package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class WolfCub extends TargeterRole {

	// TESTED

	public WolfCub() {
		super();
		team = Game.Team.WEREWOLVES;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.WOLVED;
	}

}
