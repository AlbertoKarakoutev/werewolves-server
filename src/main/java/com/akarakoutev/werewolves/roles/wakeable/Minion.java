package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;

import java.util.stream.Collectors;

public class Minion extends WakeableRole {

	// TESTED

	public Minion() {
		super();
		team = Game.Team.WEREWOLVES;
	}

	public String wake() {
		super.wake();
		if (game.getNights() == 1) {
			if (game.getPlayers().stream().noneMatch(Player::isWolf)) {
				return "There are no Werewolves!";
			} else {
				return "The Werewolves are: " + game.getPlayers()
						.stream()
						.filter(Player::isWolf)
						.map(Player::getName)
						.collect(Collectors.joining(", "));
			}
		}

		return "The minion does nothing on this night";
	}
}
