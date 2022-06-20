package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class MysticSeer extends TargeterRole {

	// TESTED

	public MysticSeer() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_ROLE_CHECK;
		roleCheckType = RoleCheckType.MYSTIC_SEER;
	}
}
