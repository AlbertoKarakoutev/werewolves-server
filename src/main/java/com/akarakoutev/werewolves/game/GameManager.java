package com.akarakoutev.werewolves.game;

import com.akarakoutev.werewolves.chat.Chat;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.generic.Diseased;
import com.akarakoutev.werewolves.roles.generic.Hunter;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.generic.ToughGuy;
import com.akarakoutev.werewolves.roles.targeter.Cupid;
import com.akarakoutev.werewolves.roles.targeter.Werewolf;
import com.akarakoutev.werewolves.roles.wakeable.WakeableRole;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;
import com.akarakoutev.werewolves.player.PlayerManager;
import com.akarakoutev.werewolves.net.exc.PlayerNotFoundException;
import com.akarakoutev.werewolves.vote.Vote;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

/**
 * Handles all the business logic for a specific game.
 *
 * @author Alberto Karakoutev
 * @see Game
 * @see PlayerManager
 */
public class GameManager {

    public static final Logger logger = Logger.getLogger(GameManager.class.getName());

    private final PlayerManager playerManager;
    private final Game game;

    public GameManager(int numberOfPlayers) {
        game = new Game(numberOfPlayers);
        playerManager = new PlayerManager(game);
        try {
            FileHandler fh = new FileHandler("logs/game_" + game.getID() + ".log");
            logger.addHandler(fh);
            class MyFormatter extends SimpleFormatter {
                @Override
                public String format(final LogRecord record) {
                    return LocalDateTime.now().toString().replaceAll("T", " ") + ": " +MessageFormat.format(record.getMessage(), record.getParameters()) + "\n";
                }
            }
            MyFormatter formatter = new MyFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update the game state to day. Check all daily conditions. 
     * @return Whether the game is over
     */
    public String applyDay() {
        logger.info("-----------------[DAY TIME (" + game.getNights() + ")]-----------------");
        game.setDay(true);

        for (Player p : game.getPlayers()) {
            p.readyToSleep = false;
            playerManager.applyEffects(p);
            if (p.roleA instanceof WakeableRole
                && ((WakeableRole) p.roleA).hasQuestion()
                && ((WakeableRole) p.roleA).isAnswerFalse()) {
                ((WakeableRole) p.roleA).resetQuestion();
            }
            if (p.roleP instanceof WakeableRole
                    && ((WakeableRole) p.roleP).hasQuestion()
                    && ((WakeableRole) p.roleP).isAnswerFalse()) {
                ((WakeableRole) p.roleP).resetQuestion();
            }
        }

        checkSweethearts();
        checkKillHunter();
        return checkGameOver();
    }

    /**
     * Update the game state to night. Check all night conditions. Reset all necessary effects.
     */
    public void applyNight() {
        game.setDay(false);
        for (Player p : game.getPlayers()) {
            p.effects.remove(TargeterRole.Effect.HEALED);
            p.effects.remove(TargeterRole.Effect.WOLVED);
            p.effects.remove(TargeterRole.Effect.VAMPIRED);
            p.effects.remove(TargeterRole.Effect.HUNTRESSED);
            p.effects.remove(TargeterRole.Effect.HAGGED);
            p.effects.remove(TargeterRole.Effect.SILENCED);
            p.effects.remove(TargeterRole.Effect.WITCH_H);

            sleepPlayerRoles(p);
            checkKillToughGuy(p);
            checkKillDiseased();
        }
        incrementNights();
        logger.info("----------------[NIGHT TIME (" + game.getNights() + ")]----------------");
    }

    /**
     * Add a player to the in-game list
     *
     * @param player The player to be added
     */
    public void addInGamePlayer(Player player) {
        game.inGame.add(player);
    }

    /**
     * Compare the expected number of players to the actual
     *
     * @return The ready status of the game
     */
    public boolean isGameReadyToStart() {
        return game.inGame.size() == game.getNumberOfPlayers();
    }

    /**
     * Compare the game size to the number of logged in players
     *
     * @return Whether the game is full
     */
    public boolean areAllPositionsFilled() {
        return game.getNumberOfPlayers() <= game.getPlayers().size();
    }

    /**
     * Check if each player is ready
     *
     * @return The players' ready status
     */
    public boolean areAllReadyToSleep() {
        if(game.getPlayers().size() != game.getNumberOfPlayers())
            return false;
        for(Player player : game.getPlayers()) {
            if(!player.readyToSleep) {
                return false;
            }
        }
        logger.info("All players are ready to sleep");
        return true;
    }

    /**
     * Get the active game
     *
     * @return The game for this instance
     * @see    Game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Get a random role
     *
     * @return The retrieved role
     * @see    Game
     * @see    WakeableRole
     */
    public WakeableRole getRandomRole() {
        List<WakeableRole> wakeableRoles = playerManager.getAllWakeableRoles();

        // Check if a role is currently taking action
        if (wakeableRoles.stream().anyMatch(wakeableRole -> wakeableRole.state == WakeableRole.State.WAKING))
            return null;

        List<WakeableRole> unawokenRoles = wakeableRoles.stream().filter(wakeableRole -> wakeableRole.state == WakeableRole.State.UNAWOKEN).collect(Collectors.toList());

        int randIndex = new Random().nextInt(unawokenRoles.size());
        return unawokenRoles.get(randIndex);
    }

    /**
     * Check if there are any roles with an UNAWOKEN state
     * @return Whether any roles should be awoken
     */
    public boolean shouldAwake() {
        return playerManager.getAllWakeableRoles().stream()
                .filter(Objects::nonNull)
                .anyMatch(wakeableRole -> wakeableRole.state == WakeableRole.State.UNAWOKEN);
    }

    /**
     * Remove all players, flagged as 'dead' from the game
     */
    public void removeDeadPlayers() {
        int counter = game.getPlayers().size()-1;
        while(counter >= 0) {
            Player p = playerManager.getPlayer(counter);
            if(p.isDead()) {
                logger.info("Player " + p.getName() + " is dead. Removing from game...");
                playerManager.removePlayer(p);
            }
            counter--;
        }
    }

    /**
     * Add a lynched player for the current night of the game
     */
    public void addLynchedPlayer(Player player) {
        logger.info("Player " + player.getName() + " has been lynched on day " + game.getNights());
        game.getLynched().put(game.getNights(), player);
    }

    /**
     * Get the lynched player on the last day
     *
     * @return The lynched player
     */
    public Player getLastLynched() {
        if (game.getLynched().get(game.getNights() - 1) != null) {
            return game.getLynched().get(game.getNights() - 1);
        }
        return null;
    }

    /**
     * Link the Cupid's sweethearts so if one is dead, the other is killed too
     *
     * @see Cupid
     */
    private void checkSweethearts() {
        List<Player> sweethearts = game.getPlayers().stream()
                .filter(player -> player.is(TargeterRole.Effect.CUPIDED))
                .collect(Collectors.toList());
        if (sweethearts.stream().anyMatch(Player::isDead)) {
            sweethearts.forEach(Player::kill);
        }
    }

    /**
     * If it is the night, after the Tough Guy was killed, kill the Tough Guy
     *
     * @param player The player, who is checked
     * @see          ToughGuy
     */
    private void checkKillToughGuy(Player player) {
        if (player.roleP.name.equals(Role.RoleName.TOUGH_GUY) && game.getNights() == player.tgCounter +1) {
            logger.info("The Tough Guy dies on this night (Night " + game.getNights() + ")");
            player.kill();
        }
    }

    /**
     * If the Diseased was killed, the Werewolves can not kill anybody
     *
     * @see Diseased
     * @see Werewolf
     */
    private void checkKillDiseased() {
        if (game.getDiseasedKilled()) {
            logger.info("The Werewolves have killed the diseased and will not target on this night (Night " + game.getNights() + ")");
            for (Player wolf : game.getPlayers()) {
                if(wolf.isWolf()) {
                    ((WakeableRole) wolf.roleA).state = WakeableRole.State.AWOKEN;
                }
            }
            game.setDiseasedKilled(false);
        }
    }

    /**
     * If the Hunter is dead, he/she can kill a player
     *
     * @see          Hunter
     */
    private void checkKillHunter() {

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).roleP.name == Role.RoleName.HUNTER && game.getPlayers().get(i).isDead()) {

                // TODO Add hunter's on-death shot support
                // JOptionPane.showMessageDialog(null, "Hunter has died.");
                // String s = JOptionPane.showInputDialog("Who will the Hunter kill?");
                String s = "";
                for (Player hunted : game.getPlayers()) {
                    if (hunted.name.equals(s)) {
                        playerManager.removePlayer(hunted);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Check game over conditions
     *
     * @return The game over message
     * @see    Role.RoleName
     * @see    Game.Team
     */
    private String checkGameOver() {
        for (Player p : game.getPlayers()) {

            // Condition 1 (Cult Wins)
            if (p.roleA.team == Game.Team.CULT) {
                if (game.getPlayers().stream().allMatch(player -> player.is(TargeterRole.Effect.CULT) || player.roleA.name == Role.RoleName.CULT_LEADER)) {
                    return gameOver("The Cult has");
                }
            }

            // Condition 2 (Tanner Wins)
            if (p.roleP.name == Role.RoleName.TANNER && p.isDead()) {
                return gameOver("The Tanner has");
            }

            // Condition 3 (Hoodlum Wins)
            if (p.roleP.name == Role.RoleName.HOODLUM) {
                if (game.getPlayers().stream().filter(player -> player.is(TargeterRole.Effect.HOODLUMED)).allMatch(Player::isDead)) {
                    return gameOver("The Hoodlum has");
                }
            }

            // Condition 4 (Town Wins)
            if (p.roleA.team == Game.Team.VILLAGE) {
                if (game.getPlayers().stream().allMatch(player -> player.roleA.team == Game.Team.VILLAGE)) {
                    return gameOver("The Town has");
                }
            }

            // Condition 5 (Lone Wolf Wins)
            if (game.getPlayers().size() == 1 && p.roleA.name == Role.RoleName.LONE_WOLF) {
                gameOver("The Lone Wolf has");
            }

            // Condition 5 (Werewolves Win)
            if (p.roleA.team == Game.Team.WEREWOLVES) {
                if (game.getPlayers().stream().allMatch(player -> player.roleA.team == Game.Team.WEREWOLVES)) {
                    return gameOver("The Werewolves have");
                }
            }

            // Condition 7 (Vampires Win)
            if (p.roleA.team == Game.Team.VAMPIRES) {
                if (game.getPlayers().stream().allMatch(player -> player.roleA.team == Game.Team.VAMPIRES)) {
                    return gameOver("The Vampires have");
                }
            }
        }

        return null;
    }

    /**
     * Log game over
     * @param winner The team/player who has won message
     * @return       The full winner message
     */
    private String gameOver(String winner) {
        String gameOverMessage = winner + " won!";
        logger.info(gameOverMessage);
        return gameOverMessage;
    }

    /**
     * Increment nights
     */
    private void incrementNights() {
        game.setNights(game.getNights()+1);
    }

    /**
     * Get a specific role by role name
     *
     * @param roleName The role to be removed
     * @see            Role.RoleName
     */
    public Role getRole(Role.RoleName roleName) {
        for (Player player : game.getPlayers()) {
            if (player.roleA.name.equals(roleName))
                return player.roleA;
            if (player.roleP.name.equals(roleName))
                return player.roleP;
        }
        return null;
    }

    /**
     * Generate a JSON string from a role, following the <u>role.json</u> schema
     *
     * @param role The role to be serialized
     * <br><b>[Serialized]</b> <i>name</i> - The name of the role
     * <br><b>[Serialized]</b> <i>team</i> - The team of the role
     * <br><b>[Serialized]</b> <i>sprite</i> - The sprite of the role
     * <br><b>[Serialized]</b> <i>gameId</i> - The game ID of the target game
     * <br><b>[Serialized]</b> <i>awoken</i> - Whether the role has been awoken
     * @see        Role
     */
    public String serializeRole(Role role) throws IOException {
        Map<String, Object> roleMap = new HashMap<>();
        Gson serializer = new Gson();
        roleMap.put("name", role.name);
        roleMap.put("team", role.team.name());
        roleMap.put("sprite", getRoleSpriteEncoded(role));
        roleMap.put("gameId", role.gameId);
        roleMap.put("owner", role.owner.getName());
        return serializer.toJson(roleMap);
    }

    /**
     * Create a new chat
     *
     * @param type The chat type
     * @param id   The chat ID
     * @return     The created chat
     * @see        Chat
     */
    public Chat createChat(Chat.Type type, UUID id) {
        Chat chat = new Chat(id, type, game.getNights());
        game.getChats().put(id, chat);
        logger.info("Created chat " + id + " with type: " + type.name());

        return chat;
    }

    /**
     * Create a new vote
     *
     * @param type The vote type
     * @param id   The vote ID
     * @return     The created type
     * @see        Vote
     */
    public Vote createVote(Vote.Type type, UUID id) {
        List<Player> votees = game.getPlayers();
        List<Player> voters = game.getPlayers();
        Vote vote = new Vote(id, type, game.getNights(), voters, votees);
        game.getVotes().put(id, vote);
        logger.info("Created vote " + id + " with type: " + type.name());
        return vote;
    }

    /**
     * Create a new vote with a single voter
     *
     * @param type  The vote type
     * @param id    The vote ID
     * @param voter The voting player
     * @return      The created type
     * @see         Vote
     */
    public Vote createVote(Vote.Type type, UUID id, Player voter) {
        List<Player> votees = game.getPlayers();
        List<Player> voters = List.of(voter);
        if (type == Vote.Type.WEREWOLVES) {
            // votees = game.getPlayers().stream().filter(player -> !player.isWolf()).collect(Collectors.toList());
        } else if (type == Vote.Type.VAMPIRES) {
            // votees = game.getPlayers().stream().filter(player -> !player.isVamp()).collect(Collectors.toList());
        }
        Vote vote = new Vote(id, type, game.getNights(), voters, votees);
        game.getVotes().put(id, vote);
        logger.info("Created vote " + id + " with type: " + type.name());
        return vote;
    }

    /**
     * @see Chat#addMessage(LocalTime, Player, String)
     */
    public void addChatMessage(UUID id, LocalTime timestamp, Player sender, String message) {
        game.getChats().get(id).addMessage(timestamp, sender, message);
    }

    /**
     * Append to the log
     */
    public void log(String message) {
        logger.info(message);
    }

    // Method mappings directly to PlayerManager's methods
    /**
     * @see PlayerManager#createPlayer(String, String)
     */
    public void createPlayer(String username, String playerAddress) {
        playerManager.createPlayer(username, playerAddress);
    }
    /**
     * @see PlayerManager#setPlayerRoles()
     */
    public void setPlayerRoles() {
        playerManager.setPlayerRoles();
    }
    /**
     * @see PlayerManager#setReadyToSleep(Player)
     */
    public void setReadyToSleep(Player player) {
        playerManager.setReadyToSleep(player);
    }
    /**
     * @see PlayerManager#removePlayer(Player)
     */
    public void removePlayer(Player player) { playerManager.removePlayer(player); }
    /**
     * @see PlayerManager#getPlayer(String)
     */
    public Player getPlayer(String username) throws PlayerNotFoundException {
        return playerManager.getPlayer(username);
    }
    /**
     * @see PlayerManager#getPlayer(int)
     */
    public Player getPlayer(int index) {
        return playerManager.getPlayer(index);
    }
    /**
     * @see PlayerManager#getRoleSpriteEncoded(Role)
     */
    public String getRoleSpriteEncoded(Role role) throws IOException { return playerManager.getRoleSpriteEncoded(role); }
    /**
     * @see PlayerManager#sleepPlayerRoles(Player)
     */
    private void sleepPlayerRoles(Player player) {
        playerManager.sleepPlayerRoles(player);
    }

}
