package com.akarakoutev.werewolves.player;

import java.util.ArrayList;

import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;

public class Player {

	public ArrayList<TargeterRole.Effect> effects;

	public int tgCounter = -2;

	public boolean readyToSleep;
	public boolean dead = false;

	public String address;
	public String name;
	public Role roleA;
	public Role roleP;

	
	public Player() {
		effects = new ArrayList<>();
	}
	
	public boolean isWolf() {
		return roleA.name == Role.RoleName.WEREWOLF || roleA.name == Role.RoleName.LONE_WOLF || roleA.name == Role.RoleName.WOLF_CUB;
	}
	
	public boolean isVamp() {
		return roleA.name == Role.RoleName.VAMPIRE;
	}

	public boolean isMason() { return roleA.name == Role.RoleName.MASON || roleP.name == Role.RoleName.MASON; }

	public boolean isDead() {
		return dead;
	}

	public boolean is(TargeterRole.Effect effect) {
		return effects.contains(effect);
	}

	public void kill() {
		dead = true;
	}

	public void revive() {
		dead = false;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null || getClass() != o.getClass()) return false;

		Player player = (Player) o;

		if(!player.getName().equals(name)) return false;
		if(!player.getAddress().equals(address)) return false;
		if(player.roleA != roleA) return false;

		return player.roleP == roleP;
	}
}
