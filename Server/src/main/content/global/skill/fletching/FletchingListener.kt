package content.global.skill.fletching

import content.global.skill.slayer.SlayerManager.Companion.getInstance
import core.api.*
import core.game.dialogue.SkillDialogueHandler
import core.game.dialogue.SkillDialogueHandler.SkillDialogue
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.zone.ZoneBorders
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import core.tools.StringUtils
import shared.consts.*
import kotlin.math.min

class FletchingListener : InteractionListener {
    override fun defineListeners() {

        /*
         * Handles fletching logs using a knife.
         */

        onUseWith(IntType.ITEM, Items.KNIFE_946, *FletchingDefinition.FLETCH_LOGS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val options = FletchingDefinition.getItems(with.id)
                ?: return@onUseWith true

            val dialogueType =
                when (options.size) {
                    1 -> SkillDialogue.ONE_OPTION
                    2 -> SkillDialogue.TWO_OPTION
                    3 -> SkillDialogue.THREE_OPTION
                    4 -> SkillDialogue.FOUR_OPTION
                    else -> SkillDialogue.ONE_OPTION
                }

            val handler =
                object : SkillDialogueHandler(player, dialogueType, *options) {
                    override fun create(amount: Int, index: Int) {
                        val entry =
                            FletchingDefinition.getEntries(with.id)?.get(index)
                                ?: return

                        if (amount <= 0) return

                        var remaining = amount

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, with.id, 1)) {
                                sendMessage(
                                    player,
                                    "You have run out of ${getItemName(with.id).lowercase()}."
                                )
                                return@queueScript stopExecuting(player)
                            }

                            if (
                                getStatLevel(
                                    player,
                                    Skills.FLETCHING
                                ) < entry.level
                            ) {
                                val name =
                                    getItemName(entry.id)
                                        .replace("(u)", "")
                                        .trim()

                                sendDialogue(player, "You need a Fletching level of ${entry.level} to make " +
                                            (if (StringUtils.isPlusN(name)) "an " else "a ") +
                                            "$name.")

                                return@queueScript stopExecuting(player)
                            }

                            /*
                             * Composite ogre bow requires wolf bones.
                             */

                            if (entry.id == Items.UNSTRUNG_COMP_BOW_4825) {
                                if (!inInventory(player, Items.WOLF_BONES_2859, 1)) {
                                    sendMessage(
                                        player,
                                        "You need wolf bones to craft this bow."
                                    )
                                    return@queueScript stopExecuting(player)
                                }
                            }

                            val anim =
                                if (with.id == Items.MAGIC_LOGS_1513) {
                                    Animation(Animations.CUT_MAGIC_LOGS_7211)
                                } else {
                                    Animation(Animations.FLETCH_LOGS_1248)
                                }

                            player.animate(anim)

                            /*
                             * Consume the logs.
                             */
                            if (!removeItem(player, with.id)) {
                                return@queueScript stopExecuting(player)
                            }

                            /*
                             * Composite ogre bow additionally consumes wolf bones.
                             */
                            if (entry.id == Items.UNSTRUNG_COMP_BOW_4825) {
                                if (!removeItem(player, Items.WOLF_BONES_2859)) {
                                    return@queueScript stopExecuting(player)
                                }
                            }

                            val product =
                                Item(entry.id, entry.amount)

                            when (entry.id) {

                                Items.OGRE_ARROW_SHAFT_2864 -> {
                                    val amt = RandomFunction.random(2, 6)

                                    product.amount = amt

                                    sendMessage(
                                        player,
                                        "You carefully cut the logs into $amt arrow shafts."
                                    )
                                }

                                Items.UNSTRUNG_COMP_BOW_4825 -> {
                                    sendMessage(
                                        player,
                                        "You carefully craft a composite ogre bow."
                                    )
                                }

                                else -> {
                                    val name =
                                        getItemName(entry.id)
                                            .replace("(u)", "")
                                            .trim()

                                    sendMessage(
                                        player,
                                        "You carefully cut the logs into " +
                                                "${if (StringUtils.isPlusN(name)) "an" else "a"} $name."
                                    )
                                }
                            }

                            addItem(player, product.id, product.amount)
                            rewardXP(player, Skills.FLETCHING, entry.xp)

                            remaining--
                            return@queueScript delayClock(player, Clocks.SKILLING, 3, true)
                        }
                    }

                    override fun getAll(index: Int): Int {
                        return amountInInventory(player, with.id)
                    }
                }

            if (options.size == 1) {
                handler.create(handler.getAll(0), 0)
            } else {
                handler.open()
            }
            return@onUseWith true
        }

        /*
         * Handles attaching a string to an unstrung bow.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.STRING_IDS, *FletchingDefinition.UNF_BOW_IDS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val enum =
                FletchingDefinition.Strings.product[with.id]
                    ?: return@onUseWith false

            sendSkillDialogue(player) {
                withItems(enum.bowId)

                create { _, amount ->

                    if (amount <= 0) {
                        return@create
                    }

                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }
                        if (enum.string != used.id) {
                            sendMessage(player, "That's not the right kind of string for this.")
                            return@queueScript stopExecuting(player)
                        }

                        if (getStatLevel(player, Skills.FLETCHING) < enum.level) {
                            sendDialogue(player, "You need a Fletching level of ${enum.level} to string this bow.")
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, enum.unfinished, 1)) {
                            sendDialogue(player, "You have run out of bows to string.")
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, enum.string, 1)) {
                            sendDialogue(
                                player,
                                "You seem to have run out of bow strings."
                            )
                            return@queueScript stopExecuting(player)
                        }

                        player.animate(Animation.create(enum.animation))
                        playAudio(player, Sounds.STRING_BOW_2606)

                        if (!removeItem(player, enum.unfinished)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!removeItem(player, enum.string)) {
                            return@queueScript stopExecuting(player)
                        }

                        addItem(player, enum.bowId)
                        rewardXP(player, Skills.FLETCHING, enum.xp)
                        sendMessage(player, "You add a string to the bow.")

                        /*
                         * Seers' Village diary.
                         */
                        if (
                            enum == FletchingDefinition.Strings.MAGIC_SHORTBOW &&
                            (
                                    ZoneBorders(2721, 3489, 2724, 3493, 0).insideBorder(player) ||
                                            ZoneBorders(2727, 3487, 2730, 3490, 0).insideBorder(player)
                                    )
                        ) {
                            finishDiaryTask(player, DiaryType.SEERS_VILLAGE, 2, 2)

                            setVarbit(player, 5810, 1, true)
                        }

                        remaining--
                        return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                    }
                }

                calculateMaxAmount {
                    amountInInventory(player, used.id)
                }
            }

            return@onUseWith true
        }

        /*
         * Handles attaching arrowheads to headless arrows to create arrows.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.ARROW_SHAFT, *FletchingDefinition.FEATHER_IDS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(FletchingDefinition.HEADLESS_ARROW)) {

                    override fun create(amount: Int, index: Int) {

                        if (amount <= 0) return

                        var remaining = amount * 15

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, used.id, 1)) {
                                sendMessage(player, "You don't have any arrow shafts.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, with.id, 1)) {
                                sendMessage(player, "You don't have any feathers.")
                                return@queueScript stopExecuting(player)
                            }

                            val shaftAmount =
                                amountInInventory(player, used.id)

                            val featherAmount =
                                amountInInventory(player, with.id)

                            val batch =
                                min(15, min(shaftAmount, featherAmount))

                            val realBatch =
                                min(batch, remaining)

                            if (realBatch <= 0) {
                                return@queueScript stopExecuting(player)
                            }
                            if (!removeItem(player, Item(used.id, realBatch))) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!removeItem(player, Item(with.id, realBatch))) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, FletchingDefinition.HEADLESS_ARROW, realBatch)
                            rewardXP(player, Skills.FLETCHING, realBatch.toDouble())

                            val message =
                                if (realBatch == 1) {
                                    "You attach a feather to a shaft."
                                } else {
                                    "You attach feathers to $realBatch arrow shafts."
                                }

                            sendMessage(player, message)
                            remaining -= realBatch

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int): Int {
                        val possible = min(amountInInventory(player, used.id), amountInInventory(player, with.id))
                        return possible / 15
                    }
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(0, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles creating mithril grapple base by attaching mithril bolts to grapple tips.
         */

        onUseWith(IntType.ITEM, Items.MITHRIL_BOLTS_9142, Items.MITH_GRAPPLE_TIP_9416) { player, used, with ->
            if (getStatLevel(player, Skills.FLETCHING) < 59) {
                sendMessage(player, "You need a Fletching level of 59 to make this.")
                return@onUseWith true
            }

            if (removeItem(player, used.asItem()) && removeItem(player, with.asItem())) {
                addItem(player, Items.MITH_GRAPPLE_9418, 1)
                sendMessage(player, "You attach the grapple tip to the bolt.")
            } else {
                sendMessage(player, "You don't have the required items.")
            }
            return@onUseWith true
        }

        /*
         * Handles attaching a rope to a mithril grapple base to create a mithril grapple.
         */

        onUseWith(IntType.ITEM, Items.ROPE_954, Items.MITH_GRAPPLE_9418) { player, used, with ->
            if (getStatLevel(player, Skills.FLETCHING) < 59) {
                sendMessage(player, "You need a Fletching level of 59 to make this.")
                return@onUseWith true
            }

            if (removeItem(player, used.asItem()) && removeItem(player, with.asItem())) {
                addItem(player, Items.MITH_GRAPPLE_9419, 1)
                sendMessage(player, "You tie the rope to the grapple.")
            } else {
                sendMessage(player, "You don't have the required items.")
            }
            return@onUseWith true
        }

        /*
         * Handles attaching a crossbow limb to a stock to create an unstrung crossbow.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.LIMB_IDS, *FletchingDefinition.STOCK_IDS) { player, used, with ->

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val limbEnum =
                FletchingDefinition.Limb.product[with.id]
                    ?: return@onUseWith true

            if (limbEnum.limb != used.id) {
                sendMessage(player, "That's not the right limb to attach to that stock.")
                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(limbEnum.cbowId)

                create { _, amount ->

                    if (amount <= 0) return@create

                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (getStatLevel(player, Skills.FLETCHING) < limbEnum.level) {
                            sendDialogue(player, "You need a Fletching level of ${limbEnum.level} to attach these limbs.")
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, limbEnum.limb, 1)) {
                            sendMessage(player, "You have run out of ${getItemName(limbEnum.limb).lowercase()}.")
                            return@queueScript stopExecuting(player)
                        }

                        if (!inInventory(player, limbEnum.stock, 1)) {
                            sendMessage(player, "You have run out of ${getItemName(limbEnum.stock).lowercase()}.")
                            return@queueScript stopExecuting(player)
                        }

                        playAudio(player, Sounds.STRING_CROSSBOW_2924)
                        player.animate(Animation.create(limbEnum.animation))

                        if (!removeItem(player, limbEnum.limb)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!removeItem(player, limbEnum.stock)) {
                            return@queueScript stopExecuting(player)
                        }

                        addItem(player, limbEnum.cbowId)
                        rewardXP(player, Skills.FLETCHING, limbEnum.xp)
                        sendMessage(player, "You attach the metal limbs to the stock.")

                        remaining--

                        return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                    }
                }

                calculateMaxAmount {
                    amountInInventory(player, used.id)
                }
            }

            return@onUseWith true
        }

        /*
         * Handles chiseling gems into bolt tips.
         */

        onUseWith(IntType.ITEM, Items.CHISEL_1755, *FletchingDefinition.BOLT_GEM_IDS) { player, _, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            val gem = FletchingDefinition.GemBolt.gemToBolt[with.id] ?: return@onUseWith true

            sendString(player, "How many gems would you like to cut into bolt tips?", Components.SKILL_MULTI1_309, 7)

            sendSkillDialogue(player) {
                withItems(gem.tip)

                create { _, amount ->
                    var remaining = amount
                    queueScript(player, 0, QueueStrength.WEAK) {
                        val currentAmount = amountInInventory(player, gem.gem)
                        if (remaining <= 0 || currentAmount <= 0) return@queueScript false

                        if (getStatLevel(player, Skills.FLETCHING) < gem.level) {
                            sendDialogue(player, "You need a Fletching level of ${gem.level} or above to do that.")
                            return@queueScript false
                        }

                        playAudio(player, Sounds.CHISEL_2586)
                        animate(player, gem.animation)

                        val rewardAmount = when (gem.gem) {
                            Items.OYSTER_PEARLS_413, Items.ONYX_6573 -> 24
                            Items.OYSTER_PEARL_411 -> 6
                            else -> 12
                        }

                        if (removeItem(player, gem.gem)) {
                            addItem(player, gem.tip, rewardAmount)
                            rewardXP(player, Skills.FLETCHING, gem.xp)
                            sendMessage(player, "You use your chisel to fetch small bolt tips.")
                            remaining--
                        }

                        return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                    }
                }
            }

            return@onUseWith true
        }

        /*
         * Handles attaching gem bolt tips to bolt bases to create gem-tipped bolts.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.GEM_BOLT_IDS, *FletchingDefinition.GEM_BOLT_TIPS_IDS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            val bolt = FletchingDefinition.GemBolt.forId(with.id) ?: return@onUseWith true

            if (used.id != bolt.base || with.id != bolt.tip) {
                return@onUseWith true
            }

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(bolt.boltTipId)) {

                    fun getMaxAmount(): Int {
                        return min(
                            amountInInventory(player, bolt.base),
                            amountInInventory(player, bolt.tip)
                        )
                    }

                    override fun create(amount: Int, index: Int) {
                        var remaining = amount * 10

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!clockReady(player, Clocks.SKILLING)) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < bolt.level) {
                                sendDialogue(player, "You need a Fletching level of ${bolt.level} or above to do that.")
                                return@queueScript stopExecuting(player)
                            }

                            val available = getMaxAmount()

                            if (available <= 0) {
                                sendMessage(player, "You have run out of materials.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(bolt.boltTipId))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val batch = min(10, min(available, remaining))

                            if (batch <= 0) {
                                return@queueScript stopExecuting(player)
                            }


                            val removedBase = removeItem(player, Item(bolt.base, batch))

                            val removedTip = removeItem(player, Item(bolt.tip, batch))

                            if (!removedBase || !removedTip) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, bolt.boltTipId, batch)
                            rewardXP(player, Skills.FLETCHING, bolt.xp * batch)

                            sendMessage(
                                player,
                                if (batch == 1) {
                                    "You attach the tip to the bolt."
                                } else {
                                    "You fletch $batch bolts."
                                }
                            )

                            remaining -= batch

                            if(amountInInventory(player, bolt.base) <= 0 ||
                                amountInInventory(player, bolt.tip) <= 0){
                                return@queueScript stopExecuting(player)
                            }
                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int): Int {
                        return getMaxAmount() / 10
                    }
                }

            val maxAmount = handler.getMaxAmount()

            if (maxAmount < 10) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles attaching kebbit spikes to create kebbit bolts.
         */

        onUseWith(IntType.ITEM, Items.CHISEL_1755, *FletchingDefinition.KEBBIT_SPIKE_IDS) { player, _, base ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val kebbit = FletchingDefinition.KebbitBolt.forId(base.asItem())
                ?: return@onUseWith true

            sendSkillDialogue(player) {
                withItems(kebbit.product)

                create { _, amount ->
                    var remaining = amount

                    queueScript(player, 0, QueueStrength.WEAK) {
                        if (remaining <= 0) {
                            return@queueScript stopExecuting(player)
                        }

                        if (!clockReady(player, Clocks.SKILLING)) {
                            return@queueScript stopExecuting(player)
                        }

                        if (getStatLevel(player, Skills.FLETCHING) < kebbit.level) {
                            sendDialogue(player, "You need a Fletching level of ${kebbit.level} to do this.")
                            return@queueScript stopExecuting(player)
                        }

                        if (amountInInventory(player, kebbit.base) <= 0) {
                            sendMessage(player, "You do not have enough materials to make kebbit bolts.")
                            return@queueScript stopExecuting(player)
                        }

                        if (!hasSpaceFor(player, Item(kebbit.product, 6))) {
                            sendDialogue(player, "You do not have enough inventory space.")
                            return@queueScript stopExecuting(player)
                        }

                        val batch = 1

                        player.animate(Animation(Animations.FLETCH_LOGS_4433))

                        if (!removeItem(player, Item(kebbit.base, batch))) {
                            return@queueScript stopExecuting(player)
                        }

                        addItem(player, kebbit.product, 6)
                        rewardXP(player, Skills.FLETCHING, kebbit.xp)

                        sendMessage(
                            player,
                            "You fletch 6 ${getItemName(kebbit.product).lowercase()}s."
                        )

                        remaining--

                        return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                    }
                }
            }

            return@onUseWith true
        }


        /*
         * Handles attaching the barb bolt tips with bronze bolts to create barbed bolts.
         */

        onUseWith(IntType.ITEM, Items.BARB_BOLTTIPS_47, Items.BRONZE_BOLTS_877) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val handler =
                object : SkillDialogueHandler(
                    player,
                    SkillDialogue.MAKE_SET_ONE_OPTION,
                    Item(Items.BARBED_BOLTS_881)
                ) {

                    fun getMaxAmount() =
                        min(
                            amountInInventory(player, used.id),
                            amountInInventory(player, with.id)
                        )

                    override fun create(amount: Int, index: Int) {
                        var remaining = amount * 10

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!clockReady(player, Clocks.SKILLING)) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < 51) {
                                sendMessage(player, "You need a Fletching level of 51 to do this.")
                                return@queueScript stopExecuting(player)
                            }

                            val tips = amountInInventory(player, used.id)
                            val bolts = amountInInventory(player, with.id)

                            if (tips <= 0) {
                                sendMessage(player, "You have run out of barbed bolt tips.")
                                return@queueScript stopExecuting(player)
                            }

                            if (bolts <= 0) {
                                sendMessage(player, "You have run out of bronze bolts.")
                                return@queueScript stopExecuting(player)
                            }

                            val batch = min(
                                10,
                                min(
                                    min(tips, bolts),
                                    remaining
                                )
                            )

                            if (batch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(Items.BARBED_BOLTS_881, batch))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val removedTips = removeItem(player, Item(used.id, batch))
                            val removedBolts = removeItem(player, Item(with.id, batch))

                            if (!removedTips || !removedBolts) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, Items.BARBED_BOLTS_881, batch)
                            rewardXP(player, Skills.FLETCHING, 9.5 * batch)
                            sendMessage(player, "You attach $batch barbed tips to the bronze bolts.")

                            remaining -= batch

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int) =
                        getMaxAmount() / 10
                }

            val maxAmount = handler.getMaxAmount()

            if (maxAmount < 10) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }


        /*
         * Handles attaching the ogre arrow shafts and feathers to create flighted ogre arrows.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.OGRE_ARROW_SHAFT, *FletchingDefinition.FEATHER_IDS) { player, used, _ ->

            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(FletchingDefinition.FLIGHTED_OGRE_ARROW)) {

                    fun getMaxAmount(): Int {
                        val shafts = amountInInventory(player, used.id)
                        val feathers =
                            FletchingDefinition.FEATHER_IDS.sumOf {
                                amountInInventory(player, it)
                            }

                        return min(shafts, feathers)
                    }

                    override fun create(amount: Int, index: Int) {
                        var remaining = amount * 4

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!clockReady(player, Clocks.SKILLING)) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < 5) {
                                sendDialogue(player, "You need a Fletching level of 5 to do this.")
                                return@queueScript stopExecuting(player)
                            }

                            val shaftAmount = amountInInventory(player, used.id)
                            val featherAmount =
                                FletchingDefinition.FEATHER_IDS.sumOf {
                                    amountInInventory(player, it)
                                }

                            if (shaftAmount <= 0) {
                                sendMessage(player, "You have run out of ogre arrow shafts.")
                                return@queueScript stopExecuting(player)
                            }

                            if (featherAmount <= 0) {
                                sendMessage(player, "You have run out of feathers.")
                                return@queueScript stopExecuting(player)
                            }

                            val batch = min(4, min(min(shaftAmount, featherAmount), remaining))

                            if (batch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(FletchingDefinition.FLIGHTED_OGRE_ARROW, batch))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val featherId =
                                FletchingDefinition.FEATHER_IDS.firstOrNull {
                                    amountInInventory(player, it) > 0
                                } ?: run {
                                    sendMessage(player, "You have run out of feathers.")
                                    return@queueScript stopExecuting(player)
                                }

                            val actualBatch = min(batch, min(shaftAmount, amountInInventory(player, featherId)))

                            if (actualBatch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            val removedShafts =
                                removeItem(player, Item(used.id, actualBatch))

                            val removedFeathers =
                                removeItem(player, Item(featherId, actualBatch))

                            if (!removedShafts || !removedFeathers) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, FletchingDefinition.FLIGHTED_OGRE_ARROW, actualBatch)
                            rewardXP(player, Skills.FLETCHING, 5.4 * actualBatch)
                            sendMessage(player, "You attach $actualBatch feathers to the ogre arrow shafts.")

                            remaining -= actualBatch

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int) =
                        getMaxAmount() / 4
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }


        /*
         * Handles attaching wolfbone arrow tips to flighted ogre arrows to create ogre arrows.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.WOLFBONE_ARROWTIP, FletchingDefinition.FLIGHTED_OGRE_ARROW) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(Items.OGRE_ARROW_2866, 5)) {

                    fun getMaxAmount() =
                        min(
                            amountInInventory(player, used.id),
                            amountInInventory(player, with.id)
                        )

                    override fun create(amount: Int, index: Int) {
                        var remaining = amount * 6

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!clockReady(player, Clocks.SKILLING)) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < 5) {
                                sendDialogue(player, "You need a Fletching level of 5 to do this.")
                                return@queueScript stopExecuting(player)
                            }

                            val tips = amountInInventory(player, used.id)
                            val arrows = amountInInventory(player, with.id)

                            if (tips <= 0) {
                                sendMessage(player, "You have run out of wolfbone arrow tips.")
                                return@queueScript stopExecuting(player)
                            }

                            if (arrows <= 0) {
                                sendMessage(player, "You have run out of flighted ogre arrows.")
                                return@queueScript stopExecuting(player)
                            }

                            val batch = min(6, min(min(tips, arrows), remaining))

                            if (batch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(FletchingDefinition.OGRE_ARROW, batch))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val removedTips =
                                removeItem(player, Item(used.id, batch))

                            val removedArrows =
                                removeItem(player, Item(with.id, batch))

                            if (!removedTips || !removedArrows) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, FletchingDefinition.OGRE_ARROW, batch)
                            rewardXP(player, Skills.FLETCHING, 6.0 * batch)
                            sendMessage(player, "You make $batch ogre arrows.")

                            remaining -= batch

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int) =
                        getMaxAmount() / 6
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles attaching nails to arrow shafts to create brutal arrows.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.FLIGHTED_OGRE_ARROW, *FletchingDefinition.NAIL_IDS) {
                player,
                _,
                with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val brutalArrow = FletchingDefinition.BrutalArrow.product[with.id]
                ?: return@onUseWith true

            val baseId = Items.FLIGHTED_OGRE_ARROW_2865
            val nailId = with.id

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(brutalArrow.product)) {

                    fun getMaxAmount() =
                        min(
                            amountInInventory(player, baseId),
                            amountInInventory(player, nailId)
                        )

                    override fun create(amount: Int, index: Int) {
                        var remaining = amount * 6

                        queueScript(player, 0, QueueStrength.WEAK) {
                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!clockReady(player, Clocks.SKILLING)) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < brutalArrow.level) {
                                sendDialogue(player, "You need a Fletching level of ${brutalArrow.level} to do this.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, Items.HAMMER_2347)) {
                                sendMessage(player, "You need a hammer to attach nails to these arrows.")
                                return@queueScript stopExecuting(player)
                            }

                            val availableBase = amountInInventory(player, baseId)
                            val availableNails = amountInInventory(player, nailId)

                            if (availableBase <= 0) {
                                sendMessage(player, "You have run out of flighted ogre arrows.")
                                return@queueScript stopExecuting(player)
                            }

                            if (availableNails <= 0) {
                                sendMessage(
                                    player,
                                    "You have run out of ${getItemName(nailId).lowercase()}."
                                )
                                return@queueScript stopExecuting(player)
                            }

                            val batchAmount = min(6, min(min(availableBase, availableNails), remaining))

                            if (batchAmount <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(brutalArrow.product, batchAmount))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val removedBase = removeItem(player, Item(baseId, batchAmount))
                            val removedNails = removeItem(player, Item(nailId, batchAmount))

                            if (!removedBase || !removedNails) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, brutalArrow.product, batchAmount)
                            rewardXP(player, Skills.FLETCHING, brutalArrow.xp * batchAmount)

                            val message =
                                if (batchAmount == 1) {
                                    "You attach the ${getItemName(nailId).lowercase()} to the flighted ogre arrow."
                                } else {
                                    "You fletch $batchAmount ${getItemName(brutalArrow.product).lowercase()} arrows."
                                }

                            sendMessage(player, message)

                            remaining -= batchAmount

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int) =
                        getMaxAmount() / 6
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles attaching feathers to darts to create darts.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.FEATHER_IDS, *FletchingDefinition.UNF_DARTS) { player, used, with ->

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val dart =
                FletchingDefinition.Dart.product[used.id]
                    ?: FletchingDefinition.Dart.product[with.id]
                    ?: return@onUseWith true

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(dart.dartId)) {

                    fun getMaxAmount(): Int {
                        return min(
                            amountInInventory(player, dart.dartTipId),
                            FletchingDefinition.getFeatherAmount(player)
                        )
                    }

                    override fun create(amount: Int, index: Int) {
                        if (amount <= 0) return
                        var remaining = amount * 10
                        queueScript(player, 0, QueueStrength.WEAK) {

                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < dart.level) {
                                sendDialogue(player, "You need a Fletching level of ${dart.level} to do this.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!isQuestComplete(player, Quests.THE_TOURIST_TRAP)) {
                                sendDialogue(player, "You need to have completed Tourist Trap to fletch darts.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, dart.dartTipId, 1)) {
                                sendMessage(player, "You have run out of dart tips.")
                                return@queueScript stopExecuting(player)
                            }

                            if (FletchingDefinition.getFeatherAmount(player) <= 0) {
                                sendMessage(player, "You have run out of feathers.")
                                return@queueScript stopExecuting(player)
                            }

                            val unfinishedAmount =
                                amountInInventory(player, dart.dartTipId)

                            val featherAmount =
                                FletchingDefinition.getFeatherAmount(player)

                            val batch =
                                min(10, min(unfinishedAmount, featherAmount))

                            val realBatch =
                                min(batch, remaining)

                            if (realBatch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!removeItem(player, Item(dart.dartTipId, realBatch))) {
                                return@queueScript stopExecuting(player)
                            }

                            var toRemove = realBatch

                            for (id in FletchingDefinition.getFeatherPriorityOrder()) {
                                if (toRemove <= 0) break

                                val have =
                                    amountInInventory(player, id)

                                if (have > 0) {
                                    val removeCount = min(have, toRemove)
                                    if (!removeItem(player, Item(id, removeCount))) {
                                        return@queueScript stopExecuting(player)
                                    }

                                    toRemove -= removeCount
                                }
                            }

                            if (toRemove > 0) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, dart.dartId, realBatch)
                            rewardXP(player, Skills.FLETCHING, dart.xp * realBatch)

                            remaining -= realBatch

                            return@queueScript delayClock(player, Clocks.SKILLING, 1, true)
                        }
                    }

                    override fun getAll(index: Int): Int {
                        return getMaxAmount() / 10
                    }
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles attaching feathers to bolts to create bolts.
         */

        onUseWith(IntType.ITEM, FletchingDefinition.FEATHER_IDS, *FletchingDefinition.UNF_BOLTS) { player, used, with ->

            if (!clockReady(player, Clocks.SKILLING)) {
                return@onUseWith true
            }

            val bolt =
                FletchingDefinition.Bolt.product[used.id]
                    ?: FletchingDefinition.Bolt.product[with.id]
                    ?: return@onUseWith true

            val featherId =
                if (used.id in FletchingDefinition.FEATHER_IDS) {
                    used.id
                } else {
                    with.id
                }

            val handler =
                object : SkillDialogueHandler(player, SkillDialogue.MAKE_SET_ONE_OPTION, Item(bolt.boltId)) {
                    fun getMaxAmount(): Int {
                        return min(
                            amountInInventory(player, bolt.unfinished),
                            amountInInventory(player, featherId)
                        )
                    }

                    override fun create(amount: Int, index: Int) {
                        if (amount <= 0) return
                        var remaining = amount * 10
                        queueScript(player, 0, QueueStrength.WEAK) {

                            if (remaining <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (bolt.unfinished == Items.BROAD_BOLTS_UNF_13279 && !getInstance(player).flags.isBroadsUnlocked()) {
                                sendDialogue(player, "You need to unlock the ability to create broad bolts.")
                                return@queueScript stopExecuting(player)
                            }

                            if (getStatLevel(player, Skills.FLETCHING) < bolt.level) {
                                sendDialogue(player, "You need a Fletching level of ${bolt.level} or above to do that.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, bolt.unfinished, 1)) {
                                sendMessage(player, "You have run out of bolts.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!inInventory(player, featherId, 1)) {
                                sendMessage(player, "You have run out of feathers.")
                                return@queueScript stopExecuting(player)
                            }

                            if (!hasSpaceFor(player, Item(bolt.boltId))) {
                                sendDialogue(player, "You do not have enough inventory space.")
                                return@queueScript stopExecuting(player)
                            }

                            val baseAmount =
                                amountInInventory(player, bolt.unfinished)

                            val featherAmount =
                                amountInInventory(player, featherId)

                            val batchAmount =
                                min(10, min(baseAmount, featherAmount))

                            val realBatch =
                                min(batchAmount, remaining)

                            if (realBatch <= 0) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!removeItem(player, Item(bolt.unfinished, realBatch))) {
                                return@queueScript stopExecuting(player)
                            }

                            if (!removeItem(player, Item(featherId, realBatch))) {
                                return@queueScript stopExecuting(player)
                            }

                            addItem(player, bolt.boltId, realBatch)
                            rewardXP(player, Skills.FLETCHING, bolt.xp * realBatch)

                            sendMessage(
                                player,
                                if (realBatch == 1) {
                                    "You attach the tip to the bolt."
                                } else {
                                    "You fletch $realBatch bolts."
                                }
                            )

                            remaining -= realBatch

                            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
                        }
                    }

                    override fun getAll(index: Int): Int {
                        return getMaxAmount() / 10
                    }
                }

            val maxAmount = handler.getAll(0)

            if (maxAmount < 1) {
                handler.create(1, 0)
            } else {
                handler.open()
            }

            return@onUseWith true
        }
    }
}
