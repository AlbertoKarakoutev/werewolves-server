package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Spellcaster extends TargeterRole {

	// TESTED

	public Spellcaster() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.SILENCED;
	}

}
