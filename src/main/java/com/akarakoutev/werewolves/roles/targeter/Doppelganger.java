package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Doppelganger extends TargeterRole {

	// TESTED

	public Doppelganger() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.DOPPELGANGED;
	}

	public String wake() {
		if (abilityUsed)
			return "The " + name.name().replaceAll("_", " ") + " does nothing on this night!";

		abilityUsed = true;
		return super.wake();
	}
}
