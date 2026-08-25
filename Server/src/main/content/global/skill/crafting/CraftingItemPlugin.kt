package content.global.skill.crafting

import content.global.skill.construction.items.NailType
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.Topic
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.tools.END_DIALOGUE
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds
import kotlin.math.min

class CraftingItemPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles crafting the crab equipment.
         */
        onUseWith(IntType.ITEM, Items.CHISEL_1755, *CraftingDefinition.CRAB_ITEM_IDS.keys.toIntArray()) { player, _, used ->
            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val (productId, xp) =
                CraftingDefinition.CRAB_ITEM_IDS[used.id]
                    ?: return@onUseWith true

            val productName = getItemName(productId).lowercase()

            if (!hasLevelDyn(player, Skills.CRAFTING, 15)) {
                sendDialogue(player, "You need a Crafting level of at least 15 in order to do this.")
                return@onUseWith true
            }

            val available = amountInInventory(player, used.id)

            if (available <= 0) {
                sendMessage(player, "You do not have enough ${getItemName(used.id).lowercase()} to craft this.")
                return@onUseWith true
            }

            if (available == 1) {
                if (removeItem(player, used.id)) {
                    addItem(player, productId)
                    rewardXP(player, Skills.CRAFTING, xp)
                    sendMessage(player, "You craft a $productName.")
                    delayClock(player, Clocks.SKILLING, 1)
                }

                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(productId)

                create { _, amount ->
                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!clockReady(player, Clocks.SKILLING)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, used.id)) {
                            sendMessage(player, "You have run out of ${getItemName(used.id).lowercase()}.")
                            return@queueScript stopExecuting(player)
                        }

                        if (removeItem(player, used.id)) {
                            addItem(player, productId)
                            rewardXP(player, Skills.CRAFTING, xp)

                            sendMessage(player, "You craft a $productName.")
                            remaining--
                        }

                        if (
                            remaining > 0 &&
                            inInventory(player, used.id)
                        ) {
                            delayClock(player, Clocks.SKILLING, 1)
                            setCurrentScriptState(player, 0)
                            delayScript(player, 1)
                        } else {
                            stopExecuting(player)
                        }
                    }
                }

                calculateMaxAmount {
                    amountInInventory(player, used.id)
                }
            }

            return@onUseWith true
        }

        /*
         * Handles crafting the Feather headdress hats.
         */
        onUseWith(IntType.ITEM, Items.COIF_1169, *CraftingDefinition.FeatherHeaddress.baseIds) { player, used, _ ->

            val item =
                CraftingDefinition.FeatherHeaddress.forBase(used.id)
                    ?: return@onUseWith false

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            if (!hasLevelDyn(player, Skills.CRAFTING, 79)) {
                sendMessage(player, "You need a Crafting level of at least 79 in order to do this.")
                return@onUseWith true
            }

            val available = amountInInventory(player, item.base) / 20

            if (available <= 0) {
                sendMessage(player, "You don't have enough ${getItemName(item.base).lowercase()} to craft this.")
                return@onUseWith true
            }

            if (available == 1) {
                if (removeItem(player, Item(item.base, 20))) {
                    addItem(player, item.product)
                    rewardXP(player, Skills.CRAFTING, 50.0)

                    sendMessage(player, "You add the feathers to the coif to make a feathered headdress.")
                    delayClock(player, Clocks.SKILLING, 1)
                }

                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(item.product)

                create { _, amount ->
                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!clockReady(player, Clocks.SKILLING)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (amountInInventory(player, item.base) < 20) {
                            sendMessage(player, "You don't have enough materials to continue crafting.")
                            return@queueScript stopExecuting(player)
                        }

                        if (removeItem(player, Item(item.base, 20))) {
                            addItem(player, item.product)
                            rewardXP(player, Skills.CRAFTING, 50.0)
                            sendMessage(player, "You add the feathers to the coif to make a feathered headdress.")
                            remaining--
                        }

                        if (
                            remaining > 0 &&
                            amountInInventory(player, item.base) >= 20
                        ) {
                            delayClock(player, Clocks.SKILLING, 1)
                            setCurrentScriptState(player, 0)
                            delayScript(player, 1)
                        } else {
                            stopExecuting(player)
                        }
                    }
                }

                calculateMaxAmount {
                    amountInInventory(player, item.base) / 20
                }
            }

            return@onUseWith true
        }

        /*
         * Handles crafting the snelm helmets.
         */
        onUseWith(IntType.ITEM, Items.CHISEL_1755, *CraftingDefinition.SnelmItem.SHELLS) { player, _, used ->
            val snelm = CraftingDefinition.SnelmItem.fromShellId(used.id) ?: return@onUseWith true
            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            if (!hasLevelDyn(player, Skills.CRAFTING, 15)) {
                sendMessage(player, "You need a Crafting level of at least 15 to do this.")
                return@onUseWith true
            }

            val available = amountInInventory(player, snelm.shell)

            if (available <= 0) {
                sendMessage(player, "You do not have enough ${getItemName(snelm.shell).lowercase()} to make this.")
                return@onUseWith true
            }

            if (available == 1) {
                if (removeItem(player, snelm.shell)) {
                    addItem(player, snelm.product)
                    rewardXP(player, Skills.CRAFTING, 32.5)

                    sendMessage(player, "You craft the shell into a helmet.")

                    delayClock(player, Clocks.SKILLING, 1)
                }

                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(snelm.product)

                create { _, amount ->
                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!clockReady(player, Clocks.SKILLING)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, snelm.shell)) {
                            sendMessage(player, "You have run out of ${getItemName(snelm.shell).lowercase()}.")
                            return@queueScript stopExecuting(player)
                        }

                        if (removeItem(player, snelm.shell)) {
                            addItem(player, snelm.product)
                            rewardXP(player, Skills.CRAFTING, 32.5)

                            sendMessage(player, "You craft the shell into a helmet.")

                            remaining--
                        }

                        if (
                            remaining > 0 &&
                            inInventory(player, snelm.shell)
                        ) {
                            delayClock(player, Clocks.SKILLING, 1)
                            setCurrentScriptState(player, 0)
                            delayScript(player, 1)
                        } else {
                            stopExecuting(player)
                        }
                    }
                }

                calculateMaxAmount {
                    amountInInventory(player, snelm.shell)
                }
            }

            return@onUseWith true
        }

        /*
         * Handles crafting the battlestaves.
         */

        onUseWith(IntType.ITEM, CraftingDefinition.Battlestaff.ORB_ID, CraftingDefinition.Battlestaff.BATTLESTAFF_ID) { player, used, with ->

            val product =
                CraftingDefinition.Battlestaff.forId(used.id)
                    ?: return@onUseWith true

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            if (!hasLevelDyn(player, Skills.CRAFTING, product.requiredLevel)) {
                sendMessage(player, "You need a Crafting level of ${product.requiredLevel} to make this.")
                return@onUseWith true
            }

            val maxAmount = min(
                amountInInventory(player, product.required),
                amountInInventory(player, CraftingDefinition.Battlestaff.BATTLESTAFF_ID)
            )

            if (maxAmount <= 0) {
                sendMessage(player, "You don't have the required materials.")
                return@onUseWith true
            }

            if (maxAmount == 1) {
                if (
                    removeItem(player, product.required) &&
                    removeItem(player, CraftingDefinition.Battlestaff.BATTLESTAFF_ID)
                ) {
                    playAudio(player, Sounds.ATTACH_ORB_2585)
                    addItem(player, product.productId, product.amount)
                    rewardXP(player, Skills.CRAFTING, product.experience)
                    handleBattlestaffDiary(player, product)
                    delayClock(player, Clocks.SKILLING, 1)
                }

                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(product.productId)

                create { _, amount ->
                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!clockReady(player, Clocks.SKILLING)) {
                            return@queueScript stopExecuting(player)
                        }

                        val available = min(
                            amountInInventory(player, product.required),
                            amountInInventory(
                                player,
                                CraftingDefinition.Battlestaff.BATTLESTAFF_ID
                            )
                        )

                        if (available <= 0) {
                            sendMessage(
                                player,
                                "You have run out of battlestaves or orbs."
                            )
                            return@queueScript stopExecuting(player)
                        }

                        if (
                            removeItem(player, product.required) &&
                            removeItem(player, CraftingDefinition.Battlestaff.BATTLESTAFF_ID)
                        ) {
                            playAudio(player, Sounds.ATTACH_ORB_2585)
                            addItem(player, product.productId, product.amount)
                            rewardXP(player, Skills.CRAFTING, product.experience)
                            handleBattlestaffDiary(player, product)
                            remaining--
                        }

                        if (remaining > 0) {
                            delayClock(player, Clocks.SKILLING, 1)
                            setCurrentScriptState(player, 0)
                            delayScript(player, 1)
                        } else {
                            stopExecuting(player)
                        }
                    }
                }

                calculateMaxAmount {
                    min(
                        amountInInventory(player, product.required),
                        amountInInventory(
                            player,
                            CraftingDefinition.Battlestaff.BATTLESTAFF_ID
                        )
                    )
                }
            }

            return@onUseWith true
        }

        /*
         * Handles crafting broodo shields.
         */
        onUseWith(IntType.ITEM, Items.HAMMER_2347, *CraftingDefinition.TRIBAL_ITEM_IDS.keys.toIntArray()) { player, _, with ->
            val maskId = with.id
            val shieldId =
                CraftingDefinition.TRIBAL_ITEM_IDS[maskId]
                    ?: return@onUseWith false

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            if (getStatLevel(player, Skills.CRAFTING) < 35) {
                sendMessage(player, "You don't have the crafting level needed to do that.")
                return@onUseWith false
            }

            if (!inInventory(player, Items.SNAKESKIN_6289, 2)) {
                sendMessage(player, "You don't have enough snakeskins.")
                return@onUseWith false
            }

            val totalNails =
                NailType.values.sumOf {
                    player.inventory.getAmount(Item(it.itemId))
                }

            if (totalNails < 8) {
                sendMessage(player, "You don't have enough nails.")
                return@onUseWith true
            }

            val hasCheapNails =
                NailType.values.any {
                    it.ordinal <= NailType.STEEL.ordinal &&
                            player.inventory.getAmount(Item(it.itemId)) > 0
                }

            val hasExpensiveNails =
                NailType.values.any {
                    it.ordinal > NailType.STEEL.ordinal &&
                            player.inventory.getAmount(Item(it.itemId)) > 0
                }

            if (!hasCheapNails && hasExpensiveNails) {
                object : DialogueFile() {

                    override fun handle(componentID: Int, buttonID: Int) {
                        when (stage) {

                            0 -> {
                                sendDoubleItemDialogue(
                                    player,
                                    Items.BLACK_NAILS_4821,
                                    Items.RUNE_NAILS_4824,
                                    "Using these nails will consume higher value nails. Are you sure?"
                                )
                                stage++
                            }

                            1 -> showTopics(
                                Topic(
                                    "Yes, use the high-value nails.",
                                    2
                                ),
                                Topic(
                                    "No, I'll get cheaper nails.",
                                    END_DIALOGUE
                                )
                            )

                            2 -> {
                                end()
                                craftBroodoShield(
                                    player,
                                    maskId,
                                    shieldId
                                )
                            }
                        }
                    }
                }
                return@onUseWith true
            }

            craftBroodoShield(player, maskId, shieldId)

            return@onUseWith true
        }
    }

    private fun handleBattlestaffDiary(player: Player, product: CraftingDefinition.Battlestaff) {
        if (product.productId == Items.AIR_BATTLESTAFF_1397) {
            finishDiaryTask(player, DiaryType.VARROCK, 2, 6)
            setVarbit(player, 4033, 1, true)
        }
    }

    private fun craftBroodoShield(player: Player, maskId: Int, shieldId: Int) {
        if (!inInventory(player, maskId)) {
            sendMessage(player, "You don't have the required mask.")
            return
        }

        if (!inInventory(player, Items.SNAKESKIN_6289, 2)) {
            sendMessage(player, "You don't have enough snakeskins.")
            return
        }

        val nailsAvailable =
            NailType.values.sumOf {
                player.inventory.getAmount(Item(it.itemId))
            }

        if (nailsAvailable < 8) {
            sendMessage(player, "You don't have enough nails.")
            return
        }

        if (!removeItem(player, maskId)) {
            return
        }

        if (!removeItem(player, Item(Items.SNAKESKIN_6289, 2))) {
            return
        }

        var remainingNails = 8

        for (type in NailType.values) {
            if (remainingNails <= 0) {
                break
            }

            val available = player.inventory.getAmount(
                Item(type.itemId)
            )

            if (available > 0) {
                val amount = min(available, remainingNails)

                removeItem(
                    player,
                    Item(type.itemId, amount)
                )

                remainingNails -= amount
            }
        }

        if (remainingNails > 0) {
            return
        }

        val animation = when (maskId) {
            Items.TRIBAL_MASK_6335 ->
                Animations.CRAFT_SHIELD_GREEN_2410

            Items.TRIBAL_MASK_6337 ->
                Animations.CRAFT_SHIELD_ORANGE_2411

            Items.TRIBAL_MASK_6339 ->
                Animations.CRAFT_SHIELD_WHITE_2409

            else ->
                Animations.CRAFT_SHIELD_GREEN_2410
        }

        animate(player, animation)
        addItemOrDrop(player, shieldId, 1)
        rewardXP(player, Skills.CRAFTING, 100.0)
        delayClock(player, Clocks.SKILLING, 1)
    }
}