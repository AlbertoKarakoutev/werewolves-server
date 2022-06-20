package com.akarakoutev.werewolves.roles.wakeable;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Mason extends WakeableRole {

	// TESTED

	public Mason() {
		super();
		team = Game.Team.VILLAGE;
	}

	public String wake() {
		super.wake();
		if (game.getNights() == 1) {
			return "The Masons are: " + game.getPlayers().stream()
					.filter(Player::isMason)
					.map(Player::getName)
					.collect(Collectors.joining(", "));
		} else {
			return "The masons do nothing on this night!";
		}
	}
}
