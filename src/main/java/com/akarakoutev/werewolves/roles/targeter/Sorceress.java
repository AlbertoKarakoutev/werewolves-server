package com.akarakoutev.werewolves.roles.targeter;

import com.akarakoutev.werewolves.game.Game;

public class Sorceress extends TargeterRole {

	// TESTED

	public Sorceress() {
		super();
		team = Game.Team.WEREWOLVES;
		targetType = TargetType.TARGET_AND_ROLE_CHECK;
		roleCheckType = RoleCheckType.SORCERESS;
	}

}
