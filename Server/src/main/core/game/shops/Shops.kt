package core.game.shops

import content.data.Quests

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import content.global.skill.crafting.CraftingDefinition
import core.ServerConstants
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.dialogue.InputType
import core.game.ge.GrandExchange
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.system.command.Privilege
import core.game.world.GameWorld
import core.tools.Log
import core.tools.secondsToTicks
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs
import java.io.FileReader

/**
 * A class that handles shop-related logic.
 */
class Shops : StartupListener, TickListener, InteractionListener, InterfaceListener, Commands {
    companion object {
        /**
         * The key used to store personalized shop settings in the server configuration.
         */
        @JvmStatic
        val personalizedShops = "world.personalized_shops"

        /**
         * A map of shops indexed by their unique id.
         */
        @JvmStatic
        val shopsById = HashMap<Int, Shop>()

        /**
         * A map of shops indexed by the NPC ID associated with the shop.
         */
        @JvmStatic
        val shopsByNpc = HashMap<Int, Shop>()

        private var lastPlayerStockClear = 0

        /**
         * Opens the shop for the given player by the shop id.
         *
         * @param player The player opening the shop.
         * @param id The unique identifier of the shop to open.
         */
        @JvmStatic
        fun openId(
            player: Player,
            id: Int,
        ) {
            shopsById[id]?.openFor(player)
        }

        /**
         * Logs a message specific to the shop system.
         *
         * @param msg The message to log.
         */
        fun logShop(msg: String) {
            log(this::class.java, Log.FINE, "[SHOPS] $msg")
        }

        /**
         * Parses the stock data from a string and converts it into a list of `ShopItem` objects.
         *
         * @param stock The raw stock string to parse.
         * @param id The shop id to log any potential issues with the stock.
         * @return A list of `ShopItem` objects representing the parsed stock.
         */
        fun parseStock(
            stock: String,
            id: Int,
        ): ArrayList<ShopItem> {
            val items = ArrayList<ShopItem>()
            val idsInStock = HashMap<Int, Boolean>()
            if (stock.isEmpty()) {
                return items
            }
            stock.split('-').map {
                try {
                    val tokens = it.replace("{", "").replace("}", "").split(",".toRegex()).toTypedArray()
                    var amount = tokens[1].trim()
                    if (amount == "inf") {
                        amount = "-1"
                    }
                    val item = tokens[0].toInt()
                    if (idsInStock[item] != null) {
                        log(this::class.java, Log.WARN, "[SHOPS] MALFORMED STOCK IN SHOP ID [$id] FOR ITEM $item")
                        items.forEach {
                            if (it.itemId == item) {
                                it.amount += amount.toInt()
                                return@map
                            }
                        }
                    } else {
                        val restockRate = tokens.getOrNull(2)?.toIntOrNull() ?: 100
                        items.add(ShopItem(item, amount.toInt(), restockRate))
                        idsInStock[item] = true
                    }
                } catch (e: Exception) {
                    log(this::class.java, Log.WARN, "[SHOPS] MALFORMED STOCK IN SHOP ID [$id] FOR ITEM $it")
                    throw e
                }
            }
            return items
        }
    }

    /**
     * Initializes the shops by loading their data from a JSON configuration file.
     */
    override fun startup() {
        val path = ServerConstants.CONFIG_PATH + "shops.json"
        logShop("Using JSON path: $path")

        FileReader(path).use { reader ->
            val gson = Gson()
            val shopListType = object : TypeToken<List<JsonObject>>() {}.type
            val data: List<JsonObject> = gson.fromJson(reader, shopListType)

            data.forEach { shopData ->
                val id = shopData.get("id").asInt
                val title = shopData.get("title").asString
                val general = shopData.get("general_store").asBoolean
                val stockString = shopData.get("stock").asString
                val stock = parseStock(stockString, id)
                val npcs = shopData.get("npcs")?.asString
                    ?.split(",")
                    ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                val currency = shopData.get("currency").asInt
                val highAlch = shopData.get("high_alch").asString == "1"
                val forceShared = shopData.get("force_shared")?.asString?.toBoolean() ?: false

                val shop = Shop(title, stock.toTypedArray(), general, currency, highAlch, forceShared)
                npcs.forEach { shopsByNpc[it] = shop }
                shopsById[id] = shop
            }
        }

        logShop("Parsed ${shopsById.size} shops.")
    }

    /**
     * Executes every tick. Manages shop restocking and player stock clearing
     * at defined intervals.
     */
    override fun tick() {
        shopsById.values.forEach(Shop::restock)

        val playerStockClearInterval = secondsToTicks(ServerConstants.PLAYER_STOCK_CLEAR_INTERVAL * 60)
        if (getWorldTicks() % playerStockClearInterval == 0) {
            val clearToGe = ServerConstants.PLAYER_STOCK_RECIRCULATE
            if (clearToGe) {
                Shop.generalPlayerStock.toArray().filterNotNull().forEach {
                    GrandExchange.addBotOffer(it.id, it.amount)
                }
            }
            Shop.generalPlayerStock.clear()
        }
    }

    override fun defineListeners() {
        on(IntType.NPC, "trade", "shop") { player, node ->
            val npc = node as NPC
            if (npc.id == NPCs.ELLIS_TANNER_2824 || npc.id == NPCs.SBOTT_1041 || npc.id == NPCs.TANNER_804) {
                CraftingDefinition.Tan.open(player, npc.id)
                return@on true
            }
            if (npc.id == NPCs.REGGIE_7601) {
                openInterface(player, Components.FOG_REWARDS_732)
                return@on true
            }
            shopsByNpc[npc.id]?.openFor(player) ?: return@on false
            return@on true
        }

        on(NPCs.SIEGFRIED_ERKLE_933, IntType.NPC, "trade") { player, node ->
            val points = getQuestPoints(player)
            if (!GameWorld.settings!!.isMembers) {
                sendNPCDialogueLines(
                    player,
                    NPCs.SIEGFRIED_ERKLE_933,
                    FaceAnim.HALF_GUILTY,
                    false,
                    "I'm sorry but serves of this shop are only for the",
                    "pleasure of those who are rightful members of the",
                    "Legends Guild. I would get into serious trouble if I sold",
                    "a non-member an item from this store.",
                )
            } else if (points < 40) {
                sendNPCDialogueLines(
                    player,
                    NPCs.SIEGFRIED_ERKLE_933,
                    FaceAnim.HALF_GUILTY,
                    false,
                    "I'm sorry but serves of this shop are only for the",
                    "pleasure of those who are rightful members of the",
                    "Legends Guild.",
                )
            } else {
                shopsByNpc[node.id]?.openFor(player)
            }
            return@on true
        }

        on(NPCs.VIKING_FUR_MONGER_1316, IntType.NPC, "trade") { player, node ->
            if (!isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS.questName)) {
                sendMessage(player, "Only Fremenniks may purchase furs here.")
                return@on true
            }
            shopsByNpc[node.id]?.openFor(player)
            return@on true
        }

        on(NPCs.CANDLE_MAKER_562, IntType.NPC, "trade") { player, node ->
            if (getQuestStage(player, Quests.MERLINS_CRYSTAL.questName) > 60) {
                openId(player, 56)
            } else {
                shopsByNpc[node.id]?.openFor(player)
            }
            return@on true
        }

        on(NPCs.OZIACH_747, IntType.NPC, "trade") { player, node ->
            val questStage = getQuestStage(player, Quests.DRAGON_SLAYER.questName)
            when (questStage) {
                0 -> sendNPCDialogue(player, node.id, "I ain't got nothing to sell ye, adventurer. Leave me be!", FaceAnim.ANNOYED)
                in 1..99 -> sendNPCDialogue(player, node.id, "I'm not sellin' ye anything till you've slayed that dragon! Now be off wi' ye.", FaceAnim.ANNOYED)
                else -> shopsByNpc[node.id]?.openFor(player)
            }
            return@on true
        }
    }

    override fun defineInterfaceListeners() {
        // Compatibility path only: the 578 main shop layout is CS2 2716/2723, not this historic grid.
        // Keep the existing stock model until a verified per-shop InvType mapping is available.
        onNative(Components.SHOP_TEMPLATE_620) { player, component, operation, buttonID, slot, itemId ->
            val shop = getAttribute<Shop?>(player, "shop", null) ?: return@onNative true
            if (player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_620) !== component || component.isHidden) return@onNative true
            val isMainStock = getAttribute(player, "shop-main", true)
            val main = getAttribute<core.game.container.Container?>(player, "shop-cont", null) ?: return@onNative true
            // Legacy tabs are not native 578 buttons. Do not interpret clicks on 578 stock/key widgets as transactions.
            if (operation == InterfaceOperation.OP1 && slot == -1 && buttonID in 25..26) {
                if (buttonID == 25 || shop.general) shop.showTab(player, buttonID == 25)
                return@onNative true
            }
            if (buttonID != (if (isMainStock) 23 else 24)) return@onNative true
            val source = if (isMainStock) main else shop.playerStock
            val item = source[slot] ?: return@onNative true
            if (item.id != itemId) return@onNative true
            if (operation == InterfaceOperation.OP10) {
                sendMessage(player, item.definition.examine)
                return@onNative true
            }
            if (operation !in InterfaceOperation.OP1..InterfaceOperation.OP5) return@onNative true
            val session = player.session
            fun buy(amount: Int) {
                if (amount <= 0 || player.session !== session || component.isHidden ||
                    player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_620) !== component ||
                    getAttribute<Shop?>(player, "shop", null) !== shop ||
                    getAttribute<core.game.container.Container?>(player, "shop-cont", null) !== main ||
                    getAttribute(player, "shop-main", true) != isMainStock || source[slot] !== item || item.id != itemId) return
                shop.buy(player, slot, amount)
            }
            when (operation) {
                InterfaceOperation.OP1 -> {
                    val price = shop.getBuyPrice(player, slot)
                    if (price.amount >= 0 && price.id != 65535)
                        sendMessage(player, "${item.name}: This item currently costs ${price.amount} ${price.name.lowercase()}.")
                }
                InterfaceOperation.OP2 -> buy(1)
                InterfaceOperation.OP3 -> buy(5)
                InterfaceOperation.OP4 -> buy(10)
                InterfaceOperation.OP5 -> sendInputDialogue(player, InputType.AMOUNT, "Enter the amount to buy:") { value ->
                    (value as? Int)?.let { buy(it) }
                }
            }
            true
        }

        onOpen(Components.SHOP_TEMPLATE_SIDE_621) { player, _ ->
            val settings = IfaceSettingsBuilder().enableOptions(0..4).enableOption(9).build()
            player.packetDispatch.sendIfaceSettings(settings, 0, Components.SHOP_TEMPLATE_SIDE_621, 0, 27)
            player.packetDispatch.sendRunScript(
                150,
                "IviiiIsssssssss",
                "",
                "",
                "",
                "",
                "Sell X",
                "Sell 10",
                "Sell 5",
                "Sell 1",
                "Value",
                -1,
                0,
                7,
                4,
                93,
                Components.SHOP_TEMPLATE_SIDE_621 shl 16,
            )
            return@onOpen true
        }

        onClose(Components.SHOP_TEMPLATE_620) { player, _ ->
            val shop = getAttribute<Shop?>(player, "shop", null)
            val listener = Shop.listenerInstances[player.details.uid]
            if (listener != null && listener.player === player) {
                getAttribute<core.game.container.Container?>(player, "shop-cont", null)?.listeners?.remove(listener)
                shop?.playerStock?.listeners?.remove(listener)
            }
            removeAttribute(player, "shop")
            removeAttribute(player, "shop-cont")
            removeAttribute(player, "shop-main")
            if (player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_SIDE_621) != null)
                player.interfaceManager.closeSingleTab()
            return@onClose true
        }

        onNative(Components.SHOP_TEMPLATE_SIDE_621) { player, component, opcode, button, slot, itemId ->
            val opValue = InterfaceOperation.OP1
            val sellOne = InterfaceOperation.OP2
            val sellFive = InterfaceOperation.OP3
            val sellTen = InterfaceOperation.OP4
            val sellX = InterfaceOperation.OP5

            val shop = getAttribute<Shop?>(player, "shop", null) ?: return@onNative false

            val session = player.session
            val main = player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_620) ?: return@onNative true
            if (main.isHidden || button != 0 || slot !in 0 until player.inventory.capacity()) return@onNative true
            val itemInSlot = player.inventory[slot]
            if (itemInSlot == null) {
                sendMessage(player, "That item doesn't appear to be there anymore. Please try again.")
                return@onNative true
            }

            if (itemInSlot.id != itemId) return@onNative true
            if (opcode == InterfaceOperation.OP10) {
                sendMessage(player, itemInSlot.definition.examine)
                return@onNative true
            }
            if (opcode !in InterfaceOperation.OP1..InterfaceOperation.OP5) return@onNative true
            val (_, price) = shop.getSellPrice(player, slot)
            val def = itemDefinition(player.inventory[slot].id)

            val valueMsg = when {
                (price.amount == -1) || !def.hasShopCurrencyValue(price.id) || def.id in intArrayOf(
                    Items.COINS_995, Items.TOKKUL_6529, Items.ARCHERY_TICKET_1464, Items.CASTLE_WARS_TICKET_4067
                ) -> "This shop will not buy that item."

                else -> "${player.inventory[slot].name}: This shop will buy this item for ${price.amount} ${price.name.lowercase()}."
            }

            when (opcode) {
                opValue -> sendMessage(player, valueMsg)
                sellOne -> shop.sell(player, slot, 1)
                sellFive -> shop.sell(player, slot, 5)
                sellTen -> shop.sell(player, slot, 10)
                sellX -> sendInputDialogue(player, InputType.AMOUNT, "Enter the amount to sell:") { value ->
                    val amt = value as? Int ?: return@sendInputDialogue
                    if (amt <= 0 || player.session !== session || player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_620) !== main || main.isHidden ||
                        player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_SIDE_621) !== component ||
                        getAttribute<Shop?>(player, "shop", null) !== shop || player.inventory[slot] !== itemInSlot || itemInSlot.id != itemId)
                        return@sendInputDialogue
                    shop.sell(player, slot, amt)
                }
            }

            return@onNative true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.NPC, "trade", "shop") { _, node ->
            val npc = node as NPC
            if (npc.getAttribute("facing_booth", false)) {
                val offsetX = npc.direction.stepX shl 1
                val offsetY = npc.direction.stepY shl 1
                return@setDest npc.location.transform(offsetX, offsetY, 0)
            }
            return@setDest node.location
        }
    }

    /**
     * Registers shop-related commands.
     */
    override fun defineCommands() {
        define("openshop", Privilege.ADMIN) { player, args ->
            if (args.size < 2) reject(player, "Usage: ::openshop shopId")
            val shopId = args[1].toInt()
            shopsById[shopId]?.openFor(player)
        }

        define("shopscript", Privilege.ADMIN) { player, args ->
            val arg1 = args[1].toInt()
            player.packetDispatch.sendRunScript(25, "vg", arg1, 92)
        }
    }
}
