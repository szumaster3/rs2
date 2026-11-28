package content.region.misthalin.silvarea.quest.rag.npc

import content.data.Quests

import core.api.isQuestInProgress
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs

class MonkeyNPC : NPCBehavior(*monkeyIds) {
    companion object {
        private val monkeyIds =
            intArrayOf(
                NPCs.MONKEY_132,
                NPCs.MM_ZOO_MONKEY_1463,
                NPCs.MM_FREED_ZOO_MONKEY_1464,
                NPCs.MM_TRANSMOGRIFICATION_NORMAL_MONKEY_1487,
                NPCs.MAGIC_CARPET_MONKEY_2301,
                NPCs.MONKEY_2302,
                NPCs.MONKEY_2303,
                NPCs.HARMLESS_ISLAND_MONKEY_4344,
                NPCs.BREW_MONKEY_4363,
                NPCs.MONKEY_5852,
                NPCs.SUROK_MAGIS_5853,
                NPCs.LORE_MONKEY_1_6943,
                NPCs.LORE_MONKEY_2_7211,
                NPCs.LORE_MONKEY_3_7213,
                NPCs.LORE_MONKEY_4_7215,
                NPCs.LORE_MONKEY_5_7217,
                NPCs.LORE_MONKEY_6_7219,
                NPCs.LORE_MONKEY_7_7221,
                NPCs.LORE_MONKEY_8_7223,
                NPCs.LORE_MONKEY_9_7225,
                NPCs.LORE_MONKEY_10_7227,
                NPCs.MM_ZOMBIE_MONKEY_LARGE_1465,
                NPCs.MM_ZOMBIE_MONKEY_LARGE_GUARD_1466,
                NPCs.MM_ZOMBIE_MONKEY_SMALL_1467,
            )
    }

    override fun onDropTableRolled(
        self: NPC,
        killer: Entity,
        drops: ArrayList<Item>,
    ) {
        super.onDropTableRolled(self, killer, drops)

        if (killer is Player && isQuestInProgress(killer, Quests.RAG_AND_BONE_MAN.questName, 1, 99)) {
            if (RandomFunction.roll(4)) {
                drops.add(Item(Items.MONKEY_PAW_7854))
            }
        }
    }
}
