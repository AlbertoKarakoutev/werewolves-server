package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class AuraSeer extends TargeterRole {

	// TESTED

	public AuraSeer() {
		super();
		team = Game.Team.VILLAGE;
		targetType = TargetType.TARGET_AND_ROLE_CHECK;
		roleCheckType = RoleCheckType.AURA_SEER;
	}
}
