package content.minigame.fistofguthix.plugin

import core.api.*
import core.cache.def.impl.ObjType
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.tools.Log
import shared.consts.Components
import shared.consts.Items

/**
 * Handles the Fist of Guthix rewards interface.
 */
class FOGRewardInterface : InterfaceListener {

    /**
     * Represents a shop stock data.
     */
    enum class FOGShopItem(val buttonId: Int, val itemId: Int, val price: Int, val amount: Int) {
        DruidicMageTop(2, Items.DRUIDIC_MAGE_TOP_100_12894, 300, 1),
        DruidicMageBottom(7, Items.DRUIDIC_MAGE_BOTTOM_100_12901, 200, 1),
        DruidicMageHood(12, Items.DRUIDIC_MAGE_HOOD_100_12887, 100, 1),
        CombatRobeTop(19, Items.COMBAT_ROBE_TOP_100_12971, 150, 1),
        CombatRobeBottom(24, Items.COMBAT_ROBE_BOTTOM_100_12978, 100, 1),
        CombatRobeHood(29, Items.COMBAT_HOOD_100_12964, 50, 1),
        BattleRobeTop(36, Items.BATTLE_ROBE_TOP_100_12873, 1500, 1),
        BattleRobeBottom(41, Items.BATTLE_ROBE_BOTTOM_100_12880, 1000, 1),
        BattleRobeHood(46, Items.BATTLE_HOOD_100_12866, 250, 1),
        GreenCoif(53, Items.GREEN_DHIDE_COIF_100_12936, 150, 1),
        BlueCoif(58, Items.BLUE_DHIDE_COIF_100_12943, 200, 1),
        RedCoif(63, Items.RED_DHIDE_COIF_100_12950, 300, 1),
        BlackCoif(68, Items.BLACK_DHIDE_COIF_100_12957, 500, 1),
        BronzeGaunt(75, Items.BRONZE_GAUNTLETS_12985, 15, 1),
        IronGaunt(80, Items.IRON_GAUNTLETS_12988, 30, 1),
        SteelGaunt(85, Items.STEEL_GAUNTLETS_12991, 50, 1),
        BlackGaunt(90, Items.BLACK_GAUNTLETS_12994, 75, 1),
        MithrilGaunt(95, Items.MITHRIL_GAUNTLETS_12997, 100, 1),
        AdamantGaunt(100, Items.ADAMANT_GAUNTLETS_13000, 150, 1),
        RuneGaunt(105, Items.RUNE_GAUNTLETS_13003, 200, 1),
        DragonGaunt(110, Items.DRAGON_GAUNTLETS_13006, 300, 1),
        AddySpike(117, Items.ADAMANT_SPIKESHIELD_100_12908, 50, 1),
        AddyBeserk(122, Items.ADAMANT_BERSERKER_SHIELD_100_12915, 100, 1),
        RuneSpike(127, Items.RUNE_SPIKESHIELD_100_12922, 200, 1),
        RuneBeserk(132, Items.RUNE_BERSERKER_SHIELD_100_12929, 300, 1),
        IritGloves(139, Items.IRIT_GLOVES_12856, 75, 1),
        AvantoeGloves(144, Items.AVANTOE_GLOVES_12857, 100, 1),
        KwuarmGloves(149, Items.KWUARM_GLOVES_12858, 200, 1),
        CadantineGloves(154, Items.CADANTINE_GLOVES_12859, 200, 1),
        SwordfishGloves(161, Items.SWORDFISH_GLOVES_12860, 200, 1),
        SharkGloves(166, Items.SHARK_GLOVES_12861, 200, 1),
        DragonGloves(171, Items.DRAGON_SLAYER_GLOVES_12862, 200, 1),
        AirGloves(176, Items.AIR_RUNECRAFTING_GLOVES_12863, 75, 1),
        WaterGloves(181, Items.WATER_RUNECRAFTING_GLOVES_12864, 75, 1),
        EarthGloves(186, Items.EARTH_RUNECRAFTING_GLOVES_12865, 75, 1);

        companion object {
            private val map = values().associateBy(FOGShopItem::buttonId)
            /**
             * Gets the shop item by interface button id.
             *
             * @param button The button id.
             * @return item.
             */
            fun fromButton(button: Int) = map[button]
        }
    }

    override fun defineInterfaceListeners() {
        on(Components.FOG_REWARDS_732) { player, _, opcode, button, _, _ ->
            val stock = FOGShopItem.fromButton(button) ?: run {
                log(this::class.java, Log.WARN, "Unhandled button ID for FOG interface: $button")
                return@on true
            }

            val action: (() -> Unit)? = when {
                button >= 139 && opcode == 124 -> { { sendMessage(player, ObjType.forId(stock.itemId).examine.replace("100", "")) } }
                opcode in listOf(196, 124, 199) -> { { handlePurchase(player, stock, opcode) } }
                opcode == 155 -> { { sendMessage(player, "${getItemName(stock.itemId).replace("100", "")} costs ${stock.price} tokens.") } }
                opcode == 234 -> { { sendMessage(player, ObjType.forId(stock.itemId).examine.replace("100", "")) } }
                else -> null
            }

            action?.invoke()
            return@on true
        }
    }

    /**
     * Handles the purchase of a shop item.
     *
     * @param player The player buying the item.
     * @param stock The shop item being purchased.
     * @param opcode The purchase type (1x, 5x, 10x).
     */
    private fun handlePurchase(player: Player, stock: FOGShopItem, opcode: Int) {
        val multiplier = when (opcode) {
            196 -> 1
            124 -> 5
            199 -> 10
            else -> return
        }

        val priceItem = Item(Items.FIST_OF_GUTHIX_TOKEN_12852, stock.price * multiplier)

        if (!player.inventory.containsItem(priceItem)) {
            sendMessage(player, "You don't have enough Fist of Guthix tokens.")
            return
        }

        if (!hasSpaceFor(player, Item(stock.itemId, multiplier))) {
            sendMessage(player, "You don't have enough space in your inventory.")
            return
        }

        removeItem(player, priceItem)
        addItem(player, stock.itemId, multiplier)
    }
}
