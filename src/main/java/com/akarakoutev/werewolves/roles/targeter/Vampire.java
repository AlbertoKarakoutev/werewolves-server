package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Vampire extends TargeterRole {

	// TESTED

	public Vampire() {
		super();
		team = Game.Team.VAMPIRES;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.VAMPIRED;
	}

}
