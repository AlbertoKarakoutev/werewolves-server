package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

import java.util.ArrayList;

public class Priest extends TargeterRole {

	// TESTED

	public Priest() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_EFFECT;
		applyingEffect = Effect.PRIESTED;

		question = new QA("Will the Priest use his ability?");
	}

	public String wake() {
		if (abilityUsed)
			return "The " + name.name().replaceAll("_", " ") + " does nothing on this night!";

		if (isAnswerFalse()) {
			return "The Priest does nothing on this night!";
		}
		return super.wake();
	}

	public String target(String target) {
		abilityUsed = true;
		return super.target(target);
	}

}
