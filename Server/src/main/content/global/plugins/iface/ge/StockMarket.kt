package content.global.plugins.iface.ge

import core.api.*
import core.game.component.Component
import core.game.dialogue.InputType
import core.game.ge.*
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.net.packet.PacketRepository
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.ContainerContext
import core.tools.Log
import core.tools.SystemLogger
import shared.consts.Components
import shared.consts.Items
import shared.consts.Sounds
import kotlin.math.min

/**
 * Handles the grand exchange interface.
 *
 * @author Ceikry
 */
class StockMarket : InterfaceListener {
    override fun defineInterfaceListeners() {
        onOpen(Components.STOCKMARKET_105) { player, _ ->
            sendInterfaceConfig(player, 105, 193, true)
            sendIfaceSettings(player, 6, 211, 105, -1, -1)
            sendIfaceSettings(player, 6, 209, 105, -1, -1)
            setVarp(player, 1112, -1)
            return@onOpen true
        }

        onClose(Components.STOCKMARKET_105) { player, _ ->
            player.packetDispatch.sendRunScript(571, "")
            player.interfaceManager.closeChatbox()
            player.interfaceManager.closeSingleTab()
            removeAttribute(player, "ge-temp")
            setAttribute(player, "ge-index", -1)
            return@onClose true
        }

        onClose(Components.EXCHANGE_SEARCH_389) { player, _ ->
            player.packetDispatch.sendRunScript(571, "")
            return@onClose true
        }

        onNative(Components.STOCKSIDE_107) { player, _, op, button, slot, itemId ->
            if (button != 18 || slot < 0 || slot > 27) {
                return@onNative false
            }
            val item = player.inventory[slot] ?: return@onNative false
            val main = player.interfaceManager.getComponent(Components.STOCKMARKET_105) ?: return@onNative true
            if (main.isHidden || item.id != itemId) return@onNative true
            when (op) {
                InterfaceOperation.OP10 -> sendMessages(player, item.definition.examine)
                InterfaceOperation.OP1 -> {
                    val offer = getAttribute<GrandExchangeOffer?>(player, "ge-temp", null) ?: return@onNative true
                    val index = getAttribute(player, "ge-index", -1)
                    if (index !in 0..5 || ExchangeHistory.getInstance(player).offerRecords[index] != null ||
                        getVarp(player, 1113) != 1 || offer.index != index || !offer.sell) return@onNative true
                    if (item.id == 995) {
                        sendMessage(player, "You can't sell money!")
                        return@onNative true
                    }
                    var id = item.id
                    if (!item.definition.isUnnoted) {
                        id = item.noteChange
                    }
                    if (!PriceIndex.canTrade(id)) {
                        sendMessage(player, "This item can't be sold on the Grand Exchange.")
                        return@onNative true
                    }
                    offer.itemID = id
                    offer.sell = true
                    offer.player = player
                    offer.offeredValue = GrandExchange.getRecommendedPrice(id)
                    offer.amount = item.amount
                    offer.index = index
                    updateVarbits(player, offer, index, true)
                    setAttribute(player, "ge-temp", offer)
                    sendString(player, GrandExchange.getOfferStats(id, true), 105, 142)
                }
            }
            return@onNative true
        }

        onNative(Components.STOCKMARKET_105) { player, component, op, button, _, _ ->
            if (op != InterfaceOperation.OP1 && !(op == InterfaceOperation.OP2 &&
                    button in intArrayOf(18, 34, 50, 69, 88, 107, 209, 211))) return@onNative true
            val tempOffer = getAttribute(player, "ge-temp", GrandExchangeOffer())
            val session = player.session
            var openedIndex = getAttribute(player, "ge-index", -1)
            var openedOffer = ExchangeHistory.getInstance(player).getOffer(openedIndex)
            if (button in intArrayOf(157,159,162,164,166,168,170,171,173,177,180,183,185,190,194) &&
                (openedIndex !in 0..5 || openedOffer != null || tempOffer.index != openedIndex ||
                    getAttribute<GrandExchangeOffer?>(player, "ge-temp", null) !== tempOffer)) return@onNative true

            when (button) {
                209, 211 -> if (openedOffer == null) {
                    SystemLogger.logGE("[WARN] Player tried to withdraw item with null openedOffer!")
                    return@onNative false
                } else {
                    withdrawNative(player, openedOffer, (button - 209) shr 1, op)
                    return@onNative true
                }

                190 -> {
                    if (openedIndex in 0..5 && openedOffer == null && tempOffer.amount > 0 &&
                        tempOffer.itemID > 0 && PriceIndex.canTrade(tempOffer.itemID))
                        confirmOffer(player, tempOffer, openedIndex)
                    return@onNative true
                }
                194 -> {
                    if (!tempOffer.sell) {
                        openChatbox(player, Components.EXCHANGE_SEARCH_389)
                        if (player.interfaceManager.chatbox?.id == Components.EXCHANGE_SEARCH_389)
                            player.packetDispatch.sendRunScript(570, "s", "Grand Exchange Item Search")
                    }
                    return@onNative true
                }
                203 -> abortOffer(player, openedOffer)
                18, 34, 50, 69, 88, 107 -> {
                    openedIndex = intArrayOf(18, 34, 50, 69, 88, 107).indexOf(button)
                    openedOffer = ExchangeHistory.getInstance(player).getOffer(openedIndex)
                    if (op == InterfaceOperation.OP2) {
                        abortOffer(player, openedOffer)
                        return@onNative true
                    }
                    if (openedOffer == null) return@onNative true
                    removeAttribute(player, "ge-temp")
                    setAttribute(player, "ge-index", openedIndex)
                    updateVarbits(player, openedOffer, openedIndex)
                    if (openedOffer != null) {
                        sendString(
                            player,
                            GrandExchange.getOfferStats(openedOffer.itemID, openedOffer.sell),
                            Components.STOCKMARKET_105,
                            142,
                        )
                    }
                    return@onNative true
                }

                30, 46, 62, 81, 100, 119 -> {
                    openedIndex = intArrayOf(30, 46, 62, 81, 100, 119).indexOf(button)
                    openedOffer = ExchangeHistory.getInstance(player).getOffer(openedIndex)
                    if (openedOffer != null) return@onNative true
                    setAttribute(player, "ge-temp", GrandExchangeOffer().also { it.index = openedIndex; it.player = player })
                    setAttribute(player, "ge-index", openedIndex)
                    updateVarbits(player, null, openedIndex)
                    openChatbox(player, Components.EXCHANGE_SEARCH_389)
                    if (player.interfaceManager.chatbox?.id == 389) {
                        player.packetDispatch.sendRunScript(570, "s", "Grand Exchange Item Search")
                    }
                    return@onNative true
                }

                31, 47, 63, 82, 101, 120 -> {
                    openedIndex = intArrayOf(31, 47, 63, 82, 101, 120).indexOf(button)
                    openedOffer = ExchangeHistory.getInstance(player).getOffer(openedIndex)
                    if (openedOffer != null) return@onNative true
                    setAttribute(player, "ge-temp", GrandExchangeOffer().also { it.index = openedIndex; it.player = player; it.sell = true })
                    setAttribute(player, "ge-index", openedIndex)
                    updateVarbits(player, null, openedIndex, true)
                    player.interfaceManager.openSingleTab(Component(Components.STOCKSIDE_107))
                    player.packetDispatch.sendRunScript(150, "IviiiIsssssssss", "", "", "", "", "", "", "", "", "Offer",
                        -1, 0, 7, 4, 93, (107 shl 16) or 18)
                    player.packetDispatch.sendIfaceSettings(IfaceSettingsBuilder().enableOptions(0, 9).build(), 18, 107, 0, 27)
                    refreshInventory(player)
                    return@onNative true
                }

                157 -> {
                    playAudio(player, Sounds.GE_DOWN_AMOUNT_4045)
                    updateOfferAmount(player, tempOffer, tempOffer.amount - 1)
                }
                159 -> {
                    playAudio(player, Sounds.GE_UP_AMOUNT_4041)
                    updateOfferAmount(player, tempOffer, tempOffer.amount + 1)
                }
                162 -> updateOfferAmount(player, tempOffer, if (tempOffer.sell) 1 else tempOffer.amount + 1)
                164 -> updateOfferAmount(player, tempOffer, if (tempOffer.sell) 10 else tempOffer.amount + 10)
                166 -> updateOfferAmount(player, tempOffer, if (tempOffer.sell) 100 else tempOffer.amount + 100)
                168 -> {
                    val amt = if (tempOffer.sell) {
                        getInventoryAmount(player, tempOffer.itemID)
                    } else {
                        tempOffer.amount + 1000
                    }

                    updateOfferAmount(player, tempOffer, amt)
                }

                170 -> sendInputDialogue(player, false, "Enter the amount:") { value ->
                    if (player.session !== session || player.interfaceManager.getComponent(105) !== component || component.isHidden ||
                        getAttribute<GrandExchangeOffer?>(player, "ge-temp", null) !== tempOffer ||
                        getAttribute(player, "ge-index", -1) != openedIndex) return@sendInputDialogue
                    var s = value.toString()
                    s = s.replace("k", "000")
                    s = s.replace("K", "000")
                    s = s.replace("m", "000000")
                    s = s.replace("M", "000000")
                    updateOfferAmount(player, tempOffer, (s.toIntOrNull() ?: return@sendInputDialogue))
                    setAttribute(player, "ge-temp", tempOffer)
                }

                180 -> updateOfferValue(player, tempOffer, GrandExchange.getRecommendedPrice(tempOffer.itemID))
                177 -> updateOfferValue(player, tempOffer, (GrandExchange.getRecommendedPrice(tempOffer.itemID) * 0.95).toInt())

                183 -> updateOfferValue(player, tempOffer, (GrandExchange.getRecommendedPrice(tempOffer.itemID) * 1.05).toInt())

                171 -> updateOfferValue(player, tempOffer, tempOffer.offeredValue - 1)
                173 -> updateOfferValue(player, tempOffer, tempOffer.offeredValue + 1)
                185 -> sendInputDialogue(player, InputType.AMOUNT, "Enter the amount:") { value ->
                    if (player.session !== session || player.interfaceManager.getComponent(105) !== component || component.isHidden ||
                        getAttribute<GrandExchangeOffer?>(player, "ge-temp", null) !== tempOffer ||
                        getAttribute(player, "ge-index", -1) != openedIndex) return@sendInputDialogue
                    var s = value.toString()
                    updateOfferValue(player, tempOffer, (s.toIntOrNull() ?: return@sendInputDialogue))
                    setAttribute(player, "ge-temp", tempOffer)
                }

                13 -> {
                    closeInterface(player)
                    return@onNative true
                }
                127 -> {
                    closeChatBox(player)
                    closeSingleTab(player)
                    removeAttribute(player, "ge-temp")
                    setAttribute(player, "ge-index", -1)
                    return@onNative true
                }
                else -> return@onNative true
            }
            setAttribute(player, "ge-index", openedIndex)
            setAttribute(player, "ge-temp", tempOffer)
            return@onNative true
        }
    }

    /**
     * Updates the offer's value (price) in the Grand Exchange.
     *
     * @param player The player making the offer.
     * @param offer The current Grand Exchange offer being made.
     * @param newAmt The new price for the offer.
     */
    private fun updateOfferValue(
        player: Player,
        offer: GrandExchangeOffer,
        newAmt: Int,
    ) {
        offer.offeredValue = newAmt.coerceAtLeast(1)
        setVarp(player, 1111, offer.offeredValue)
    }

    /**
     * Updates the amount of an offer in the Grand Exchange.
     *
     * @param player The player making the offer.
     * @param offer The current Grand Exchange offer being made.
     * @param newAmt The new amount of items being offered.
     */
    private fun updateOfferAmount(player: Player, offer: GrandExchangeOffer, newAmt: Int) {
        offer.amount = newAmt.coerceAtLeast(1)
        setVarp(player, 1110, offer.amount)
    }

    /**
     * Aborts a currently active offer.
     *
     * @param player The player requesting the offer cancellation.
     * @param offer The Grand Exchange offer to be aborted.
     */
    private fun abortOffer(player: Player, offer: GrandExchangeOffer?) {
        if (offer == null) {
            log(this::class.java, Log.WARN, "Opened offer was null and was attempted to be aborted!")
            return
        }
        sendMessage(player, "Abort request acknowledged. Please be aware that your offer may")
        sendMessage(player, "have already been completed.")

        if (!offer.isActive) {
            log(
                this::class.java,
                Log.WARN,
                "Offer ${offer.uid}[${offer.index}]: ${if (offer.sell) "s" else "b"} ${offer.itemID}x ${offer.amount} was NO LONGER active when abort attempted"
            )
            return
        }
        offer.offerState = OfferState.ABORTED
        if (offer.sell) {
            playAudio(player,Sounds.GE_COLLECT_ITEMS_4040)
            offer.addWithdrawItem(offer.itemID, offer.amountLeft)
        } else {
            playAudio(player,Sounds.GE_COLLECT_COINS_4042)
            offer.addWithdrawItem(Items.COINS_995, offer.amountLeft * offer.offeredValue)
        }
        offer.update()
        offer.visualize(player)
    }

    enum class OfferConfirmResult {
        Success, ZeroCoins, TooManyCoins, NotEnoughItemsOrCoins, ItemRemovalFailure, OfferPlacementError,
    }

    /**
     * Confirms the offer.
     *
     * @param player The player confirming the offer.
     * @param offer The offer being confirmed.
     * @param index The index of the offer in the Grand Exchange.
     * @return The result of the offer confirmation.
     */
    fun confirmOffer(player: Player, offer: GrandExchangeOffer, index: Int): OfferConfirmResult {
        if (offer.offeredValue < 1) {
            playAudio(player, Sounds.GE_TRADE_ERROR_4039)
            sendMessage(player, "You can't make an offer for 0 coins.")
            return OfferConfirmResult.ZeroCoins
        }
        if (offer.amount > Int.MAX_VALUE / offer.offeredValue) {
            playAudio(player, Sounds.GE_TRADE_ERROR_4039)
            sendMessage(player, "You can't ${if (offer.sell) "sell" else "buy"} this much!")
            return OfferConfirmResult.TooManyCoins
        }
        offer.index = index
        if (offer.sell) {
            val maxAmt = getInventoryAmount(player, offer.itemID)
            if (offer.amount > maxAmt) {
                playAudio(player, Sounds.GE_TRADE_ERROR_4039)
                sendMessage(player, "You do not have enough of this item in your inventory to cover the")
                sendMessage(player, "offer.")
                return OfferConfirmResult.NotEnoughItemsOrCoins
            }
            val amountUnnoted = min(amountInInventory(player, offer.itemID), offer.amount)
            val amountLeft = offer.amount - amountUnnoted
            val removedUnnoted = if (amountUnnoted > 0) removeItem(player, Item(offer.itemID, amountUnnoted)) else true
            val removeNoted =
                if (amountLeft > 0) removeItem(player, Item(itemDefinition(offer.itemID).noteId, amountLeft)) else true
            if (!removedUnnoted || !removeNoted) return OfferConfirmResult.ItemRemovalFailure
            if (GrandExchange.dispatch(player, offer)) {
                player.removeAttribute("ge-temp")
            } else {
                if (amountUnnoted > 0) {
                    addItem(player, offer.itemID, offer.amount - amountLeft)
                }
                if (amountLeft > 0) {
                    addItem(player, itemDefinition(offer.itemID).noteId, amountLeft)
                }
                sendMessage(player, "Unable to place GE offer. Please try again.")
                return OfferConfirmResult.OfferPlacementError
            }
        } else {
            val total = offer.amount * offer.offeredValue
            if (!inInventory(player, Items.COINS_995, total)) {
                playAudio(player, Sounds.GE_TRADE_ERROR_4039)
                sendMessage(player, "You do not have enough coins to cover the offer.")
                return OfferConfirmResult.NotEnoughItemsOrCoins
            }
            if (GrandExchange.dispatch(player, offer) && removeItem(player, Item(Items.COINS_995, total))) {
                player.removeAttribute("ge-temp")
            }
        }
        playAudio(player, Sounds.GE_PLACE_ITEM_4043)
        offer.visualize(player)
        toMainInterface(player)
        return OfferConfirmResult.Success
    }

    private fun getInventoryAmount(player: Player, itemId: Int): Int {
        val item = Item(itemId)
        var amount = player.inventory.getAmount(item)
        if (item.definition.noteId > -1) {
            amount += player.inventory.getAmount(Item(item.definition.noteId))
        }
        return amount
    }

    companion object {
        @JvmStatic
        fun openFor(player: Player) {
            if (!player.bankPinManager.isUnlocked) {
                player.bankPinManager.openType(4)
                return
            }
            openInterface(player, Components.STOCKMARKET_105)
        }

        @JvmStatic
        fun updateVarbits(player: Player, offer: GrandExchangeOffer?, index: Int, sale: Boolean? = null) {
            val isSale = sale ?: offer?.sell ?: false
            var lowPrice = 0
            var highPrice = 0
            val recommendedPrice = GrandExchange.getRecommendedPrice(offer?.itemID ?: 0)
            if (PriceIndex.canTrade(offer?.itemID ?: 0)) {
                lowPrice = (recommendedPrice * 0.95).toInt()
                highPrice = (recommendedPrice * 1.05).toInt()
            }
            setVarp(player, 1109, offer?.itemID ?: -1)
            setVarp(player, 1110, offer?.amount ?: 0)
            setVarp(player, 1111, offer?.offeredValue ?: 0)
            setVarp(player, 1112, index)
            setVarp(player, 1113, if (isSale) 1 else 0)
            setVarp(player, 1114, recommendedPrice)
            setVarp(player, 1115, lowPrice)
            setVarp(player, 1116, highPrice)
            if (offer != null) {
                PacketRepository.send(
                    Encoders578.CONTAINER_PACKET,
                    ContainerContext(player, -1, -1757, 523 + offer.index, offer.withdraw, false)
                )
            }
        }

        /** CS2 scripts 593 and 662 select the collection option order by item quantity. */
        internal fun collectAsNote(canNote: Boolean, amount: Int, operation: Int): Boolean? {
            if (amount <= 0 || operation !in InterfaceOperation.OP1..InterfaceOperation.OP2) return null
            if (!canNote) return if (operation == InterfaceOperation.OP1) false else null
            return if (amount > 1) operation == InterfaceOperation.OP1 else operation == InterfaceOperation.OP2
        }

        /** Historical API uses legacy listener IDs; active interface callers use native indices. */
        @JvmStatic
        fun withdraw(player: Player, offer: GrandExchangeOffer, index: Int, op: Int) {
            val operation = when (op) { 155 -> InterfaceOperation.OP1; 196 -> InterfaceOperation.OP2; else -> return }
            withdrawNative(player, offer, index, operation)
        }

        @JvmStatic
        fun withdrawNative(player: Player, offer: GrandExchangeOffer, index: Int, operation: Int) {
            if (index !in offer.withdraw.indices || offer.index !in 0..5) return
            val record = ExchangeHistory.getInstance(player).offerRecords[offer.index] ?: return
            if (record.uid != offer.uid) return
            val item = offer.withdraw[index] ?: return
            if (item.amount <= 0) return
            val definition = item.definition
            val noteId = definition.noteId
            val canNote = definition.isUnnoted && noteId in 0..65534 && noteId != item.id &&
                !itemDefinition(noteId).isUnnoted && itemDefinition(noteId).noteId == item.id
            val takeNote = collectAsNote(canNote, item.amount, operation) ?: return
            val result = if (takeNote) Item(noteId, item.amount) else item
            if (!hasSpaceFor(player, result) || !addItem(player, result.id, result.amount)) {
                playAudio(player, Sounds.GE_TRADE_ERROR_4039)
                sendMessage(player, "You do not have enough room in your inventory.")
                return
            }

            offer.withdraw[index] = null
            if (!offer.isActive && offer.withdraw[0] == null && offer.withdraw[1] == null) {
                offer.offerState = OfferState.REMOVED
                if (offer.completedAmount > 0) {
                    val newHistory = arrayOfNulls<GrandExchangeOffer>(5)
                    newHistory[0] = offer
                    System.arraycopy(ExchangeHistory.getInstance(player).history, 0, newHistory, 1, 4)
                    ExchangeHistory.getInstance(player).history = newHistory
                }
                ExchangeHistory.getInstance(player).offerRecords[offer.index] = null
                toMainInterface(player)
            }
            offer.update()
            offer.visualize(player)
        }

        /**
         * Returns to the main interface.
         */
        fun toMainInterface(player: Player) {
            setVarp(player, 1112, -1)
            removeAttribute(player, "ge-temp")
            closeChatBox(player)
            closeSingleTab(player)
            setAttribute(player, "ge-index", -1)
        }
    }
}
