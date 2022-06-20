package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;

public class PI extends WakeableRole {

	// TESTED

	public PI() {
		super();
		team = Game.Team.VILLAGE;
	}

	public String wake() {
		super.wake();
		int ownerIndex = game.getPlayers().indexOf(owner);
		Player next = (ownerIndex > 0)
				? game.getPlayers().get(ownerIndex - 1)
				: game.getPlayers().get(game.getPlayers().size()-1);
		Player previous = (ownerIndex < game.getPlayers().size()-1)
				? game.getPlayers().get(ownerIndex + 1)
				: game.getPlayers().get(0);
		String message;
		if(next.isWolf() || previous.isWolf()) {
			message = "Either " + next.getName() + " or " + previous.getName() + " is a Werewolf.";
		} else {
			message = next.getName() + " and " + previous.getName() + " are not Werewolves.";
		}
		return message;
	}
}
