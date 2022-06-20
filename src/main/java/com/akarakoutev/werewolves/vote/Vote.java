package com.akarakoutev.werewolves.vote;

import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

public class Vote {

    private UUID id;
    private Type type;
    private int cycle;

    List<Player> voters;
    List<Player> votees;

    private final Map<Player, List<Player>> ballot;

    public enum Type {
        LYNCH("lynch"),
        WEREWOLVES("ww"),
        VAMPIRES("vamp");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Vote(UUID id, Type type, int cycle, List<Player> voters, List<Player> votees) {
        this.ballot = new HashMap<>();
        this.voters = voters;
        this.votees = votees;
        this.id = id;
        this.type = type;
        this.cycle = cycle;
    }

    public void setVote(Player voter, List<Player> votees) {
        if (type == Type.LYNCH) {
            if (ballot.containsKey(voter))
                return;
        }
        ballot.put(voter, votees);
    }

    private boolean everyoneHasVoted() {
        return ballot.keySet().equals(new HashSet<>(voters));
    }

    private boolean voteIsUnanimous() {
        Player randomPlayer = (Player) ballot.keySet().toArray()[0];
        return ballot.values().stream().allMatch(votees -> votees.containsAll(ballot.get(randomPlayer)));
    }

    public boolean isComplete() {
        if (type == Type.LYNCH)
            return everyoneHasVoted();

        return everyoneHasVoted() && voteIsUnanimous();
    }

    public Type getType() {
        return type;
    }

    public Player getVampedVotee() {
        Map<Player, Integer> voted = numberOfVotesForPlayers();

        for (Map.Entry<Player, Integer> next : voted.entrySet()) {
            if (next.getKey().is(TargeterRole.Effect.VAMPIRED) && next.getValue() == 2) {
                return next.getKey();
            }
        }
        return null;
    }

    public JsonObject serialize() {
        JsonObject serializedVote = new JsonObject();
        JsonArray serializedVotees = new JsonArray();
        for (Player votee : votees)
            serializedVotees.add(votee.getName());
        serializedVote.add("votees", serializedVotees);
        if (type != Type.LYNCH)
            serializedVote.addProperty("ballot", new Gson().toJson(numberOfVotesForPlayers()));
        serializedVote.addProperty("type", type.getValue());
        serializedVote.addProperty("cycle", Integer.toString(cycle));
        serializedVote.addProperty("id", id.toString());
        return serializedVote;
    }

    private Map<Player, Integer> numberOfVotesForPlayers() {
        Map<Player, Integer> playerIntegerMap = votees.stream().collect(Collectors.toMap(player -> player, player -> 0));
        for (Player voter : ballot.keySet()) {
            for (Player votee : ballot.get(voter)) {
                playerIntegerMap.merge(votee, (voter.roleP.name == Role.RoleName.MAYOR) ? 2 : 1, Integer::sum);
            }
        }

        return playerIntegerMap;
    }

    public Player getMostVoted() {
        return numberOfVotesForPlayers().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .get(0);
    }
}
