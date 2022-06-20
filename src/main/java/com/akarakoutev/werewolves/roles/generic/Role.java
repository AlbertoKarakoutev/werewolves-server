package com.akarakoutev.werewolves.roles.generic;

import com.akarakoutev.werewolves.game.Game;
import com.akarakoutev.werewolves.player.Player;
import com.akarakoutev.werewolves.roles.wakeable.*;
import com.akarakoutev.werewolves.roles.targeter.*;

/**
 * A base Role object, used to describe the generic properties of each role,
 * as well as to handle the creation and listing of all roles in the game
 *
 * @author Alberto Karakoutev
 * @see RoleName
 */
public class Role {

    private static final String SPRITE_PATH_FORMAT = "src/main/resources/img/%s.jpg";

    public Player owner;
    public String sprite;
    public String gameId;
    public RoleName name;
    public Game.Team team;

    public Role() {
    }

    public enum RoleName {
        // Active
        ALFA_WOLF(AlfaWolf.class),
        APPRENTICE_SEER(ApprenticeSeer.class),
        AURA_SEER(AuraSeer.class),
        BODYGUARD(Bodyguard.class),
        CULT_LEADER(CultLeader.class),
        DRUNK(Drunk.class),
        HUNTRESS(Huntress.class),
        LONE_WOLF(LoneWolf.class),
        MINION(Minion.class),
        MYSTIC_SEER(MysticSeer.class),
        OLD_HAG(OldHag.class),
        PRIEST(Priest.class),
        PI(PI.class),
        REVEALER(Revealer.class),
        SEER(Seer.class),
        SORCERESS(Sorceress.class),
        SPELLCASTER(Spellcaster.class),
        VAMPIRE(Vampire.class),
        VILLAGER(Villager.class),
        WEREWOLF(Werewolf.class),
        WITCH(Witch.class),
        WOLF_CUB(WolfCub.class),

        // Passive
        CUPID(Cupid.class),
        CURSED(Cursed.class),
        DISEASED(Diseased.class),
        DOPPELGANGER(Doppelganger.class),
        HOODLUM(Hoodlum.class),
        HUNTER(Hunter.class),
        LYCAN(Lycan.class),
        MAD_BOMBER(MadBomber.class),
        MASON(Mason.class),
        MAYOR(Mayor.class),
        PRINCE(Prince.class),
        PACIFIST(Pacifist.class),
        TANNER(Tanner.class),
        TOUGH_GUY(ToughGuy.class),
        TROUBLEMAKER(Troublemaker.class),
        VILLAGE_IDIOT(VillageIdiot.class);

        final Class<? extends Role> roleName;
        RoleName(Class<? extends Role> roleName) {
            this.roleName = roleName;
        }
        public static RoleName from(String term) {
            for (RoleName roleName : RoleName.values()) {
                if (roleName.name().equals(term))
                    return roleName;
            }
            throw new IllegalArgumentException(String.format("Role name %s not found!", term));
        }

        public Class<? extends Role> getRole() {
            return roleName;
        }
    }

    /**
     * Create a role based on a role name and assign it to a player
     *
     * @param roleName The name object of the role
     * @param owner    The player object for the role to be assigned to
     * @param gameId   The id of the target game
     * @return     	   The created role
     * @see            RoleName
     */
    public static Role createRole(RoleName roleName, Player owner, String gameId) {
        try {
            return (roleName
                    .getRole()
                    .getDeclaredConstructor()
                    .newInstance())
                    .withOwner(owner)
                    .withGameId(gameId)
                    .withName(roleName)
                    .withSprite(String.format(SPRITE_PATH_FORMAT,
                            roleName.getRole().getSimpleName().replaceAll(" ", "")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Role withName(RoleName name) {
        this.name = name;
        return this;
    }

    private Role withSprite(String sprite) {
        this.sprite = sprite;
        return this;
    }

    Role withOwner(Player owner) {
        this.owner = owner;
        return this;
    }

    public Role withGameId(String gameId) {
        this.gameId = gameId;
        return this;
    }

    public String toString() {
        return name + "("+team+")";
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (!role.owner.getName().equals(owner.getName())) return false;
        if (role.team != team) return false;

        return role.name.equals(name);
    }
}