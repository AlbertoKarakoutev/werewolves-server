package com.akarakoutev.werewolves.roles.wakeable;

import java.util.Random;

import com.akarakoutev.werewolves.game.Game;

public class Drunk extends WakeableRole {

	// TESTED

	public Drunk() {
		super();
		team = Game.Team.VILLAGE;
	}

	public String wake() {
		super.wake();
		if (game.getNights() == 1) {
			return "The Drunk does nothing on this night.";
		} else if (game.getNights() == 2) {
			Random r = new Random();
			int roleIndex = r.nextInt(game.getActiveRoles().size());
			RoleName newRole = game.getActiveRoles().get(roleIndex);
			owner.roleA = createRole(newRole, owner, game.getID());

			return "The Drunk will become a " + newRole + " on the next night!";
		}
		return "";
	}
}
