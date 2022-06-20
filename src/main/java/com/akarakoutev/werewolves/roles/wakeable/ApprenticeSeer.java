package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;

public class ApprenticeSeer extends WakeableRole {

	// TESTED

	public ApprenticeSeer() {
		super();
		team = Game.Team.VILLAGE;
	}

	public String wake() {
		super.wake();
		if (game.getPlayers().stream().anyMatch(player -> player.roleA.name == RoleName.SEER))
			return "The Apprentice Seer does nothing this night.";

		owner.roleA = createRole(RoleName.SEER, owner, game.getID());
		return "There is no seer. The Apprentice Seer is now the Seer.";
	}

}
