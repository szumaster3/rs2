package content.region.wilderness.plugin;

import core.cache.def.impl.LocType;
import core.game.global.action.DoorActionHandler;
import core.game.interaction.DestinationFlag;
import core.game.interaction.MovementPulse;
import core.game.interaction.OptionHandler;
import core.game.node.Node;
import core.game.node.entity.impl.PulseType;
import core.game.node.entity.player.Player;
import core.game.node.entity.player.link.warning.WarningManager;
import core.game.node.entity.player.link.warning.WarningType;
import core.game.node.scenery.Scenery;
import core.game.world.map.Location;
import core.plugin.Initializable;
import core.plugin.Plugin;
import shared.consts.Animations;
import shared.consts.Sounds;

import static core.api.ContentAPIKt.forceMove;
import static core.api.ContentAPIKt.playAudio;

/**
 * Represents the plugin to handle the crossing.
 *
 * @author Vexia
 */
@Initializable
public final class WildernessDitchPlugin extends OptionHandler {

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
        LocType.forId(23271).getHandlers().put("option:cross", this);
        return this;
    }

    @Override
    public boolean handle(final Player player, final Node node, String option) {

        if (player.isArtificial()) {
            Location[] locations = getDitchLocations(player.getLocation(), node.getLocation(), 0);
            Location start = locations[0];
            Location end = locations[1];

            if (!player.getLocation().equals(start)) {
                player.getPulseManager().run(new MovementPulse(player, start, DestinationFlag.LOCATION) {
                    @Override
                    public boolean pulse() {
                        return true;
                    }
                });
                return true;
            }

            forceMove(player, start, end, 0, 60, null, Animations.HUMAN_JUMP_FENCE_6132, () -> {
                return kotlin.Unit.INSTANCE;
            });

            return true;
        }

        if (player.getLocation().getDistance(node.getLocation()) < 3) {
            handleDitch(player, node);
        } else {
            player.getPulseManager().run(new MovementPulse(player, node) {
                @Override
                public boolean pulse() {
                    handleDitch(player, node);
                    return true;
                }
            }, PulseType.STANDARD);
        }

        return true;
    }

    public void handleDitch(final Player player, Node node) {
        player.faceLocation(node.getLocation());

        Scenery ditch = (Scenery) node;
        player.setAttribute("wildy_ditch", ditch);

        if (!player.isArtificial()) {
            boolean crossInto = false;

            if (ditch.getRotation() % 2 == 0) {
                crossInto = player.getLocation().getY() <= node.getLocation().getY();
            } else {
                crossInto = player.getLocation().getX() <= node.getLocation().getX();
            }

            if (crossInto) {
                WarningManager.INSTANCE.trigger(player, WarningType.WILDERNESS_DITCH, () -> {
                    handleJump(player);
                    return kotlin.Unit.INSTANCE;
                });
                return;
            }
        }

        handleJump(player);
    }

    private void handleJump(Player player) {
        Scenery ditch = player.getAttribute("wildy_ditch");
        if (ditch == null) return;

        player.removeAttribute("wildy_ditch");

        if (player.getAttribute("wild-metal-gate", false)) {
            player.removeAttribute("wild-metal-gate");
            DoorActionHandler.handleAutowalkDoor(player, ditch);
            return;
        }

        Location[] locs = getDitchLocations(player.getLocation(), ditch.getLocation(), ditch.getRotation());
        Location start = locs[0];
        Location end = locs[1];

        if (player.getLocation().getDistance(ditch.getLocation()) < 3) {
            forceMove(player, start, end, 0, 60, null, Animations.HUMAN_JUMP_FENCE_6132, () -> {
                return kotlin.Unit.INSTANCE;
            });
            playAudio(player, Sounds.JUMP2_2462);
        } else {
            forceMove(player, start, end, 0, 60, null, Animations.HUMAN_JUMP_FENCE_6132, () -> {
                return kotlin.Unit.INSTANCE;
            });
            playAudio(player, Sounds.JUMP2_2462);
        }
    }

    public static Location[] getDitchLocations(Location playerLoc, Location ditchLoc, int rotation) {
        int x = playerLoc.getX();
        int y = playerLoc.getY();

        if (rotation % 2 == 0) {
            if (y <= ditchLoc.getY()) {
                return new Location[] {
                        Location.create(x, ditchLoc.getY() - 1, 0),
                        Location.create(x, ditchLoc.getY() + 2, 0)
                };
            } else {
                return new Location[] {
                        Location.create(x, ditchLoc.getY() + 2, 0),
                        Location.create(x, ditchLoc.getY() - 1, 0)
                };
            }
        } else {
            if (x > ditchLoc.getX()) {
                return new Location[] {
                        Location.create(ditchLoc.getX() + 2, y, 0),
                        Location.create(ditchLoc.getX() - 1, y, 0)
                };
            } else {
                return new Location[] {
                        Location.create(ditchLoc.getX() - 1, y, 0),
                        Location.create(ditchLoc.getX() + 2, y, 0)
                };
            }
        }
    }

    @Override
    public boolean isWalk() {
        return true;
    }
}