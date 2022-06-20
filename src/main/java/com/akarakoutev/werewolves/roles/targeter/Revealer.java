package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

import java.util.ArrayList;

public class Revealer extends TargeterRole {

	// TESTED

	public Revealer() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_ROLE_CHECK;
		roleCheckType = RoleCheckType.REVEALER;
		canCancelTurn = true;

		question = new QA("Will the Revealer use his shot?");
	}

	public String wake() {
		if (abilityUsed)
			return "The " + name.name().replaceAll("_", " ") + " does nothing on this night!";

		if (isAnswerFalse()) {
			return "The Revealer does nothing on this night!";
		}
		return super.wake();
	}

	public String target(String target) {
		abilityUsed = true;
		return super.target(target);
	}
}
