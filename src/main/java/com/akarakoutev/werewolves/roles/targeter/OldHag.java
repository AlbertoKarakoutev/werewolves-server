package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class OldHag extends TargeterRole {

	// TESTED

	public OldHag() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.HAGGED;
	}
}
