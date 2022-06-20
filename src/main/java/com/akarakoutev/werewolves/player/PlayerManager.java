package com.akarakoutev.werewolves.player;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.roles.generic.Role;
import com.akarakoutev.werewolves.roles.wakeable.WakeableRole;
import com.akarakoutev.werewolves.roles.targeter.TargeterRole;
import com.akarakoutev.werewolves.net.exc.PlayerNotFoundException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

/**
 * Handles all the business logic for all players of a specific game.
 *
 * @author Alberto Karakoutev
 * @see Player
 */
public class PlayerManager {

    private final Game game;

    public static final Logger logger = Logger.getLogger(PlayerManager.class.getName());

    public PlayerManager(Game game) {
        this.game = game;
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
     * Calculate the final result of the night's effects on a player. Cancel out where necessary
     *
     * @param targetPlayer The player, who has the effects
     * @see   TargeterRole.Effect
     */
    public void applyEffects(Player targetPlayer) {

        if (targetPlayer.is(TargeterRole.Effect.HUNTRESSED) || targetPlayer.is(TargeterRole.Effect.WITCH_K)) {
            targetPlayer.kill();
        }
        if (targetPlayer.is(TargeterRole.Effect.WOLVED)) {
            targetPlayer.kill();
            if (targetPlayer.roleP.name == Role.RoleName.CURSED) {
                targetPlayer.revive();
                targetPlayer.roleA = Role.createRole(Role.RoleName.WEREWOLF, targetPlayer, game.getID());
                logger.info("The Cursed is now a Werewolf!");
            }
            if (targetPlayer.roleP.name == Role.RoleName.TOUGH_GUY && targetPlayer.tgCounter +1 != game.getNights()) {
                targetPlayer.revive();
                targetPlayer.tgCounter = game.getNights();
            }
            if (targetPlayer.roleA.name == Role.RoleName.VAMPIRE) {
                logger.info("The Werewolves can not kill " + targetPlayer.getName() + "(VAMPIRE)!");
                targetPlayer.revive();
            }
            if (targetPlayer.roleP.name == Role.RoleName.DISEASED && targetPlayer.isDead()) {
                game.setDiseasedKilled(true);
            }

        }
        if (targetPlayer.is(TargeterRole.Effect.HEALED)
                || targetPlayer.is(TargeterRole.Effect.WITCH_H)
                || targetPlayer.is(TargeterRole.Effect.PRIESTED)) {
            targetPlayer.revive();
        }
        if (targetPlayer.roleP.name == Role.RoleName.MAD_BOMBER && targetPlayer.isDead()) {
            logger.info(targetPlayer.name + " was the Mad Bomber");
            int targetIndex = game.getPlayers().indexOf(targetPlayer);
            Player next = (targetIndex > 0)
                    ? game.getPlayers().get(targetIndex - 1)
                    : game.getPlayers().get(game.getPlayers().size()-1);
            Player previous = (targetIndex < game.getPlayers().size()-1)
                    ? game.getPlayers().get(targetIndex + 1)
                    : game.getPlayers().get(0);
            next.kill();
            previous.kill();
        }
        if (targetPlayer.isDead()) {
            if (targetPlayer.is(TargeterRole.Effect.DOPPELGANGED)) {
                for(Player p : game.getPlayers()) {
                    if(p.roleP.name == Role.RoleName.DOPPELGANGER) {
                        p.roleA = Role.createRole(targetPlayer.roleA.name, p, game.getID());
                        logger.info("The Doppelganger has the new active role of " + p.roleA.name.name());
                    }
                }
            }
        }
    }

    /**
     * Add both player roles to the unawoken list if they can be awoken
     *
     * @param player The player, who's roles should be sleeping
     */
    public void sleepPlayerRoles(Player player) {
        if (player.roleP instanceof WakeableRole) {
            ((WakeableRole) player.roleP).state = WakeableRole.State.UNAWOKEN;
        }
        if (player.roleA instanceof WakeableRole) {
            ((WakeableRole) player.roleA).state = WakeableRole.State.UNAWOKEN;
        }
    }

    /**
     * Pick random roles for the players. Remove the selected roles from the list to avoid duplicates.
     *
     * @see Role
     */
    public void setPlayerRoles() {
//        // 1 player testing
//        Player player1 = game.getPlayers().get(0);
//        player1.roleA = Role.createRole(Role.RoleName.from("WEREWOLF"), player1, game.getID());
//        logger.info("Player " + player1.getName()+"'s active: " + player1.roleA.name);
//        player1.roleP = Role.createRole(Role.RoleName.from("MASON"), player1, game.getID());
//        logger.info("Player " + player1.getName()+"'s passive: " + player1.roleP.name);

//        // 2 player testing
//        Player player1 = game.getPlayers().get(0);
//        player1.roleA = Role.createRole(Role.RoleName.from("WEREWOLF"), player1, game.getID());
//        logger.info("Player " + player1.getName()+"'s active: " + player1.roleA.name);
//        player1.roleP = Role.createRole(Role.RoleName.from("CUPID"), player1, game.getID());
//        logger.info("Player " + player1.getName()+"'s passive: " + player1.roleP.name);
//        Player player2 = game.getPlayers().get(1);
//        player2.roleA = Role.createRole(Role.RoleName.from("VILLAGER"), player2, game.getID());
//        logger.info("Player " + player2.getName()+"'s active: " + player2.roleA.name);
//        player2.roleP = Role.createRole(Role.RoleName.from("MASON"), player2, game.getID());
//        logger.info("Player " + player2.getName()+"'s passive: " + player2.roleP.name);

        // Production
        for (Player player : game.getPlayers()) {
            Random random = new Random();

            int activeIndex = random.nextInt(game.getActiveRoles().size());
            player.roleA = Role.createRole(game.getActiveRoles().get(activeIndex), player, game.getID());
            logger.info("Player " + player.getName()+"'s active: " + player.roleA.name);
            game.getActiveRoles().remove(activeIndex);

            int passiveIndex = random.nextInt(game.getPassiveRoles().size());
            player.roleP = Role.createRole(game.getPassiveRoles().get(passiveIndex), player, game.getID());
            logger.info("Player " + player.getName()+"'s passive: " + player.roleP.name);
            game.getPassiveRoles().remove(passiveIndex);
        }
    }

    /**
     * Get all wakeable roles for the game
     *
     * @return The role list
     * @see    WakeableRole
     */
    public List<WakeableRole> getAllWakeableRoles() {
        List<WakeableRole> actives = game.getPlayers().stream()
                .map(player -> player.roleA)
                .filter(role -> role instanceof WakeableRole)
                .map(role -> (WakeableRole) role)
                .collect(Collectors.toList());
        List<WakeableRole> passives = game.getPlayers().stream()
                .map(player -> player.roleP)
                .filter(role -> role instanceof WakeableRole)
                .map(role -> (WakeableRole) role)
                .collect(Collectors.toList());
        actives.addAll(passives);
        return actives;
    }

    /**
     * Create a new player object. Add it to the game
     *
     * @param username      The name of the player
     * @param playerAddress The IP address of the player client
     * @see   Player
     */
    public void createPlayer(String username, String playerAddress) {
        Player pl = new Player();
        pl.name = username;
        pl.address = playerAddress;
        game.getPlayers().add(pl);
    }

    /**
     * Remove a player object. Remove it from the game.
     *
     * @param player The player to be removed
     * @see   Player
     */
    public void removePlayer(Player player) {
        List<Player> players = game.getPlayers();
        players.remove(player);
        game.setPlayers(players);
        game.inGame.remove(player);
        game.setNumberOfPlayers(game.getNumberOfPlayers()-1);
    }

    /**
     * Set a player's ready-to-sleep flag to true
     *
     * @param player The player to be asleep
     * @see   Player#readyToSleep
     */
    public void setReadyToSleep(Player player) {
        player.readyToSleep = true;
    }

    /**
     * Get the sprite of a role and encode it to base 64.
     *
     * @param role The role to be encoded
     * @return     The encoded image
     * @throws     IOException When a sprite file is not found
     * @see        Role
     */
    public String getRoleSpriteEncoded(Role role) throws IOException {
        BufferedImage roleAImage = ImageIO.read(new File(role.sprite));
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ImageIO.write(roleAImage, "jpg", ba);
        byte[] bytesA = ba.toByteArray();
        return Base64.getEncoder().encodeToString(bytesA);
    }

    /**
     * Get a player by username
     *
     * @param username The username of the target player
     * @return         The player object from the game
     * @throws         PlayerNotFoundException When the player is not found
     * @see            Player
     */
    public Player getPlayer(String username) throws PlayerNotFoundException {
        for(Player player : game.getPlayers()) {
            if(player.name.equals(username)) {
                return player;
            }
        }
        throw new PlayerNotFoundException();
    }

    /**
     * Get a player by index
     *
     * @param index The index of the target player
     * @return      The player object from the game
     * @see         Player
     */
    public Player getPlayer(int index) {
        return game.getPlayers().get(index);
    }

}
