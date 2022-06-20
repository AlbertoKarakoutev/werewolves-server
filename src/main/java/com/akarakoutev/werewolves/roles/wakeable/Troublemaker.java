package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.wakeable.WakeableRole;

import java.util.ArrayList;

public class Troublemaker extends WakeableRole {

	// TESTED

	public Troublemaker() {
		super();
		team = Game.Team.VILLAGE;

		question = new QA("Will the Troublemaker use her ability?");
	}

	public String wake() {
		super.wake();
		if (abilityUsed)
			return "The " + name.name().replaceAll("_", " ") + " does nothing on this night!";

		if (question.a != null && question.a) {
			game.setTroublemakerNight(game.getNights());
			abilityUsed = true;
			return "Successfully used ability!";
		}

		return "The Troublemaker does nothing on this night!";
	}

}
