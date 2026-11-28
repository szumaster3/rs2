package content.region.misthalin.varrock.plugin;

import core.cache.def.impl.NpcType;
import core.game.interaction.OptionHandler;
import core.game.node.Node;
import core.game.node.entity.npc.NPC;
import core.game.node.entity.player.Player;
import core.game.world.update.flag.context.Animation;
import core.plugin.Initializable;
import core.plugin.Plugin;
import shared.consts.Animations;

/**
 * The type Shoo away stray dog plugin.
 */
@Initializable
public final class ShooAwayStrayDogPlugin extends OptionHandler {

    private static final Animation ANIMATION = new Animation(Animations.EMOTE_YA_BOO_SUCKS_2110);

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
        NpcType.forId(5917).getHandlers().put("option:shoo-away", this);
        NpcType.setOptionHandler("shoo-away", this);
        return this;
    }

    @Override
    public boolean handle(Player player, Node node, String option) {
        player.sendChat("Thbbbbt!");
        player.animate(ANIMATION);
        NPC dog = (NPC) node;
        dog.sendChat("Whine!");
        dog.moveStep();
        dog.getPulseManager().clear();
        return true;
    }

}
