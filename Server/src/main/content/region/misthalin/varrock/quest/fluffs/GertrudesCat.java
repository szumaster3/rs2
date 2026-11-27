package content.region.misthalin.varrock.quest.fluffs;

import content.data.Quests;

import core.game.node.entity.player.Player;
import core.game.node.entity.player.link.quest.Quest;
import core.game.node.entity.skill.Skills;
import core.game.node.item.GroundItemManager;
import core.game.node.item.Item;
import core.plugin.Initializable;
import core.tools.RandomFunction;
import shared.consts.Vars;

import static core.api.ContentAPIKt.addItemOrBank;

/**
 * The type Gertrudes cat.
 */
@Initializable
public class GertrudesCat extends Quest {

    /**
     * Instantiates a new Gertrudes cat.
     */
    public GertrudesCat() {
        super(Quests.GERTRUDES_CAT, Vars.VARP_QUEST_GERTRUDES_CAT_PROGRESS_180, 0, 1, 100);
    }

    @Override
    public void drawJournal(Player player, int stage) {
        super.drawJournal(player, stage);
        switch (stage) {
            case 0:
                writeJournalLine(player, BLUE + "I can start this quest by speaking to " + RED + "Gertrude.", 4 + 7);
                writeJournalLine(player, BLUE + "She can be found to the " + RED + "west of Varrock.", 5 + 7);
                break;
            case 10:
                writeJournalLine(player, "<str>I accepted the challenge of finding Gertrude's lost cat.", 4 + 7);
                writeJournalLine(player, BLUE + "I need " + RED + "to speak to Shilop and Wilough" + BLUE + " at the " + RED + "marketplace.", 6 + 7);
                break;
            case 20:
                writeJournalLine(player, "<str>I accepted the challenge of finding Gertrude's lost cat.", 4 + 7);
                writeJournalLine(player, "<str>I spoke to Shilop, Gertrude's Son.", 5 + 7);
                writeJournalLine(player, BLUE + "I need to " + RED + "go to their play area " + BLUE + "and " + RED + "find the lost cat and", 7 + 7);
                writeJournalLine(player, RED + "return it to Gertrude.", 8 + 7);
                break;
            case 30:
                writeJournalLine(player, "<str>I accepted the challenge of finding Gertrude's lost cat.", 4 + 7);
                writeJournalLine(player, "<str>I spoke to Shilop, Gertrude's Son.", 5 + 7);
                writeJournalLine(player, "<str>I found the lost cat but it won't come back.</str>", 6 + 7);
                writeJournalLine(player, RED + "I still need to " + RED + "get her to follow me home.", 8 + 7);
                break;
            case 40:
            case 50:
                writeJournalLine(player, "<str>I accepted the challenge of finding Gertrude's lost cat.", 4 + 7);
                writeJournalLine(player, "<str>I spoke to Shilop, Gertrude's Son.", 5 + 7);
                writeJournalLine(player, "<str>I found the lost cat but it won't come back.</str>", 6 + 7);
                writeJournalLine(player, "<str>I gave the cat milk and sardines.</str>", 7 + 7);
                writeJournalLine(player, BLUE + "I still need to " + RED + "get her to follow me home.", 9 + 7);
                break;
            case 60:
                writeJournalLine(player, "<str>I accepted the challenge of finding Gertrude's lost cat.", 4 + 7);
                writeJournalLine(player, "<str>I spoke to Shilop, Gertrude's Son.", 5 + 7);
                writeJournalLine(player, "<str>I found the lost cat but it won't come back.</str>", 6 + 7);
                writeJournalLine(player, "<str>I gave the cat milk and sardines.</str>", 7 + 7);
                writeJournalLine(player, BLUE + "She ran off home.", 9 + 7);
                break;
            case 100:
                writeJournalLine(player, "<str>I helped Gertrude to find her lost cat,", 4 + 7);
                writeJournalLine(player, "<str>I fed it and returned her missing kitten,", 5 + 7);
                writeJournalLine(player, "<str>Gertrude gave me my very own pet for a reward.", 6 + 7);
                writeJournalLine(player, "<col=FF0000>QUEST COMPLETE!", 8 + 7);
                break;
        }
    }

    @Override
    public void finish(Player player) {
        super.finish(player);
        final Item kitten = getKitten();
        player.getPacketDispatch().sendString("1 Quest Point", 277, 8 + 2);
        player.getPacketDispatch().sendString("A kitten!", 277, 9 + 2);
        player.getPacketDispatch().sendString("1525 Cooking XP", 277, 10 + 2);
        player.getPacketDispatch().sendString("A chocolate cake", 277, 11 + 2);
        player.getPacketDispatch().sendString("A bowl of stew", 277, 12 + 2);
        player.getPacketDispatch().sendString("Raise cats.", 277, 13 + 2);
        player.getSkills().addExperience(Skills.COOKING, 1525);
        player.getPacketDispatch().sendItemZoomOnInterface(kitten.getId(), 240, 277, 3 + 2);
        setStage(player, 100);
        if (player.getFamiliarManager().hasFamiliar()) {
            addItemOrBank(player, kitten.getId(), 1);
        } else {
            player.getFamiliarManager().summon(kitten, true, false);
        }
        final Item cake = new Item(1897);
        final Item stew = new Item(2003);
        if (!player.getInventory().add(cake)) {
            GroundItemManager.create(cake, player.getLocation(), player);
        }
        if (!player.getInventory().add(stew)) {
            GroundItemManager.create(stew, player.getLocation(), player);
        }
    }

    /**
     * Gets kitten.
     *
     * @return the kitten
     */
    public Item getKitten() {
        return new Item(RandomFunction.random(1555, 1560));
    }

    @Override
    public Quest newInstance(Object object) {

        return this;
    }

}
