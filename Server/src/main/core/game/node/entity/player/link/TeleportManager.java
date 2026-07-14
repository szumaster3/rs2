package core.game.node.entity.player.link;

import content.data.GameAttributes;
import content.global.plugins.item.ForinthryBraceletPlugin;
import content.region.other.tutorial_island.plugin.*;
import core.ServerConstants;
import core.game.interaction.QueueStrength;
import core.game.node.entity.Entity;
import core.game.node.entity.impl.Animator.Priority;
import core.game.node.entity.player.Player;
import core.game.world.GameWorld;
import core.game.world.map.Location;
import core.game.world.update.flag.context.Animation;
import core.game.world.update.flag.context.Graphics;
import core.worker.ManagementEvents;
import proto.management.JoinClanRequest;
import shared.consts.Animations;
import shared.consts.Sounds;
import static core.api.ContentAPIKt.*;

/**
 * Handles the entity teleport.
 *
 * @author SonicForce41, Woah
 */
public class TeleportManager {

    /**
     * The constant wilderness teleports.
     */
    public static final int WILDERNESS_TELEPORT = 1 << 16 | 8;

    /**
     * The animations used in the home teleport.
     */
    private final static int[] HOME_ANIMATIONS = {1722, 1723, 1724, 1725, 2798, 2799, 2800, 3195, 4643, 4645, 4646, 4847, 4848, 4849, 4850, 4851, 4852, 65535};

    /**
     * The graphics used in the home teleport.
     */
    private final static int[] HOME_GRAPHICS = {775, 800, 801, 802, 803, 804, 1703, 1704, 1705, 1706, 1707, 1708, 1709, 1710, 1711, 1712, 1713, 65535};

    /**
     * The entity being handled.
     */
    private final Entity entity;

    /**
     * The last teleport location.
     */
    private final Location lastTeleport;

    /**
     * The current teleport type.
     */
    private int teleportType;

    /**
     * Instantiates a new Teleport manager.
     *
     * @param entity the entity
     */
    public TeleportManager(Entity entity) {
        this.entity = entity;

        Location defaultLocation = ServerConstants.HOME_LOCATION;

        if (entity instanceof Player) {
            Player player = (Player) entity;
            defaultLocation = player.getRespawnLocation();
        }

        this.lastTeleport = defaultLocation;
    }

    /**
     * Sends the teleport.
     *
     * @param location the Location.
     * @return {@code True} if the player successfully started teleporting.
     */
    public boolean send(Location location) {
        return send(location, entity instanceof Player ? getType((Player) entity) : TeleportType.NORMAL, 0);
    }

    /**
     * Sends the teleport.
     *
     * @param location the Location.
     * @param type     the NodeType.
     * @return {@code True} if the player successfully started teleporting.
     */
    public boolean send(Location location, TeleportType type) {
        return send(location, type, 0);
    }

    /**
     * Sends the teleport.
     *
     * @param location     the Location.
     * @param type         the NodeType.
     * @param teleportType The teleporting type. (0=spell, 1=item, 2=object, 3=npc -1= force)
     * @return {@code True} if the player successfully started teleporting.
     */
    public boolean send(Location location, TeleportType type, int teleportType) {
        boolean canIgnoreTeleblock = false;

        /*
         * The Forinthry bracelet only bypasses revenant teleblock.
         * Do not consume a charge unless teleport is actually blocked.
         */

        if (hasTimerActive(entity, GameAttributes.TELEBLOCK_TIMER)
                && entity.isPlayer()
                && entity.getAttribute(GameAttributes.REVENANT_TELEBLOCK, false)) {
            canIgnoreTeleblock = ForinthryBraceletPlugin.canIgnoreTeleblock(entity.asPlayer());
        }

        if (teleportType == WILDERNESS_TELEPORT || type == TeleportType.OBELISK) {
            if (hasTimerActive(entity, GameAttributes.TELEBLOCK_TIMER) && !canIgnoreTeleblock) {
                if (entity.isPlayer()) {
                    entity.asPlayer().sendMessage("A magical force has stopped you from teleporting.");
                }
                return false;
            }

        } else {
            if (!entity.getZoneMonitor().teleport(teleportType, null)) {
                return false;
            }
            if (teleportType != -1 && entity.isTeleBlocked() && !canIgnoreTeleblock) {
                if (entity.isPlayer()) {
                    entity.asPlayer().sendMessage("A magical force has stopped you from teleporting.");
                }
                return false;
            }
        }
        if (entity instanceof Player) {
            ((Player) entity).getDialogueInterpreter().close();
            ((Player) entity).getInterfaceManager().close();
        }
        if (entity.getAttribute("tablet-spell", false)) {
            type = TeleportType.TELETABS;
        }
        this.teleportType = teleportType;
        entity.getWalkingQueue().reset();
        entity.lock(12);
        entity.getLocks().lockComponent(12);
        entity.getImpactHandler().setDisabledTicks(teleportType == -1 ? 5 : 12);
        type.queue(entity, location);
        return true;
    }

    /**
     * Fires a random event.
     *
     * @param entity   The entity teleporting.
     * @param location The destination location.
     */
    public static void fireRandom(Entity entity, Location location) {
        if (entity instanceof Player && entity.getTeleporter().teleportType == 0) {
            Player p = (Player) entity;
        }
    }

    /**
     * Get the home teleport audio based on tick count.
     *
     * @param count
     */
    private static int getAudio(int count) {
        switch (count) {
            case 0:
                return Sounds.AIDE_TP_CHALK_193;
            case 4:
                return Sounds.AIDE_TP_SITDOWN_196;
            case 8:
                return Sounds.AIDE_TP_BOOK_194;
            case 11:
                return Sounds.AIDE_TP_PORTAL_195;
        }
        return -1;
    }

    /**
     * Gets entity.
     *
     * @return the entity
     */
    public final Entity getEntity() {
        return entity;
    }

    /**
     * Represents a NodeType for Teleporter.
     *
     * @author SonicForce41
     */
    public enum TeleportType {
        NORMAL(new TeleportSettings(
                Animations.MODERN_TELEPORT_START_8939,
                Animations.MODERN_TELEPORT_END_8941,
                shared.consts.Graphics.NORMAL_TP_UPWARDS_1576,
                shared.consts.Graphics.NORMAL_TP_DOWNWARDS_1577
        )) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_ALL_200);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 4:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_REVERSE_201);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));

                            entity.unlock();
                            entity.lock(4);
                            return true;
                    }
                    return false;
                });
            }
        },

        ANCIENT(new TeleportSettings(
                Animations.CAST_SPELL_1979,
                -1,
                shared.consts.Graphics.ANCIENTS_TP_392,
                -1
        )) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.BLOCK_TP_197, 0, 7);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 4:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 5:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.graphics(new Graphics(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        LUNAR(new TeleportSettings(
                Animations.OLD_SHRINK_AND_RISE_UP_TP_1816,
                -1,
                shared.consts.Graphics.LUNARS_TP_747,
                -1
        )) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.graphics(new Graphics(getSettings().getStartGfx(), 120));
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 4:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        TELETABS(new TeleportSettings(
                Animations.TELEPORT_TABLET_4731, -1,
                shared.consts.Graphics.BLUE_GLOW_678, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.getAnimator().forceAnimation(new Animation(Animations.BREAK_SPELL_TABLET_A_4069));
                            return false;
                        case 2:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            return false;
                        case 4:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote()));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            entity.unlock();
                            entity.lock(2);
                            return true;
                    }
                    return false;
                });
            }
        },

        /**
         * TODO: Complete this teleport with delay 30 min.
         */
        HOME(new TeleportSettings(
                Animations.HOME_TP_ANIMATION_4847,
                Animations.IDLE_4857,
                shared.consts.Graphics.HOME_TP_DRAW_800,
                shared.consts.Graphics.HOME_TP_RING_804
        )) {
            @Override
            public void queue(Entity entity, Location location) {
                Player player = (Player) entity;
                queueScript(entity, 0, QueueStrength.WEAK, false, stage -> {
                    if (stage == 18) {
                        player.getProperties().setTeleportLocation(location);
                        if (player.getAttribute(GameAttributes.TUTORIAL_STAGE, -1) == 72) {
                            completeTutorial(player);
                        }
                        return true;
                    }
                    playGlobalAudio(entity.getLocation(), getAudio(stage));
                    player.getPacketDispatch().sendGraphic(HOME_GRAPHICS[stage]);
                    player.getPacketDispatch().sendAnimation(HOME_ANIMATIONS[stage]);
                    return false;
                });
            }
        },

        OBELISK(new TeleportSettings(
                Animations.MODERN_TELEPORT_START_8939,
                Animations.MODERN_TELEPORT_END_8941,
                shared.consts.Graphics.WILDERNESS_OBELISK_TP_661, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.lock();
                            entity.getAnimator().forceAnimation(new Animation(Animations.OLD_SHRINK_AND_RISE_UP_TP_1816));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            return false;
                        case 4:
                            entity.getAnimator().forceAnimation(Animation.RESET);
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        TELE_OTHER(new TeleportSettings(Animations.OLD_SHRINK_AND_RISE_UP_TP_1816, -1, shared.consts.Graphics.TELEOTHER_ACCEPT_342, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.TELE_OTHER_CAST_199);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            return false;
                        case 4:
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        FAIRY_RING(new TeleportSettings(-1, -1, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                entity.graphics(Graphics.create(shared.consts.Graphics.FLOWERS_AROUND_PLAYER_569));
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 2:
                            entity.animate(Animation.create(Animations.FADE_B_3265));
                            if (entity instanceof Player) {
                                playAudio(entity.asPlayer(), Sounds.FT_FAIRY_TP_1098);
                            }
                            return false;
                        case 4:
                            entity.animate(Animation.create(Animations.FADE_A_3266));
                            entity.getProperties().setTeleportLocation(location);
                            entity.unlock();
                            entity.lock(2);
                            return true;
                    }
                    return false;
                });
            }
        },

        PURO_PURO(new TeleportSettings(Animations.ANIMATION_6601, Animations.HANDHOLDS_1118, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(shared.consts.Graphics.TP_TO_PURO_PURO_WHEAT_FIELD_BEAM_1118));
                            return false;
                        case 9:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        ECTOPHIAL(new TeleportSettings(
                Animations.MODERN_TELEPORT_START_8939,
                Animations.MODERN_TELEPORT_END_8941,
                1587,
                1588
        )) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_ALL_200);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 4:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_REVERSE_201);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            return true;
                    }
                    return false;
                });
            }
        },

        CHRISTMAS(new TeleportSettings(7534, -1, shared.consts.Graphics.SNOW_GOING_UPWARDS_1292, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_ALL_200);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 4:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_REVERSE_201);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            return true;
                    }
                    return false;
                });
            }
        },

        CABBAGE(new TeleportSettings(9984, 9986, 1731, 1732)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            if (entity instanceof Player) {
                                playAudio(entity.asPlayer(), 5036);
                            }
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 5:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);

                            if (entity instanceof Player) {
                                playAudio(entity.asPlayer(), 5034);
                            }
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            return true;
                    }
                    return false;
                });
            }
        },

        ENTRANA_MAGIC_DOOR(new TeleportSettings(10100, 9013, 1745, 1747)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_ALL_200);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(getSettings().getStartGfx()));
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            fireRandom(entity, location);
                            return false;
                        case 4:
                            playGlobalAudio(entity.getLocation(), Sounds.TP_REVERSE_201);
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getEndEmote(), Priority.HIGH));
                            entity.graphics(new Graphics(getSettings().getEndGfx()));
                            return true;
                    }
                    return false;
                });
            }
        },

        RANDOM_EVENT_OLD(new TeleportSettings(714, -1, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.NORMAL, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(shared.consts.Graphics.OLD_RANDOM_EVENT_TP_308, 100, 50));

                            if (entity instanceof Player) {
                                playAudio(entity.asPlayer(), Sounds.TP_ALL_200);
                            }
                            return false;

                        case 4:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        MINIGAME(new TeleportSettings(6601, 1118, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(shared.consts.Graphics.TP_TO_PURO_PURO_WHEAT_FIELD_BEAM_1118));
                            return false;
                        case 9:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        PHARAOH_SCEPTRE(new TeleportSettings(714, 715, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.getAnimator().forceAnimation(new Animation(getSettings().getStartEmote()));
                            entity.graphics(new Graphics(shared.consts.Graphics.PHARAOH_SCEPTRE_TP_715));
                            return false;
                        case 4:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            entity.getAnimator().forceAnimation(new Animation(-1));
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        },

        INSTANT(new TeleportSettings(-1, -1, -1, -1)) {
            @Override
            public void queue(Entity entity, Location location) {
                queueScript(entity, 0, QueueStrength.STRONG, false, stage -> {
                    switch (stage) {
                        case 0:
                            entity.lock();
                            return false;
                        case 3:
                            entity.getProperties().setTeleportLocation(Location.create(location));
                            return false;
                        case 4:
                            entity.getAnimator().forceAnimation(Animation.RESET);
                            entity.unlock();
                            return true;
                    }
                    return false;
                });
            }
        };

        private final TeleportSettings settings;

        TeleportType(TeleportSettings settings) {
            this.settings = settings;
        }

        public abstract void queue(Entity entity, Location location);

        public TeleportSettings getSettings() {
            return settings;
        }
    }

    /**
     * Gets the teleporting type for the player depending on spellbook.
     *
     * @param player The player.
     * @return The teleport type.
     */
    public static TeleportType getType(Player player) {
        switch (player.getSpellBookManager().getSpellBook()) {
            case 193:
                return TeleportType.ANCIENT;
            case 430:
                return TeleportType.LUNAR;
            default:
                return TeleportType.NORMAL;
        }
    }

    /**
     * Completes the tutorial.
     *
     * @param player the player for whom the tutorial complete.
     */
    private static void completeTutorial(Player player) {
        setVarbit(player, TutorialStage.FLASHING_ICON, 0);
        setVarp(player, 281, 1000, true);
        setVarbit(player, 4895, 0, true);
        setAttribute(player, GameAttributes.TUTORIAL_COMPLETE, true);
        setAttribute(player, GameAttributes.TUTORIAL_STAGE, 73);

        player.unhook(TutorialCastReceiver.INSTANCE);
        player.unhook(TutorialKillReceiver.INSTANCE);
        player.unhook(TutorialFireReceiver.INSTANCE);
        player.unhook(TutorialResourceReceiver.INSTANCE);
        player.unhook(TutorialUseWithReceiver.INSTANCE);
        player.unhook(TutorialInteractionReceiver.INSTANCE);
        player.unhook(TutorialButtonReceiver.INSTANCE);

        if (GameWorld.getSettings() != null && GameWorld.getSettings().getEnable_default_clan()) {
            player.getCommunication().setCurrentClan(ServerConstants.SERVER_NAME);
            JoinClanRequest.Builder clanJoin = JoinClanRequest.newBuilder();
            clanJoin.setClanName(ServerConstants.SERVER_NAME);
            clanJoin.setUsername(player.getName());
            ManagementEvents.publish(clanJoin.build());
        }

        closeOverlay(player);
        player.getInventory().clear();
        player.getBank().clear();
        player.getEquipment().clear();
        player.getInventory().add(TutorialStage.STARTER_PACK);
        player.getBank().add(TutorialStage.STARTER_BANK);
        player.getInterfaceManager().restoreTabs();
        player.getInterfaceManager().setViewedTab(3);
        player.getInterfaceManager().openDefaultTabs();
        player.getDialogueInterpreter().sendDialogues("Welcome to Lumbridge! To get more help, simply click on the", "Lumbridge Guide or one of the Tutors - these can be found by", "looking for the question mark icon on your minimap. If you find you", "are lost at any time, look for a signpost or use the Lumbridge Home", "Teleport spell.");
        setAttribute(player, "close_c_", true);
    }

    /**
     * Represents teleport node settings
     *
     * @author SonicForce41
     */
    static class TeleportSettings {
        private final int startAnim;
        private final int endAnim;
        private final int startGfx;
        private final int endGfx;

        public TeleportSettings(int startAnim, int endAnim, int startGfx, int endGfx) {
            this.startAnim = startAnim;
            this.endAnim = endAnim;
            this.startGfx = startGfx;
            this.endGfx = endGfx;
        }

        public int getStartEmote() {
            return startAnim;
        }

        public int getEndEmote() {
            return endAnim;
        }

        public int getStartGfx() {
            return startGfx;
        }

        public int getEndGfx() {
            return endGfx;
        }
    }
}
