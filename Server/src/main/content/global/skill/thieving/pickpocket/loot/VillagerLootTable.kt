package content.global.skill.thieving.pickpocket.loot

import core.api.utils.WeightBasedTable
import core.api.utils.WeightedItem
import shared.consts.Items
import shared.consts.NPCs

object VillagerLootTable {

    val NPC_ID = intArrayOf(
        NPCs.FEUD_VILLAGER_1_1_1888,
        NPCs.FEUD_VILLAGER_1_2_1889,
        NPCs.FEUD_VILLAGER_1_3_1890,
        NPCs.FEUD_VILLAGER_2_1_1892,
        NPCs.FEUD_VILLAGER_2_2_1893,
        NPCs.FEUD_VILLAGER_2_3_1894,
        NPCs.FEUD_VILLAGER_3_1_1896,
        NPCs.FEUD_VILLAGER_3_2_1897,
        NPCs.FEUD_VILLAGER_3_3_1898,
    )

    val LOOT = WeightBasedTable.create(
        WeightedItem(Items.COINS_995, 5, 5, 1.0, true)
    )
}