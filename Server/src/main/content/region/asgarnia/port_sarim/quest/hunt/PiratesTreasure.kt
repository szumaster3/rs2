package content.region.asgarnia.port_sarim.quest.hunt

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.item.Item
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class PiratesTreasure : Quest(
    Quests.PIRATES_TREASURE,
    23, 22, 2,
    Vars.VARP_QUEST_PIRATES_TREASURE_PROGRESS_71,
    0, 1, 4
) {

    companion object {
        private const val KARAMJAN_RUM = Items.KARAMJAN_RUM_431
        private const val KEY = Items.CHEST_KEY_432
        private const val PIRATE_MESSAGE = Items.PIRATE_MESSAGE_433
        private const val CASKET = Items.CASKET_7956

        val CASKET_REWARDS: Array<Item> = arrayOf(
            Item(Items.COINS_995, 450),
            Item(Items.GOLD_RING_1635, 1),
            Item(Items.EMERALD_1605, 1)
        )
    }

    override fun newInstance(`object`: Any?): Quest {
        return this
    }

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11

        when (stage) {
            0 -> {
                line(player, "I can start this quest by speaking to !!Redbeard Frank?? who", line++)
                line(player, "is at !!Port Sarim??.", line++)
                line(player, "There aren't any requirements for this quest.", line)
            }

            10 -> {
                if (!inInventory(player, KARAMJAN_RUM)) {
                    line(player, "I have spoken to !!Redbeard Frank??. He has agreed to tell me", line++)
                    line(player, "the location of some !!treasure?? for some !!Karamjan rum??.", line++)
                    line(player, "I have taken employment on the !!banana plantation??,", line++)
                    line(player, "as the !!Customs Officers?? might not notice the rum if", line++)
                    line(player, "it is covered in !!bananas??.", line++)
                    line++
                    line(player, "Now all I need is some !!rum?? to hide in the next crate", line++)
                    line(player, "destined for !!Wydin's store??...", line)
                } else {
                    line(player, "I have spoken to !!Redbeard Frank??. He has agreed to tell me", line++)
                    line(player, "the location of some !!treasure?? for some !!Karamjan rum??.", line++)
                    line++
                    line(player, "I have the !!Karamjan rum??. I should take it to !!Redbeard Frank??.", line)
                }
            }

            20 -> {
                line(player, "<str>I have smuggled some rum off Karamja, and retrieved it", line++)
                line(player, "<str>from the back room of Wydin's shop.", line++)
                line++
                line(player, "I have given the rum to !!Redbeard Frank??. He has told me", line++)
                line(player, "that the !!treasure?? is hidden in the chest in the upstairs", line++)
                line(player, "room of the !!Blue Moon Inn?? in !!Varrock??.", line++)

                when {
                    player.attributes.containsKey("pirate-read") -> {
                        line++
                        line(player, "The note reads !!'Visit the city of the White Knights. In the", line++)
                        line(player, "park, Saradomin points to the X which marks the spot.'??", line++)
                        if (!inInventory(player, PIRATE_MESSAGE) && !inBank(player, PIRATE_MESSAGE)) {
                            line++
                            line(player, "It's a good job I remembered that, as I have lost the !!note??.", line)
                        }
                    }

                    inInventory(player, PIRATE_MESSAGE) -> {
                        line++
                        line(player, "I have opened the chest in the !!Blue Moon??, and found a", line++)
                        line(player, "!!note?? inside. I think it will tell me where to dig.", line)
                    }

                    inInventory(player, KEY) -> {
                        line++
                        line(player, "I have a !!key?? that can be used to unlock the chest that", line++)
                        line(player, "holds the treasure.", line)
                    }

                    else -> {
                        line++
                        line(player, "I have lost the !!key?? that !!Redbeard Frank?? gave me. I should", line++)
                        line(player, "see if he has another.", line)
                    }
                }
            }

            100 -> {
                line(player, "<str>The note reads 'Visit the city of the White Knights. In the", line++)
                line(player, "<str>park, Saradomin points to the X which marks the spot.'", line++)
                line++
                line(player, "%%QUEST COMPLETE!&&", line++)
                line++
                line(player, "I've found a treasure, gained 2 Quest Points and gained", line++)
                line(player, "access to the !!Pay-fare?? option to travel to and from", line++)
                line(player, "!!Karamja??!", line)
            }
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10
        drawReward(player, Items.CASKET_7956)
        drawReward(player, "2 Quest Points", line++)
        drawReward(player, "One-Eyed Hector's Treasure", line++)
        drawReward(player, "Chest", line++)
        drawReward(player, "You can also use the Pay-", line++)
        drawReward(player, "fare option to go to and from", line++)
        drawReward(player, "Karamja", line)
        removeAttribute(player, "gardener-attack")
        removeAttribute(player, "pirate-read")
        addItemOrDrop(player, CASKET)
        updateQuestTab(player)
    }
}
