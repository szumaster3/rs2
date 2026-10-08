package content.region.misthalin.draynor.wizardstower.rc_guild.plugin

import core.api.*
import core.game.dialogue.InputType
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components
import shared.consts.Items

class ElrissStorePlugin : InterfaceListener {

    private companion object {
        const val SELECTION = "rc-selection"
        const val OP_SELECT = 155
        const val OP_BUY_X = 196
        const val CONFIRM_BUTTON = 163
        const val TOKENS_COMPONENT = 135
        const val SELECTED_COMPONENT = 136
        const val TOKEN = Items.RUNECRAFTING_GUILD_TOKEN_13650
    }

    override fun defineInterfaceListeners() {

        /*
         * Handles the opening of the RC Guild Rewards interface.
         */

        onOpen(Components.RCGUILD_REWARDS_779) { player, _ ->
            sendTokens(player)
            return@onOpen true
        }

        /*
         * Handles interaction with the RC Guild Rewards interface.
         */

        on(Components.RCGUILD_REWARDS_779) { player, _, opcode, button, _, _ ->
            val stock = ElrissStock.fromButton(button)
            when {
                opcode == OP_SELECT && button == CONFIRM_BUTTON -> confirmPurchase(player)
                opcode == OP_SELECT && stock != null -> select(player, stock, 1)
                opcode == OP_BUY_X && stock != null ->
                    sendInputDialogue(player, InputType.AMOUNT, "Enter the amount to buy:") { value ->
                        val amount = value.toString().toIntOrNull()
                        if (amount == null || amount <= 0) {
                            sendDialogue(player, "Please enter a valid amount greater than zero.")
                        } else {
                            select(player, stock, amount)
                        }
                    }
            }
            return@on true
        }

        onClose(Components.RCGUILD_REWARDS_779) { player, _ ->
            player.removeAttribute(SELECTION)
            return@onClose true
        }
    }

    private fun select(player: Player, stock: ElrissStock, amount: Int) {
        player.setAttribute(SELECTION, Selection(stock, amount))
        sendString(player, "${getItemName(stock.itemId)}($amount)", Components.RCGUILD_REWARDS_779, SELECTED_COMPONENT)
    }

    private fun confirmPurchase(player: Player) {
        val (stock, amount) = player.getAttribute<Selection>(SELECTION) ?: run {
            sendMessage(player, "You must select something to buy before you can confirm your purchase.")
            return
        }

        val reward = Item(stock.itemId, amount)
        if (!player.inventory.hasSpaceFor(reward)) {
            sendMessage(player, "You don't have enough space in your inventory.")
            return
        }

        val cost = stock.price.toLong() * amount
        val tokens = Item(TOKEN, cost.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        if (cost > Int.MAX_VALUE || !player.inventory.containsItem(tokens)) {
            sendMessage(player, "You don't have enough tokens to purchase that.")
            return
        }

        player.inventory.remove(tokens)
        player.inventory.add(reward)
        player.removeAttribute(SELECTION)
        sendMessage(player, "Your purchase has been added to your inventory.")
        sendString(player, " ", Components.RCGUILD_REWARDS_779, SELECTED_COMPONENT)
        sendTokens(player)
    }

    private fun sendTokens(player: Player) {
        sendString(player, "Tokens: ${amountInInventory(player, TOKEN)}", Components.RCGUILD_REWARDS_779, TOKENS_COMPONENT)
    }
}

private data class Selection(val stock: ElrissStock, val amount: Int)

private enum class ElrissStock(val buttonId: Int, val itemId: Int, val price: Int) {
    AIR_TALISMAN(6,     Items.AIR_TALISMAN_1438,    50),
    MIND_TALISMAN(13,   Items.MIND_TALISMAN_1448,   50),
    WATER_TALISMAN(15,  Items.WATER_TALISMAN_1444,  50),
    EARTH_TALISMAN(10,  Items.EARTH_TALISMAN_1440,  50),
    FIRE_TALISMAN(11,   Items.FIRE_TALISMAN_1442,   50),
    BODY_TALISMAN(7,    Items.BODY_TALISMAN_1446,   50),
    COSMIC_TALISMAN(9,  Items.COSMIC_TALISMAN_1454, 125),
    CHAOS_TALISMAN(8,   Items.CHAOS_TALISMAN_1452,  125),
    NATURE_TALISMAN(14, Items.NATURE_TALISMAN_1462, 125),
    LAW_TALISMAN(12,    Items.LAW_TALISMAN_1458,    125),

    BLUE_RC_HAT(36,     Items.RUNECRAFTER_HAT_13626,    1000),
    YELLOW_RC_HAT(37,   Items.RUNECRAFTER_HAT_13616,    1000),
    GREEN_RC_HAT(38,    Items.RUNECRAFTER_HAT_13621,    1000),
    BLUE_RC_ROBE(39,    Items.RUNECRAFTER_ROBE_13624,   1000),
    YELLOW_RC_ROBE(40,  Items.RUNECRAFTER_ROBE_13614,   1000),
    GREEN_RC_ROBE(41,   Items.RUNECRAFTER_ROBE_13619,   1000),
    BLUE_RC_BOTTOM(42,  Items.RUNECRAFTER_SKIRT_13627,  1000),
    YELLOW_RC_BOTTOM(43,Items.RUNECRAFTER_SKIRT_13617,  1000),
    GREEN_RC_BOTTOM(44, Items.RUNECRAFTER_SKIRT_13622,  1000),
    BLUE_RC_GLOVES(45,  Items.RUNECRAFTER_GLOVES_13628, 1000),
    YELLOW_RC_GLOVES(46,Items.RUNECRAFTER_GLOVES_13618, 1000),
    GREEN_RC_GLOVES(47, Items.RUNECRAFTER_GLOVES_13623, 1000),

    RC_STAFF(114,       Items.RUNECRAFTING_STAFF_13629, 10000),
    PURE_ESSENCE(115,   Items.PURE_ESSENCE_7937,        100),

    AIR_TABLET(72,      Items.AIR_ALTAR_TP_13599,       30),
    MIND_TABLET(80,     Items.MIND_ALTAR_TP_13600,      32),
    WATER_TABLET(83,    Items.WATER_ALTAR_TP_13601,     34),
    EARTH_TABLET(77,    Items.EARTH_ALTAR_TP_13602,     36),
    FIRE_TABLET(78,     Items.FIRE_ALTAR_TP_13603,      37),
    BODY_TABLET(73,     Items.BODY_ALTAR_TP_13604,      38),
    COSMIC_TABLET(75,   Items.COSMIC_ALTAR_TP_13605,    39),
    CHAOS_TABLET(74,    Items.CHAOS_ALTAR_TP_13606,     40),
    ASTRAL_TABLET(81,   Items.ASTRAL_ALTAR_TP_13611,    41),
    NATURE_TABLET(82,   Items.NATURE_ALTAR_TP_13607,    42),
    LAW_TABLET(79,      Items.LAW_ALTAR_TP_13608,       43),
    DEATH_TABLET(76,    Items.DEATH_ALTAR_TP_13609,     44),
    BLOOD_TABLET(84,    Items.BLOOD_ALTAR_TP_13610,     45),
    GUILD_TABLET(85,    Items.RUNECRAFTING_GUILD_TP_13598, 15);

    companion object {
        private val byButton = values().associateBy { it.buttonId }
        fun fromButton(id: Int): ElrissStock? = byButton[id]
    }
}