package content.region.kandarin.west_ardougne.quest.sheepherder

import content.data.Quests
import content.region.kandarin.west_ardougne.quest.sheepherder.npc.HerderSheepNPC
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.item.Item
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class SheepHerder : Quest(Quests.SHEEP_HERDER) {

    companion object {
        @JvmField
        val CATTLE_PROD = Item(Items.CATTLEPROD_278)

        @JvmField
        val POISON = Item(Items.SHEEP_FEED_279)

        @JvmField
        val PLAGUE_TOP = Item(Items.PLAGUE_JACKET_284)

        @JvmField
        val PLAGUE_BOTTOM = Item(Items.PLAGUE_TROUSERS_285)

        @JvmField
        val RED_SHEEP_BONES = Item(Items.SHEEP_BONES_1_280)

        @JvmField
        val GREEN_SHEEP_BONES = Item(Items.SHEEP_BONES_2_281)

        @JvmField
        val BLUE_SHEEP_BONES = Item(Items.SHEEP_BONES_3_282)

        @JvmField
        val YELLOW_SHEEP_BONES = Item(Items.SHEEP_BONES_4_283)

        const val RED_SHEEP = NPCs.HERDER_PLAGUESHEEP_1_2345
        const val GREEN_SHEEP = NPCs.HERDER_PLAGUESHEEP_2_2346
        const val BLUE_SHEEP = NPCs.HERDER_PLAGUESHEEP_3_2347
        const val YELLOW_SHEEP = NPCs.HERDER_PLAGUESHEEP_4_2348

        @JvmField
        val boneMap = hashMapOf(
            RED_SHEEP to RED_SHEEP_BONES,
            GREEN_SHEEP to GREEN_SHEEP_BONES,
            BLUE_SHEEP to BLUE_SHEEP_BONES,
            YELLOW_SHEEP to YELLOW_SHEEP_BONES,
        )

        private val sheepSpawns = mapOf(
            RED_SHEEP to arrayOf(
                Location.create(2609, 3343, 0),
                Location.create(2610, 3344, 0),
                Location.create(2609, 3345, 0),
                Location.create(2615, 3343, 0),
            ),
            GREEN_SHEEP to arrayOf(
                Location.create(2622, 3366, 0),
                Location.create(2622, 3366, 0),
                Location.create(2619, 3371, 0),
                Location.create(2617, 3365, 0),
            ),
            YELLOW_SHEEP to arrayOf(
                Location.create(2610, 3390, 0),
                Location.create(2613, 3389, 0),
                Location.create(2606, 3391, 0),
                Location.create(2608, 3387, 0),
            ),
            BLUE_SHEEP to arrayOf(
                Location.create(2559, 3388, 0),
                Location.create(2562, 3388, 0),
                Location.create(2563, 3383, 0),
                Location.create(2570, 3381, 0),
            ),
        )
    }

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)

        var line = 11

        val hasGear =
            stage >= 20 ||
                (
                    inInventory(player, PLAGUE_BOTTOM.id) &&
                            inInventory(player, PLAGUE_TOP.id)
                ) ||
                (
                    inEquipment(player, PLAGUE_BOTTOM.id) &&
                            inEquipment(player, PLAGUE_TOP.id)
                )

        val sheepDead = player.getAttribute("sheep_herder:all_dead", false)

        when {
            stage < 10 -> {
                line(player, "I can start this quest by speaking to !!Councillor Halgrive??", line++)
                line(player, "near to the !!Zoo?? in !!East Ardougne??.", line++)
            }

            stage == 10 -> {
                line(player, "!!Councillor Halgrive?? said I should speak to !!Doctor Orbon?? about", line++, hasGear)
                line(player, "Getting some protective gear.", line++, hasGear)
                line(player, "I need to !!locate the diseased sheep?? and corral them !!into the pen??", line++, sheepDead)
                line(player, "After which, I need to !!poison them?? and !!incinerate their bones??.", line++, sheepDead)

                if (sheepDead) {
                    line(player, "I should inform !!Councillor Halgrive?? that I have taken care of the problem.", line++)
                } else {
                    line(player, "I still need:", line++)
                    line(player, "A !!Red Sheep??", line++, player.getAttribute("sheep_herder:red_dead", false))
                    line(player, "A !!Blue Sheep??", line++, player.getAttribute("sheep_herder:blue_dead", false))
                    line(player, "A !!Green Sheep??", line++, player.getAttribute("sheep_herder:green_dead", false))
                    line(player, "A !!Yellow Sheep??", line++, player.getAttribute("sheep_herder:yellow_dead", false))
                }
            }

            stage == 100 -> {
                line(player, "I helped Councillor Halgrive by putting down", line++, true)
                line(player, "plague-bearing sheep.", line++, true)
                line++
                line(player, "<col=FF0000>QUEST COMPLETE!</col>", line)
            }
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10
        drawReward(player,Items.COINS_8897)
        drawReward(player, "4 Quest Points", line++)
        drawReward(player, "3100 coins", line)
        removeAttributes(player, "sheep_herder:red_dead", "sheep_herder:blue_dead", "sheep_herder:green_dead", "sheep_herder:yellow_dead", "sheep_herder:all_dead")
        addItemOrDrop(player,Items.COINS_995, 3100)
    }

    override fun newInstance(arg: Any?): Quest {
        sheepSpawns.forEach { (id, locations) ->
            locations.forEach { location ->
                HerderSheepNPC(id, location).init()
            }
        }
        return this
    }
}