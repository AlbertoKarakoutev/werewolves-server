package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Seer extends TargeterRole {

	// TESTED

	public Seer() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_ROLE_CHECK;
		roleCheckType = RoleCheckType.SEER;
	}

}
