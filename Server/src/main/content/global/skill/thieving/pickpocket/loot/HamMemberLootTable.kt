package content.global.skill.thieving.pickpocket.loot

import core.api.utils.WeightBasedTable
import core.api.utils.WeightedItem
import shared.consts.Items
import shared.consts.NPCs

object HamMemberLootTable {

    val NPC_ID_MALE = intArrayOf(NPCs.H_A_M_MEMBER_1714)
    val NPC_ID_FEMALE = intArrayOf(NPCs.H_A_M_MEMBER_1715)

    val LOOT = WeightBasedTable.create(
        WeightedItem(Items.H_A_M_BOOTS_4310, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_CLOAK_4304, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_GLOVES_4308, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_HOOD_4302, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_LOGO_4306, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_ROBE_4300, 1, 1, 102.0),
        WeightedItem(Items.H_A_M_SHIRT_4298, 1, 1, 102.0),
        WeightedItem(Items.BRONZE_ARROW_882, 1, 13, 34.0),
        WeightedItem(Items.BRONZE_HATCHET_1351, 1, 1, 34.0),
        WeightedItem(Items.BRONZE_DAGGER_1205, 1, 1, 34.0),
        WeightedItem(Items.BRONZE_PICKAXE_1265, 1, 1, 34.0),
        WeightedItem(Items.IRON_HATCHET_1349, 1, 1, 34.0),
        WeightedItem(Items.IRON_DAGGER_P_1219, 1, 1, 34.0),
        WeightedItem(Items.IRON_PICKAXE_1267, 1, 1, 34.0),
        WeightedItem(Items.LEATHER_BODY_1129, 1, 1, 34.0),
        WeightedItem(Items.STEEL_ARROW_886, 1, 13, 51.0),
        WeightedItem(Items.STEEL_HATCHET_1353, 1, 1, 51.0),
        WeightedItem(Items.STEEL_DAGGER_1207, 1, 1, 51.0),
        WeightedItem(Items.STEEL_PICKAXE_1269, 1, 1, 51.0),
        WeightedItem(Items.COINS_995, 1, 21, 6.0),
        WeightedItem(Items.BUTTONS_688, 1, 1, 25.5),
        WeightedItem(Items.DAMAGED_ARMOUR_697, 1, 1, 25.5),
        WeightedItem(Items.RUSTY_SWORD_686, 1, 1, 25.5),
        WeightedItem(Items.FEATHER_314, 1, 7, 34.0),
        WeightedItem(Items.LOGS_1511, 1, 1, 34.0),
        WeightedItem(Items.THREAD_1734, 1, 10, 34.0),
        WeightedItem(Items.COWHIDE_1739, 1, 1, 34.0),
        WeightedItem(Items.KNIFE_946, 1, 1, 51.0),
        WeightedItem(Items.NEEDLE_1733, 1, 1, 51.0),
        WeightedItem(Items.RAW_ANCHOVIES_321, 1, 1, 51.0),
        WeightedItem(Items.RAW_CHICKEN_2138, 1, 1, 51.0),
        WeightedItem(Items.TINDERBOX_590, 1, 1, 51.0),
        WeightedItem(Items.UNCUT_OPAL_1625, 1, 1, 51.0),
        WeightedItem(Items.COAL_453, 1, 1, 51.0),
        WeightedItem(Items.IRON_ORE_441, 1, 1, 51.0),
        WeightedItem(Items.UNCUT_JADE_1627, 1, 1, 51.0),
        WeightedItem(Items.GRIMY_GUAM_199, 1, 1, 93.5),
        WeightedItem(Items.GRIMY_MARRENTILL_201, 1, 1, 187.0),
        WeightedItem(Items.GRIMY_TARROMIN_203, 1, 1, 280.5)
    )
}