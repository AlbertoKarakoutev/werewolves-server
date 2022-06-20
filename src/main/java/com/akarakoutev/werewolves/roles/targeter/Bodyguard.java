package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Bodyguard extends TargeterRole {

	// TESTED

	public Bodyguard() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.HEALED;
	}
}
