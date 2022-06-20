package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.net.mvc.BaseService;
import com.akarakoutev.werewolves.net.mvc.GameService;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;

import java.util.*;

public class AlfaWolf extends WakeableRole {

	// TESTED

	public AlfaWolf() {
		super();
		team = Game.Team.WEREWOLVES;
		question = new QA("Will the Alfa Wolf turn the target into a werewolf?");
	}

	public String wake() {
		super.wake();

		if (question.a != null && question.a) {
			for (Player p : game.getPlayers()) {
				if (p.is(TargeterRole.Effect.WOLVED)) {
					p.roleA = createRole(RoleName.WEREWOLF, p, game.getID());
					return p.getName() + " is now a Werewolf!";
				}
			}
		}

		return "The Alfa Wolf does nothing this night.";
	}

	public boolean canQuestionBeAsked() {
		return game.getNights() > 1
				&& game.getLynched().get(game.getNights() - 1) != null
				&& game.getLynched().get(game.getNights() - 1).roleA.name.equals(Role.RoleName.WEREWOLF);
	}

}
