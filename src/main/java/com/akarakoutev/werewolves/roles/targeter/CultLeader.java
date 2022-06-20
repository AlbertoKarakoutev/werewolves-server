package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class CultLeader extends TargeterRole {

	// TESTED

	public CultLeader() {
		super();
		team = Game.Team.CULT;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.CULT;
	}
}
