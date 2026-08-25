package content.global.skill.crafting

import content.region.misthalin.lumbridge.diary.LumbridgeAchievementDiary
import content.region.misthalin.varrock.diary.VarrockAchievementDiary
import core.api.*
import core.game.interaction.*
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.world.map.Location
import core.tools.StringUtils
import shared.consts.*
import java.util.*

class PotteryPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles use soft clay on pottery wheels.
         */

        onUseWith(IntType.ITEM, Items.SOFT_CLAY_1761, *CraftingDefinition.POTTERY_WHEELS) { player, _, _ ->
            handlePotteryWheel(player)
            return@onUseWith true
        }

        /*
         * Handles unfired pottery in oven.
         */

        onUseWith(IntType.ITEM, CraftingDefinition.UNFIRED_POTTERY_ITEM_IDS, *CraftingDefinition.POTTERY_OVENS) { player, used, oven ->
            val pottery = CraftingDefinition.Pottery.forId(used.id)
                ?: return@onUseWith false

            firePottery(player, pottery, oven)
            return@onUseWith true
        }

        /*
         * Handles use of oven.
         */

        on(CraftingDefinition.POTTERY_OVENS, IntType.SCENERY, "fire") { player, node ->
            handleOvenInteraction(player, node)
            return@on true
        }

        /*
         * Handles cooking range interaction.
         */

        on(CraftingDefinition.RANGE, IntType.SCENERY, "fire") { player, node ->
            if (inInventory(player, Items.UNCOOKED_STEW_2001, 1)) {
                faceLocation(player, node.location)
                openDialogue(player, 43989, Items.UNCOOKED_STEW_2001, "stew")
            }
            return@on true
        }
    }

    private fun handlePotteryWheel(player: Player) {
        val items = CraftingDefinition.Pottery.values()
            .map { it.unfinished }
            .toTypedArray()

        sendSkillDialogue(player) {
            withItems(*items)

            create { itemId, amount ->
                val pottery = CraftingDefinition.Pottery.forId(itemId)
                    ?: return@create

                if (!checkRequirements(player, pottery.level, Items.SOFT_CLAY_1761)) {
                    return@create
                }

                process(
                    player = player,
                    amount = amount,
                    inputId = Items.SOFT_CLAY_1761,
                    output = pottery.unfinished.id,
                    exp = pottery.exp,
                    animation = 883
                ) {
                    handleDiaryWheel(player, pottery)

                    val article =
                        if (StringUtils.isPlusN(pottery.unfinished.name)) "an" else "a"

                    sendMessage(
                        player,
                        "You make the clay into $article ${pottery.unfinished.name.lowercase(Locale.getDefault())}."
                    )
                }
            }

            calculateMaxAmount {
                amountInInventory(player, Items.SOFT_CLAY_1761)
            }
        }
    }

    private fun firePottery(player: Player, pottery: CraftingDefinition.Pottery, oven: Node): Boolean {
        if (oven.id == Scenery.POTTERY_OVEN_4308 && !isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS)) {
            sendMessage(player, "Only Fremenniks may use this ${oven.name.lowercase(Locale.getDefault())}.")
            return false
        }

        if (!checkRequirements(player, pottery.level, pottery.unfinished.id)) {
            return false
        }

        sendSkillDialogue(player) {
            withItems(pottery.unfinished)

            create { _, amount ->
                process(
                    player = player,
                    amount = amount,
                    inputId = pottery.unfinished.id,
                    output = pottery.product.id,
                    exp = pottery.fireExp,
                    animation = Animations.HUMAN_FURNACE_SMELT_3243,
                    audio = Sounds.POTTERY_2588
                ) {
                    sendMessage(player, "You put ${pottery.unfinished.name.lowercase(Locale.getDefault())} in the oven.")
                    sendMessage(player, "You remove ${pottery.product.name.lowercase(Locale.getDefault())} from the oven.")
                    handleDiaryOven(player, pottery)
                }
            }

            calculateMaxAmount {
                player.inventory.getAmount(pottery.unfinished)
            }
        }

        return true
    }

    private fun handleOvenInteraction(player: Player, node: Node) {
        val potteryMap = mapOf(
            Items.UNFIRED_POT_1787 to CraftingDefinition.Pottery.POT,
            Items.UNFIRED_PIE_DISH_1789 to CraftingDefinition.Pottery.DISH,
            Items.UNFIRED_BOWL_1791 to CraftingDefinition.Pottery.BOWL,
            Items.UNFIRED_PLANT_POT_5352 to CraftingDefinition.Pottery.PLANT,
            Items.UNFIRED_POT_LID_4438 to CraftingDefinition.Pottery.LID
        )

        sendSkillDialogue(player) {
            withItems(*potteryMap.keys.toIntArray())

            create { selectedItemId, _ ->
                potteryMap[selectedItemId]?.let {
                    firePottery(player, it, node)
                }
            }

            calculateMaxAmount { selectedItemId ->
                amountInInventory(player, selectedItemId)
            }
        }
    }

    private fun checkRequirements(
        player: Player,
        level: Int,
        itemId: Int
    ): Boolean {
        if (getStatLevel(player, Skills.CRAFTING) < level) {
            sendMessage(player, "You need a Crafting level of $level to do this.")
            return false
        }

        if (!inInventory(player, itemId)) {
            sendMessage(player, "You don't have the required item.")
            return false
        }

        return true
    }

    private fun process(
        player: Player,
        amount: Int,
        inputId: Int,
        output: Int,
        exp: Double,
        animation: Int,
        audio: Int? = null,
        diaryAction: (() -> Unit)? = null
    ) {
        var remaining = amount

        queueScript(player, 0, QueueStrength.WEAK) {

            if (remaining <= 0) {
                return@queueScript stopExecuting(player)
            }

            if (!clockReady(player, Clocks.SKILLING)) {
                return@queueScript stopExecuting(player)
            }

            if (!inInventory(player, inputId)) {
                sendMessage(player, "You have run out of materials.")
                return@queueScript stopExecuting(player)
            }

            audio?.let { playAudio(player, it) }
            animate(player, animation)

            if (!removeItem(player, inputId)) {
                return@queueScript stopExecuting(player)
            }

            addItem(player, output)
            rewardXP(player, Skills.CRAFTING, exp)
            diaryAction?.invoke()

            remaining--

            return@queueScript delayClock(player, Clocks.SKILLING, 5, true)
        }
    }

    private fun handleDiaryWheel(
        player: Player,
        pottery: CraftingDefinition.Pottery
    ) {
        when (pottery) {
            CraftingDefinition.Pottery.BOWL ->
                if (withinDistance(player, Location(3086, 3410, 0))) {
                    setAttribute(
                        player,
                        "/save:diary:varrock:spun-bowl",
                        true
                    )
                }

            CraftingDefinition.Pottery.POT ->
                if (withinDistance(player, Location(3086, 3410, 0))) {
                    finishDiaryTask(
                        player,
                        DiaryType.LUMBRIDGE,
                        0,
                        LumbridgeAchievementDiary.Companion.BeginnerTasks.BARBARIAN_VILLAGE_MAKE_A_POT
                    )

                    setVarbit(player, 4956, 1, true)
                }

            else -> {}
        }
    }

    private fun handleDiaryOven(
        player: Player,
        pottery: CraftingDefinition.Pottery
    ) {
        when (pottery) {
            CraftingDefinition.Pottery.BOWL ->
                if (
                    withinDistance(player, Location(3085, 3408, 0)) &&
                    getAttribute(player, "diary:varrock:spun-bowl", false)
                ) {
                    finishDiaryTask(
                        player,
                        DiaryType.VARROCK,
                        0,
                        VarrockAchievementDiary.Companion.EasyTasks.BARBARIAN_VILLAGE_SPIN_A_BOWL
                    )

                    setVarbit(player, 3995, 1, true)
                }

            CraftingDefinition.Pottery.POT ->
                if (withinDistance(player, Location(3085, 3408, 0))) {
                    finishDiaryTask(
                        player,
                        DiaryType.LUMBRIDGE,
                        0,
                        LumbridgeAchievementDiary.Companion.BeginnerTasks.BARBARIAN_VILLAGE_FIRE_A_POT
                    )

                    setVarbit(player, 4957, 1, true)
                }

            else -> {}
        }
    }
}