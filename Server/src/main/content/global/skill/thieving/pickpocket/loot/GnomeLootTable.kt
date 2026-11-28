package content.global.skill.thieving.pickpocket.loot

import core.api.utils.WeightBasedTable
import core.api.utils.WeightedItem
import shared.consts.Items
import shared.consts.NPCs

object GnomeLootTable {

    val NPC_ID = intArrayOf(
        NPCs.GNOME_66,
        NPCs.BROWCLOTHEDGNOME_67,
        NPCs.DARKSKINNED_GNOME_68,
        NPCs.GNOMEFEMALE_168,
        NPCs.GNOMEFEMALE_DSKINNED_169,
        NPCs.BATTLEFIELD_GNOME_TROOP_2249,
        NPCs.BATTLEFIELD_DARKSKINNED_GNOME_ARCHER_2250,
        NPCs.BATTLEFIELD_GNOME_ARCHER_2251,
        NPCs.MOURNER_HIDEOUT_GNOME_HEAD_2371,
        NPCs.GNOME_2649,
        NPCs.GNOME_2650,
        NPCs.GRIM_GNOME_INCAGE_1_6002,
        NPCs.GRIM_GNOME_INCAGE_3_6004
    )

    val LOOT = WeightBasedTable.create(
        WeightedItem(Items.ARROW_SHAFT_52, 2, 4, 56.0),
        WeightedItem(Items.COINS_995, 300, 300, 30.0),
        WeightedItem(Items.SWAMP_TOAD_2150, 1, 1, 24.0),
        WeightedItem(Items.GOLD_ORE_444, 1, 1, 8.0),
        WeightedItem(Items.EARTH_RUNE_557, 1, 1, 5.0),
        WeightedItem(Items.KING_WORM_2162, 1, 1, 3.0),
        WeightedItem(Items.FIRE_ORB_569, 1, 1, 2.0)
    ).insertMediumClue(2.0)
}