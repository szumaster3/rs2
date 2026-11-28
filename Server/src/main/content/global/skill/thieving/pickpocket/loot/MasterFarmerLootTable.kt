package content.global.skill.thieving.pickpocket.loot

import core.api.utils.WeightBasedTable
import core.api.utils.WeightedItem
import shared.consts.Items
import shared.consts.NPCs

object MasterFarmerLootTable {

    val NPC_ID = intArrayOf(
        NPCs.MASTER_FARMER_1_2234,
        NPCs.MASTER_FARMER_2_2235,
        NPCs.MARTIN_THE_MASTER_FARMER_3299
    )

    val LOOT = WeightBasedTable.create(
        WeightedItem(Items.POTATO_SEED_5318, 1, 3, 50.0),
        WeightedItem(Items.ONION_SEED_5319, 1, 3, 50.0),
        WeightedItem(Items.CABBAGE_SEED_5324, 1, 3, 50.0),
        WeightedItem(Items.TOMATO_SEED_5322, 1, 2, 50.0),
        WeightedItem(Items.SWEETCORN_SEED_5320, 1, 2, 50.0),
        WeightedItem(Items.STRAWBERRY_SEED_5323, 1, 1, 25.0),
        WeightedItem(Items.WATERMELON_SEED_5321, 1, 1, 8.0),
        WeightedItem(Items.BARLEY_SEED_5305, 1, 4, 50.0),
        WeightedItem(Items.HAMMERSTONE_SEED_5307, 1, 3, 50.0),
        WeightedItem(Items.ASGARNIAN_SEED_5308, 1, 3, 50.0),
        WeightedItem(Items.JUTE_SEED_5306, 1, 3, 50.0),
        WeightedItem(Items.YANILLIAN_SEED_5309, 1, 2, 25.0),
        WeightedItem(Items.KRANDORIAN_SEED_5310, 1, 2, 25.0),
        WeightedItem(Items.WILDBLOOD_SEED_5311, 1, 1, 8.0),
        WeightedItem(Items.MARIGOLD_SEED_5096, 1, 1, 50.0),
        WeightedItem(Items.NASTURTIUM_SEED_5098, 1, 1, 50.0),
        WeightedItem(Items.ROSEMARY_SEED_5097, 1, 1, 50.0),
        WeightedItem(Items.WOAD_SEED_5099, 1, 1, 50.0),
        WeightedItem(Items.LIMPWURT_SEED_5100, 1, 1, 25.0),
        WeightedItem(Items.REDBERRY_SEED_5101, 1, 1, 50.0),
        WeightedItem(Items.CADAVABERRY_SEED_5102, 1, 1, 50.0),
        WeightedItem(Items.DWELLBERRY_SEED_5103, 1, 1, 25.0),
        WeightedItem(Items.JANGERBERRY_SEED_5104, 1, 1, 25.0),
        WeightedItem(Items.WHITEBERRY_SEED_5105, 1, 1, 25.0),
        WeightedItem(Items.GUAM_SEED_5291, 1, 1, 50.0),
        WeightedItem(Items.MARRENTILL_SEED_5292, 1, 1, 50.0),
        WeightedItem(Items.TARROMIN_SEED_5293, 1, 1, 50.0),
        WeightedItem(Items.HARRALANDER_SEED_5294, 1, 1, 25.0),
        WeightedItem(Items.RANARR_SEED_5295, 1, 1, 8.0),
        WeightedItem(Items.TOADFLAX_SEED_5296, 1, 1, 8.0),
        WeightedItem(Items.IRIT_SEED_5297, 1, 1, 8.0),
        WeightedItem(Items.AVANTOE_SEED_5298, 1, 1, 8.0),
        WeightedItem(Items.KWUARM_SEED_5299, 1, 1, 8.0),
        WeightedItem(Items.SNAPDRAGON_SEED_5300, 1, 1, 5.0),
        WeightedItem(Items.CADANTINE_SEED_5301, 1, 1, 8.0),
        WeightedItem(Items.LANTADYME_SEED_5302, 1, 1, 5.0),
        WeightedItem(Items.DWARF_WEED_SEED_5303, 1, 1, 5.0),
        WeightedItem(Items.TORSTOL_SEED_5304, 1, 1, 5.0)
    )

}
