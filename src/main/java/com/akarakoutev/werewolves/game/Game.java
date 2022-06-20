package com.akarakoutev.werewolves.game;

import java.util.*;

import com.akarakoutev.werewolves.chat.Chat;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.generic.Role.RoleName;
import com.akarakoutev.werewolves.vote.Vote;

public class Game {
	private int nights = 0;
	private boolean day = true;
	private boolean diseasedKilled = false;
	private Integer troublemakerNight = null;

	private int numberOfPlayers;

	private boolean started = false;
	private final String gameID;

	public List<Player> inGame = new ArrayList<>();

	private final Map<UUID, Chat> chats;
	private final Map<UUID, Vote> votes;
	private final Map<Integer, Player> lynched;

	private List<Player> players = new ArrayList<>();
	private List<RoleName> activeRoles = new ArrayList<>();
	private List<RoleName> passiveRoles = new ArrayList<>();

	public enum Team {
		VILLAGE,
		WEREWOLVES,
		CULT,
		VAMPIRES
	}

	public Game(int numberOfPlayers) {
		Random r = new Random();
		this.numberOfPlayers = numberOfPlayers;
		this.gameID = Integer.toString(r.nextInt(10000));
		chats = new HashMap<>();
		votes = new HashMap<>();
		lynched = new HashMap<>();

		activeRoles.addAll(List.of(RoleName.ALFA_WOLF, RoleName.APPRENTICE_SEER, RoleName.AURA_SEER, RoleName.BODYGUARD,
				RoleName.CULT_LEADER, RoleName.DRUNK, RoleName.HUNTRESS, RoleName.LONE_WOLF, RoleName.MINION, RoleName.MYSTIC_SEER,
				RoleName.OLD_HAG, RoleName.PRIEST, RoleName.PI, RoleName.REVEALER, RoleName.SEER, RoleName.SORCERESS,
				RoleName.SPELLCASTER, RoleName.VAMPIRE, RoleName.VAMPIRE, RoleName.VAMPIRE, RoleName.VILLAGER, RoleName.VILLAGER,
				RoleName.VILLAGER, RoleName.WEREWOLF, RoleName.WEREWOLF,
				RoleName.WEREWOLF, RoleName.WITCH, RoleName.WOLF_CUB));
		passiveRoles.addAll(List.of(RoleName.CUPID, RoleName.CURSED, RoleName.DISEASED, RoleName.DOPPELGANGER,
				RoleName.HOODLUM, RoleName.HUNTER, RoleName.LYCAN, RoleName.MAD_BOMBER,	RoleName.MASON, RoleName.MASON,
				RoleName.MASON, RoleName.MAYOR, RoleName.PRINCE, RoleName.PACIFIST, RoleName.TANNER,
				RoleName.TROUBLEMAKER, RoleName.TOUGH_GUY, RoleName.VILLAGER, RoleName.VILLAGER, RoleName.VILLAGER,
				RoleName.VILLAGE_IDIOT));
	}

	public List<Player> getPlayers() {
		return this.players;
	}
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public String getID(){
		return gameID;
	}

	public List<RoleName> getPassiveRoles() {
		return passiveRoles;
	}
	public void setPassiveRoles(List<RoleName> passiveRoles) {
		this.passiveRoles = passiveRoles;
	}

	public List<RoleName> getActiveRoles() {
		return activeRoles;
	}
	public void setActiveRoles(List<RoleName> activeRoles) {
		this.activeRoles = activeRoles;
	}

	public boolean getDiseasedKilled() {
		return diseasedKilled;
	}
	public void setDiseasedKilled(boolean diseasedKilled) {
		this.diseasedKilled = diseasedKilled;
	}

	public boolean getDay() {
		return day;
	}
	public void setDay(boolean day) {
		this.day = day;
	}

	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}
	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	public int getNights() {
		return nights;
	}
	public void setNights(int nights) {
		this.nights = nights;
	}

	public boolean getStarted() {
		return started;
	}
	public void setStarted(boolean started) {
		this.started = started;
	}

	public Integer getTroublemakerNight() {
		return troublemakerNight;
	}
	public void setTroublemakerNight(int night) {
		troublemakerNight = night;
	}

	public Map<UUID, Chat> getChats() {
		return chats;
	}

	public Map<UUID, Vote> getVotes() {
		return votes;
	}

	public Map<Integer, Player> getLynched() {
		return lynched;
	}

	@Override
	public String toString() {
		 return players.size() + "/" + numberOfPlayers;
	 }
	
}


