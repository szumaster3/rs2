package core.game.shops

import core.ServerConstants
import core.api.*
import core.game.component.Component
import core.game.container.Container
import core.game.container.ContainerEvent
import core.game.container.ContainerType
import core.game.event.ItemShopPurchaseEvent
import core.game.event.ItemShopSellEvent
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.shops.Shops.Companion.logShop
import core.game.system.config.ItemConfigParser
import core.game.world.GameWorld
import core.net.packet.PacketRepository
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.ContainerContext
import shared.consts.Components
import shared.consts.Items
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Represents a single item in a shop.
 */
data class ShopItem(var itemId: Int, var amount: Int, val restockRate: Int = 100)

/**
 * A listener for shop container updates, used to send interface updates to the player.
 */
class ShopListener(val player: Player) : ContainerListener {
    var enabled = false

    /**
     * Called when the container changes.
     */
    override fun update(c: Container?, event: ContainerEvent?) {
        if (event == null || event.isClear) {
            refresh(c)
            return
        }
        PacketRepository.send(Encoders578.CONTAINER_PACKET, ContainerContext(player, -1, -1, 92, event.items, false, *event.slots))
    }

    /**
     * Refreshes the entire container.
     */
    override fun refresh(c: Container?) {
        if (c == null) return
        PacketRepository.send(Encoders578.CONTAINER_PACKET, ContainerContext(player, -1, -1, 92, c.toArray(), c.capacity(), false))
    }
}

/**
 * Represents a player-accessible shop with configurable stock, currency, and settings.
 */
class Shop(val title: String, val stock: Array<ShopItem>, val general: Boolean = false, val currency: Int = Items.COINS_995, val highAlch: Boolean = false, val forceShared: Boolean = false) {

    /**
     * Map of player UID or global key to stock containers.
     */
    val stockInstances = HashMap<Int, Container>()

    /**
     * Container for general (player-sellable) stock.
     */
    val playerStock = if (general) generalPlayerStock else Container(40, ContainerType.SHOP)

    /**
     * Map of players or global key to a flag indicating whether the shop should be restocked.
     */
    val needsUpdate = HashMap<Int, Boolean>()

    /**
     * Map of item ids to their restocking rates.
     */
    val restockRates = HashMap<Int, Int>()

    init {
        if (!getServerConfig().getBoolean(Shops.personalizedShops, false) || forceShared) {
            stockInstances[ServerConstants.SERVER_NAME.hashCode()] = generateStockContainer()
        }
    }

    /**
     * Opens the shop interface for the given player.
     */
    fun openFor(player: Player) {
        if (player.interfaceManager.open(Component(Components.SHOP_TEMPLATE_620)) == null) return
        val cont = getContainer(player)
        sendString(player, title, Components.SHOP_TEMPLATE_620, 20)
        sendInterfaceConfig(player, Components.SHOP_TEMPLATE_620, 19, !general)
        setAttribute(player, "shop", this)
        setAttribute(player, "shop-cont", cont)
        player.interfaceManager.openSingleTab(Component(Components.SHOP_TEMPLATE_SIDE_621))
        showTab(player, true)
        logShop("Opening shop [Title: $title, Player: ${player.username}]")
    }

    /**
     * Shows the main or player stock tab in the shop for the player.
     */
    fun showTab(player: Player, main: Boolean) {
        if (player.interfaceManager.getComponent(Components.SHOP_TEMPLATE_620) == null) return
        val cont = if (main) getAttribute<Container?>(player, "shop-cont", null) ?: return else playerStock

        val listener = listenerInstances[player.details.uid] ?: return
        val mainStock = getAttribute<Container?>(player, "shop-cont", null) ?: return
        mainStock.listeners.remove(listener)
        playerStock.listeners.remove(listener)
        cont.listeners.add(listener)

        val settings = IfaceSettingsBuilder().enableOptions(0..9).build()

        player.packetDispatch.sendIfaceSettings(
            settings,
            if (main) 23 else 24,
            Components.SHOP_TEMPLATE_620,
            0,
            cont.capacity(),
        )
        player.packetDispatch.sendRunScript(
            150,
            "IviiiIsssssssss",
            "",
            "",
            "",
            "",
            "Buy X",
            "Buy 10",
            "Buy 5",
            "Buy 1",
            "Value",
            -1,
            0,
            4,
            10,
            92,
            (Components.SHOP_TEMPLATE_620 shl 16) or if (main) 23 else 24,
        )
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 23, !main)
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 24, main)
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 29, !main)
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 25, main)
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 27, main)
        player.packetDispatch.sendInterfaceConfig(Components.SHOP_TEMPLATE_620, 26, !main)

        if (!main) {
            playerStock.refresh()
        } else {
            cont.refresh()
        }

        setAttribute(player, "shop-main", main)
    }

    /**
     * Gets the shop container.
     */
    fun getContainer(player: Player): Container {
        val container = if (getServerConfig().getBoolean(Shops.personalizedShops, false) && !forceShared) {
            stockInstances[player.details.uid] ?: generateStockContainer().also {
                stockInstances[player.details.uid] = it
            }
        } else {
            stockInstances[ServerConstants.SERVER_NAME.hashCode()]
                ?: generateStockContainer().also { stockInstances[ServerConstants.SERVER_NAME.hashCode()] = it }
        }

        val listener = listenerInstances[player.details.uid]

        if (listener != null && listener.player != player) {
            container.listeners.remove(listener)
        }

        if (listener == null || listener.player != player) {
            listenerInstances[player.details.uid] = ShopListener(player)
        }

        return container
    }

    /**
     * Generates a new container based on the initial stock defined in the shop.
     *
     * @return A newly populated stock container.
     */
    private fun generateStockContainer(): Container {
        val container = Container(40, ContainerType.SHOP)
        for (item in stock) {
            container.add(Item(item.itemId, item.amount))
            restockRates[item.itemId] = item.restockRate
        }

        return container
    }

    /**
     * Restocks all shop containers that have been flagged for update.
     * Adjusts item amount to match initial stock over time.
     */
    fun restock() {
        stockInstances.filter { needsUpdate[it.key] == true }.forEach { (player, cont) ->
            for (i in 0 until cont.capacity()) {
                if (cont[i] == null) continue
                if (stock.size < i + 1) break
                if (stock[i].restockRate == 0) continue
                if (GameWorld.ticks % stock[i].restockRate != 0) continue

                if (cont[i].amount < stock[i].amount) {
                    cont[i].amount++
                    cont.event.flag(i, cont[i])
                } else if (cont[i].amount > stock[i].amount) {
                    cont[i].amount--
                    cont.event.flag(i, cont[i])
                }
                if (cont[i].amount != stock[i].amount) needsUpdate[player] = true
            }
            cont.update()
        }
    }

    /**
     * Gets the cost in currency for a player to buy an item from the shop.
     */
    fun getBuyPrice(player: Player, slot: Int): Item {
        val isMainStock = getAttribute(player, "shop-main", true)
        val cont =
            if (isMainStock) getAttribute<Container?>(player, "shop-cont", null) ?: return Item(-1, -1) else playerStock
        val item = cont[slot]

        if (item == null || (isMainStock && stock.getOrNull(slot)?.itemId != item.id)) {
            sendMessage(player, "That item doesn't appear to be there anymore. Please try again.")
            return Item(-1, -1)
        }

        val price = when (currency) {
            Items.TOKKUL_6529 -> item.definition.getConfiguration(ItemConfigParser.TOKKUL_PRICE, 1)
            Items.ARCHERY_TICKET_1464 -> item.definition.getConfiguration(ItemConfigParser.ARCHERY_TICKET_PRICE, 1)
            Items.CASTLE_WARS_TICKET_4067 -> item.definition.getConfiguration(
                ItemConfigParser.CASTLE_WARS_TICKET_PRICE,
                1,
            )

            else -> {
                val fixedPrice = fixedPriceItems[item.id]
                fixedPrice ?: getGPCost(Item(item.id, 1),
                    if (isMainStock) stock[slot].amount else item.amount,
                    item.amount)
            }
        }

        return Item(currency, price)
    }

    /**
     * Gets the shop payment and target container when a player sells an item.
     */
    fun getSellPrice(player: Player, slot: Int): Pair<Container?, Item> {
        val shopCont = getAttribute<Container?>(player, "shop-cont", null) ?: return Pair(null, Item(-1, -1))
        val item = player.inventory[slot]

        if (item == null) {
            player.debug("Inventory slot $slot does not contain an item!")
            sendMessage(player, "That item doesn't seem to be there anymore. Please try again.")
            return Pair(null, Item(-1, -1))
        }

        val shopItemId = if (item.definition.isUnnoted) {
            item.id
        } else {
            item.noteChange
        }
        var (isPlayerStock, shopSlot) = getStockSlot(shopItemId)

        val stockAmt = if (isPlayerStock) {
            0
        } else {
            if (shopSlot != -1) {
                stock[shopSlot].amount
            } else {
                0
            }
        }
        val currentAmt = if (isPlayerStock) {
            playerStock.getAmount(shopItemId)
        } else {
            if (shopSlot != -1) {
                shopCont[shopSlot]?.amount ?: return Pair(null, Item(-1, -1))
            } else {
                isPlayerStock = true
                0
            }
        }

        val price = when (currency) {
            // selling items authentically return 10x less tokkul (floored/truncated) than the item's shop price
            Items.TOKKUL_6529 -> (item.definition.getConfiguration(ItemConfigParser.TOKKUL_PRICE, 1) / 10.0).toInt()
            Items.ARCHERY_TICKET_1464 -> item.definition.getConfiguration(ItemConfigParser.ARCHERY_TICKET_PRICE, 1)
            Items.CASTLE_WARS_TICKET_4067 -> item.definition.getConfiguration(ItemConfigParser.CASTLE_WARS_TICKET_PRICE, 1)
            else -> {
                val fixedPrice = fixedPriceItems[shopItemId]
                fixedPrice ?: getGPSell(Item(shopItemId, 1), stockAmt, currentAmt)
            }
        }

        if (!general && stockAmt == 0 && shopSlot == -1) {
            return Pair(null, Item(-1, -1))
        }

        return Pair(if (isPlayerStock) playerStock else shopCont, Item(currency, price))
    }

    /**
     * Calculates the adjusted buy price of an item based on stock levels.
     */
    private fun getGPCost(item: Item, stockAmount: Int, currentAmt: Int): Int {
        var mod: Int
        mod = if (stockAmount == 0) {
            100
        } else if (currentAmt == 0) {
            130
        } else if (currentAmt >= stockAmount) {
            100
        } else {
            130 - (30L * currentAmt / stockAmount).toInt()
        }
        if (mod < 1) mod = 1
        mod = max(100, min(130, mod))

        val price: Int = ceil(item.definition.cost * mod.toDouble() / 100.0).toInt()

        return max(price, 1)
    }

    /** Sum the existing per-unit formula by its 30 percentage bands, rather than iterating a potentially huge quantity. */
    private fun bulkGpBuyCost(item: Item, baseline: Int, current: Int, amount: Int): Long {
        if (baseline <= 0) return getGPCost(item, baseline, current).toLong() * amount
        val low = current.toLong() - amount + 1
        val high = current.toLong()
        var total = 0L
        fun addBand(from: Long, to: Long) {
            val start = maxOf(low, from)
            val end = minOf(high, to)
            if (start <= end) total += getGPCost(item, baseline, start.toInt()).toLong() * (end - start + 1)
        }
        for (band in 0..29) {
            val from = (band.toLong() * baseline + 29) / 30
            val to = ((band + 1L) * baseline + 29) / 30 - 1
            addBand(from, to)
        }
        addBand(baseline.toLong(), high)
        return total
    }

    /**
     * Calculates the GP received when selling an item to the shop.
     */
    private fun getGPSell(item: Item, stockAmount: Int, currentAmt: Int): Int {
        val base = item.definition.getAlchemyValue(highAlch)
        val overstock = (currentAmt.toLong() - stockAmount).coerceAtMost(10L)
        if (overstock < 0) {
            return base
        }
        val price = (base - (item.definition.cost * 0.03 * overstock)).roundToInt()
        if (price < 1) {
            return 1
        }
        return price
    }

    fun buy(player: Player, slot: Int, amount: Int): TransactionStatus {
        if (amount !in 1..Integer.MAX_VALUE) return TransactionStatus.Failure("Invalid amount: $amount")
        val isMainStock = getAttribute(player, "shop-main", true)
        val cont = if (isMainStock) {
            (getAttribute<Container?>(player, "shop-cont", null)
                ?: return TransactionStatus.Failure("Invalid shop-cont attr"))
        } else {
            playerStock
        }
        val inStock = cont[slot] ?: return TransactionStatus.Failure("Empty shop slot: $slot")
        if (isMainStock && stock.getOrNull(slot)?.itemId != inStock.id)
            return TransactionStatus.Failure("Shop stock slot no longer matches its configuration")
        if (inStock.amount <= 0) {
            sendMessage(player, "There is no stock of that item at the moment.")
            return TransactionStatus.Failure("Shop item out of stock.")
        }
        val item = Item(inStock.id, minOf(amount, inStock.amount))
        // Paying with the last coins may free an inventory slot. Price that one item before attempting payment.
        item.amount = minOf(item.amount, player.inventory.getMaximumAdd(item).coerceAtLeast(1))
        val cost = getBuyPrice(player, slot)
        if (cost.id == 65535 || cost.amount < 0) {
            sendMessage(player, "This shop cannot sell that item.")
            return TransactionStatus.Failure("Shop cannot sell this item")
        }
        val fixedPrice = fixedPriceItems[item.id]
        val total = if (currency == Items.COINS_995 && fixedPrice == null) {
            val baseline = if (isMainStock) stock[slot].amount else inStock.amount
            bulkGpBuyCost(item, baseline, inStock.amount, item.amount)
        } else cost.amount.toLong() * item.amount
        if (total !in 0L..Int.MAX_VALUE.toLong()) {
            sendMessage(player, "You don't have enough money.")
            return TransactionStatus.Failure("Shop purchase cost exceeds valid amount")
        }
        cost.amount = total.toInt()
        if (inInventory(player, cost.id, cost.amount)) {
            if (removeItem(player, cost)) {
                if (!hasSpaceFor(player, item)) {
                    addItem(player, cost.id, cost.amount)
                    sendMessage(player, "You don't have enough inventory space to buy that many.")
                    return TransactionStatus.Failure("Not enough inventory space")
                }
                if (!isMainStock && cont[slot].amount - item.amount == 0) {
                    cont.remove(cont[slot], false)
                    cont.refresh()
                } else {
                    cont[slot].amount -= item.amount
                    cont.event.flag(slot, cont[slot])
                    cont.update()
                }
                addItem(player, item.id, item.amount)
                if (getServerConfig().getBoolean(Shops.personalizedShops, false)) {
                    needsUpdate[player.details.uid] = true
                } else {
                    needsUpdate[ServerConstants.SERVER_NAME.hashCode()] = true
                }
            } else return TransactionStatus.Failure("Unable to remove purchase currency")
        } else {
            when (currency) {
                Items.TOKKUL_6529 -> sendMessage(player, "You don't have enough tokkul to purchase that.")
                Items.ARCHERY_TICKET_1464 -> sendMessage(player, "You only had enough money to buy some of the items you requested.")
                Items.CASTLE_WARS_TICKET_4067 -> sendMessage(player, "You don't have enough castle wars tickets to purchase that.")
                else -> sendMessage(player, "You don't have enough money.")
            }
            return TransactionStatus.Failure("Not enough money in inventory")
        }
        player.dispatch(ItemShopPurchaseEvent(item.id, item.amount, cost))
        return TransactionStatus.Success()
    }

    fun sell(player: Player, slot: Int, amount: Int): TransactionStatus {
        if (amount !in 1..Integer.MAX_VALUE) return TransactionStatus.Failure("Invalid amount: $amount")
        val playerInventory = player.inventory[slot] ?: return TransactionStatus.Failure("Empty inventory slot: $slot")
        if (playerInventory.id in intArrayOf(Items.COINS_995, Items.TOKKUL_6529, Items.ARCHERY_TICKET_1464)) {
            sendMessage(player, "You can't sell currency to a shop.")
            return TransactionStatus.Failure("Tried to sell currency - ${playerInventory.id}")
        }
        val item = Item(playerInventory.id, amount)
        val def = itemDefinition(item.id)

        if (def.hasDestroyAction()) {
            sendMessage(player, "You can't sell this item.")
            return TransactionStatus.Failure("Attempt to sell a destroyable - ${playerInventory.id}.")
        }

        if (!def.isTradeable) {
            sendMessage(player, "You can't sell this item.")
            return TransactionStatus.Failure("Attempt to sell an untradeable - ${playerInventory.id}.")
        }

        val (container, profit) = getSellPrice(player, slot)
        if (container == null || profit.amount < 0 || profit.id == 65535) {
            sendMessage(player, "This item can't be sold to this shop.").also {
                return TransactionStatus.Failure("Can't sell this item to this shop - ${playerInventory.id}, general: $general, price: $profit")
            }
        }
        if (amount > player.inventory.getAmount(item.id)) {
            item.amount = player.inventory.getAmount(item.id)
        }

        val id = if (!item.definition.isUnnoted) item.noteChange else item.id
        val (isPlayerStock, shopSlot) = getStockSlot(id)

        if (isPlayerStock && shopSlot == -1 && generalPlayerStock.freeSlots() == 0) {
            sendMessage(player, "The shop is too full to buy any more items")
            return TransactionStatus.Failure("Attempt to sell to full shop.")
        }

        val sold = Item(id, item.amount)
        if (item.amount <= 0 || container.getMaximumAdd(sold) < item.amount)
            return TransactionStatus.Failure("Shop cannot accept that quantity")
        val fixedPrice = fixedPriceItems[id]
        var total = if (fixedPrice != null) fixedPrice.toLong() * item.amount else profit.amount.toLong() * item.amount
        if (fixedPrice == null && currency == Items.COINS_995 && item.amount > 1) {
            val baseline = if (isPlayerStock) 0 else stock[shopSlot].amount
            val current = container[shopSlot]?.amount ?: playerStock.getAmount(id)
            val first = current.toLong() + 1
            val last = current.toLong() + item.amount - 1
            total = profit.amount.toLong()
            // The legacy GP sell formula has a flat rate below baseline and after 10 overstock units.
            fun addBand(from: Long, to: Long, sample: Int) {
                val count = minOf(last, to) - maxOf(first, from) + 1
                if (count > 0) total += getGPSell(Item(item.id, 1), baseline, sample).toLong() * count
            }
            if (baseline > Int.MIN_VALUE) addBand(first, baseline.toLong() - 1, baseline - 1)
            addBand(baseline.toLong(), baseline.toLong(), baseline)
            for (overstock in 1..9) {
                val level = baseline.toLong() + overstock
                if (level <= Int.MAX_VALUE) addBand(level, level, level.toInt())
            }
            val cap = baseline.toLong() + 10
            if (cap <= Int.MAX_VALUE) addBand(cap, last, cap.toInt())
        }
        if (total !in 0L..Int.MAX_VALUE.toLong())
            return TransactionStatus.Failure("Shop sale value exceeds valid amount")
        profit.amount = total.toInt()

        if (removeItem(player, item)) {
            if (!hasSpaceFor(player, profit)) {
                sendMessage(player, "You don't have enough space to do that.")
                addItem(player, item.id, item.amount)
                return TransactionStatus.Failure("Did not have enough inventory space")
            }
            if (container == playerStock && getAttribute(player, "shop-main", false)) {
                showTab(player, false)
            } else if (!getAttribute(player, "shop-main", false) && container != playerStock) {
                showTab(player, true)
            }
            addItem(player, profit.id, profit.amount)
            if (!item.definition.isUnnoted) {
                item.id = item.noteChange
            }
            container.add(item)
            container.refresh()
            if (getServerConfig().getBoolean(Shops.personalizedShops, false)) {
                needsUpdate[player.details.uid] = true
            } else {
                needsUpdate[ServerConstants.SERVER_NAME.hashCode()] = true
            }
        } else return TransactionStatus.Failure("Unable to remove sold items")

        player.dispatch(ItemShopSellEvent(item.id, item.amount, profit))
        return TransactionStatus.Success()
    }

    /**
     * Finds the stock source and slot index for a given item id.
     */
    fun getStockSlot(itemId: Int): Pair<Boolean, Int> {
        var shopSlot: Int = -1
        var isPlayerStock = false
        val notechange = itemDefinition(itemId).noteId
        for ((stockSlot, shopItem) in stock.withIndex()) {
            if (shopItem.itemId == itemId || shopItem.itemId == notechange) {
                shopSlot = stockSlot
            }
        }
        if (shopSlot == -1) {
            for ((stockSlot, playerStockItem) in playerStock.toArray().withIndex()) {
                if (playerStockItem == null) continue
                if (playerStockItem.id == itemId || playerStockItem.id == notechange) {
                    shopSlot = stockSlot
                    isPlayerStock = true
                }
            }
        }

        if (shopSlot == -1) isPlayerStock = true
        return Pair(isPlayerStock, shopSlot)
    }

    companion object {
        /**
         * Shared container for player-sold items in general stores.
         */
        val generalPlayerStock = Container(40, ContainerType.SHOP)

        /**
         * Maps shop ids to their active ShopListener instances.
         */
        val listenerInstances = HashMap<Int, ShopListener>()

        /**
         * Items with fixed prices that don't fluctuate based on stock (both buy & sell price).
         */
        val fixedPriceItems = mapOf(
            Items.SPIRIT_SHARDS_12183 to 25,
            Items.POUCH_12155 to 1
        )
    }

    /**
     * Represents the outcome of a shop transaction.
     */
    sealed class TransactionStatus {

        /**
         * Transaction completed successfully.
         */
        class Success : TransactionStatus()

        /**
         * Transaction failed with a reason.
         */
        class Failure(val reason: String) : TransactionStatus()
    }
}
