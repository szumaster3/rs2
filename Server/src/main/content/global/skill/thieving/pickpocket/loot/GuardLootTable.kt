package content.global.skill.thieving.pickpocket.loot

import core.api.utils.WeightBasedTable
import core.api.utils.WeightedItem
import shared.consts.Items
import shared.consts.NPCs

object GuardLootTable {

    val NPC_ID = intArrayOf(
        NPCs.GUARD1_9,
        NPCs.ARDOUGNE_GUARD_32,
        NPCs.EDGE_GUARD1_296,
        NPCs.EDGE_GUARD2_297,
        NPCs.EDGE_GUARD3_298,
        NPCs.EDGE_GUARD4_299,
        NPCs.JAIL_GUARD_1_2699,
        NPCs.JAIL_GUARD_2_2700,
        NPCs.JAIL_GUARD_3_2701,
        NPCs.JAIL_GUARD_4_2702,
        NPCs.JAIL_GUARD_5_2703,
        NPCs.FAI_FALADOR_GUARD1_3228,
        NPCs.FAI_FALADOR_GUARD2_3229,
        NPCs.FAI_FALADOR_GUARD3_3230,
        NPCs.FAI_FALADOR_GUARD4_3231,
        NPCs.FAI_FALADOR_GUARD5_3232,
        NPCs.FAI_FALADOR_GUARD6_3233,
        NPCs.FALADOR_DORIC_AREA_GUARD_3241,
        NPCs.EDGE_GUARD5_3407,
        NPCs.EDGE_GUARD6_3408,
        NPCs.DTTD_HAM_GUARD_1_POSTQUEST_4307,
        NPCs.DTTD_HAM_GUARD_2_POSTQUEST_4308,
        NPCs.DTTD_HAM_GUARD_3_POSTQUEST_4309,
        NPCs.DTTD_HAM_GUARD_4_POSTQUEST_4310,
        NPCs.DTTD_HAM_GUARD_5_POSTQUEST_4311,
        NPCs.FAI_VARROCK_GUARD_5919,
        NPCs.FAI_VARROCK_GUARD_CAPTAIN_5920,
        NPCs.ZEMO_FAI_VARROCK_GUARD_8173
    )

    val LOOT = WeightBasedTable.create(
        WeightedItem(Items.COINS_995, 30, 30, 1.0, true)
    )
}