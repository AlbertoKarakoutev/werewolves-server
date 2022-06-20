package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

import java.util.ArrayList;

public class Huntress extends TargeterRole {

	// TESTED

	public Huntress() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.HUNTRESSED;

		question = new QA("Will the Huntress use her shot?");
	}

	public String wake() {
		if (abilityUsed)
			return "The " + name.name().replaceAll("_", " ") + " does nothing on this night!";

		if (isAnswerFalse()) {
			return "The Huntress does nothing on this night!";
		}
		return super.wake();
	}

	public String target(String target) {
		abilityUsed = true;
		return super.target(target);
	}

}
