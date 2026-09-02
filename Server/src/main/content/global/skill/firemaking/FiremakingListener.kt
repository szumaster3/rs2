package content.global.skill.firemaking

import content.global.skill.firemaking.GnomishFirelighter
import content.global.skill.firemaking.LogItem
import content.data.skill.SkillingTool
import content.region.kandarin.baxtorian.BarbarianTraining
import core.api.*
import core.game.event.LitFireEvent
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.GroundItem
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.world.GameWorld
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import kotlin.math.ceil

class FiremakingListener : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles lighting logs using the light option.
         */

        on(LOG_IDS, IntType.ITEM, "light") { player, node ->
            val log = LogItem.forId(node.id) ?: return@on false

            if (!inInventory(player, Items.TINDERBOX_590, 1)) {
                sendMessage(player, "You do not have the required items to light this.")
                return@on true
            }

            startFiremaking(
                player = player,
                fire = log,
                node = node as Item,
                ground = null,
                type = FiremakingType.STANDARD,
            )

            return@on true
        }

        /*
         * Handles lighting ground logs using the light option.
         */

        on(LOG_IDS, IntType.GROUND_ITEM, "light") { player, node ->
            val groundItem = node as? GroundItem ?: return@on false
            val log = LogItem.forId(groundItem.id) ?: return@on false

            if (!inInventory(player, Items.TINDERBOX_590, 1)) {
                sendMessage(player, "You do not have the required items to light this.")
                return@on true
            }

            startFiremaking(
                player = player,
                fire = log,
                node = Item(groundItem.id, 1),
                ground = groundItem,
                type = FiremakingType.STANDARD,
            )

            return@on true
        }

        /*
         * Handles lighting logs using a tinderbox.
         */

        onUseWith(IntType.ITEM, Items.TINDERBOX_590, *LOG_IDS) { player, _, with ->
            val log = LogItem.forId(with.id) ?: return@onUseWith true

            startFiremaking(
                player = player,
                fire = log,
                node = with.asItem(),
                ground = null,
                type = FiremakingType.STANDARD,
            )

            return@onUseWith true
        }

        /*
         * Handles lighting ground logs using a tinderbox.
         */

        onUseWith(IntType.GROUND_ITEM, Items.TINDERBOX_590, *LOG_IDS) { player, _, with ->
            val groundItem = with as? GroundItem ?: return@onUseWith true
            val log = LogItem.forId(groundItem.id) ?: return@onUseWith true

            startFiremaking(
                player = player,
                fire = log,
                node = Item(groundItem.id, 1),
                ground = groundItem,
                type = FiremakingType.STANDARD,
            )

            return@onUseWith true
        }

        /*
         * Handles combining logs with gnomish firelighters.
         */

        onUseWith(
            IntType.ITEM,
            Items.LOGS_1511,
            Items.RED_FIRELIGHTER_7329,
            Items.GREEN_FIRELIGHTER_7330,
            Items.BLUE_FIRELIGHTER_7331,
            Items.PURPLE_FIRELIGHTER_10326,
            Items.WHITE_FIRELIGHTER_10327,
        ) { player, used, with ->
            val firelighter = GnomishFirelighter.forProduct(with.id)
                ?: return@onUseWith false

            if (with.id == firelighter.product || used.id == firelighter.base) {
                sendMessage(player, "You can't do that.")
                return@onUseWith true
            }

            if (!removeItem(player, Item(with.id, 1))) {
                sendMessage(
                    player,
                    "You don't have the required items in your inventory.",
                )
                return@onUseWith true
            }

            addItem(player, firelighter.product, 1)

            val chemicalName = getItemName(firelighter.base)
                .replaceFirst("firelighter", "chemicals")
                .lowercase()

            sendMessage(player, "You coat the log with the $chemicalName.")
            return@onUseWith true
        }

        /*
         * Handles barbarian firemaking.
         */

        onUseWith(IntType.ITEM, BARB_TOOLS, *LOG_IDS) { player, used, with ->
            checkRequirements(used.asItem())?.let {
                sendDialogue(player, it)
                return@onUseWith false
            }

            val log = LogItem.forId(with.id) ?: return@onUseWith false

            startFiremaking(
                player = player,
                fire = log,
                node = with.asItem(),
                ground = null,
                type = FiremakingType.BARBARIAN,
            )

            return@onUseWith true
        }
    }

    /**
     * Checks whether a barbarian firemaking tool can be used to light a log.
     *
     * @param item the firemaking tool being used.
     * @return an error message if the tool cannot be used.
     */
    private fun checkRequirements(item: Item): String? =
        when {
            item.id == Items.DARK_BOW_11235 || item.id == Items.DARK_BOW_13405 ->
                "The innate darkness of the bow sucks all the heat from your firemaking attempt. You realise that this type of bow is useless for firelighting."

            item.name.contains("CRYSTAL BOW", true) ||
                    item.name.contains("CRYSTAL SHIELD", true) ->
                "The bow resists all attempts to light the fire. It seems that the sentient tools of the elves don't approve of you burning down forests."

            item.id == Items.COMP_OGRE_BOW_4827 ||
                    item.id == Items.OGRE_BOW_2883 ->
                "This bow is vast, clumsy and most of a tree. You realise that this type of bow is useless for firelighting."

            else -> null
        }

    companion object {

        val BARB_TOOLS = intArrayOf(
            Items.OGRE_BOW_2883,
            Items.COMP_OGRE_BOW_4827,
            Items.TRAINING_BOW_9705,
            Items.LONGBOW_839,
            Items.SHORTBOW_841,
            Items.OAK_SHORTBOW_843,
            Items.OAK_LONGBOW_845,
            Items.WILLOW_LONGBOW_847,
            Items.WILLOW_SHORTBOW_849,
            Items.MAPLE_LONGBOW_851,
            Items.MAPLE_SHORTBOW_853,
            Items.YEW_LONGBOW_855,
            Items.YEW_SHORTBOW_857,
            Items.MAGIC_LONGBOW_859,
            Items.MAGIC_SHORTBOW_861,
            Items.SEERCULL_6724,
            Items.DARK_BOW_11235,
            Items.DARK_BOW_13405,
        )

        val LOG_IDS = intArrayOf(
            Items.LOGS_1511,
            Items.OAK_LOGS_1521,
            Items.WILLOW_LOGS_1519,
            Items.MAPLE_LOGS_1517,
            Items.YEW_LOGS_1515,
            Items.MAGIC_LOGS_1513,
            Items.ACHEY_TREE_LOGS_2862,
            Items.PYRE_LOGS_3438,
            Items.OAK_PYRE_LOGS_3440,
            Items.WILLOW_PYRE_LOGS_3442,
            Items.MAPLE_PYRE_LOGS_3444,
            Items.YEW_PYRE_LOGS_3446,
            Items.MAGIC_PYRE_LOGS_3448,
            Items.TEAK_PYRE_LOGS_6211,
            Items.MAHOGANY_PYRE_LOG_6213,
            Items.MAHOGANY_LOGS_6332,
            Items.TEAK_LOGS_6333,
            Items.RED_LOGS_7404,
            Items.GREEN_LOGS_7405,
            Items.BLUE_LOGS_7406,
            Items.PURPLE_LOGS_10329,
            Items.WHITE_LOGS_10328,
            Items.SCRAPEY_TREE_LOGS_8934,
            Items.DREAM_LOG_9067,
            Items.ARCTIC_PYRE_LOGS_10808,
            Items.ARCTIC_PINE_LOGS_10810,
            Items.SPLIT_LOG_10812,
            Items.WINDSWEPT_LOGS_11035,
            Items.EUCALYPTUS_LOGS_12581,
            Items.EUCALYPTUS_PYRE_LOGS_12583,
            Items.JOGRE_BONES_3125,
        )

        /**
         * Starts the fm process for a log or an existing ground item.
         *
         * @param player the player.
         * @param fire the log type being burned.
         * @param node the inventory item representing the log.
         * @param ground the existing ground item to light, or null when lighting a log from the inventory.
         * @param type the fm method used to light the fire.
         */
        fun startFiremaking(
            player: Player,
            fire: LogItem,
            node: Item,
            ground: GroundItem?,
            type: FiremakingType,
        ) {
            val quickLight = getLastFire(player) >= GameWorld.ticks
            var logRemoved = ground != null

            val groundItem = ground
                ?: GroundItem(Item(node.id, 1), player.location, player)

            val animation = if (type == FiremakingType.BARBARIAN) {
                SkillingTool.getFiremakingTool(player)?.let { Animation(it.animation) }
            } else {
                Animation(Animations.HUMAN_LIGHT_FIRE_WITH_TINDERBOX_733)
            }

            val graphics = if (type == FiremakingType.BARBARIAN) {
                Graphics(shared.consts.Graphics.BARBARIAN_FIREMAKING_1169)
            } else {
                null
            }

            var ticks = 0

            queueScript(
                player,
                0,
                QueueStrength.WEAK,
                script = firemaking@{
                    if (!checkRequirements(player, fire, type)) {
                        return@firemaking true
                    }

                    if (!finishedMoving(player)) {
                        sendMessage(player, "You can't do this right now.")
                        return@firemaking true
                    }

                    if (!logRemoved) {
                        if (!inInventory(player, node.id, 1)) {
                            return@firemaking true
                        }

                        logRemoved = true

                        replaceSlot(
                            player,
                            node.slot,
                            Item(node.id, node.amount - 1),
                            node,
                            Container.INVENTORY,
                        )

                        GroundItemManager.create(groundItem)
                    }

                    if (quickLight) {
                        finishFire(
                            player,
                            fire,
                            node,
                            groundItem,
                            type,
                        )
                        return@firemaking true
                    }

                    if (ticks == 0) {
                        sendMessage(
                            player,
                            "You attempt to light the ${
                                if (node.id == Items.JOGRE_BONES_3125) "bones" else "logs"
                            }..",
                        )

                        playAnimation(player, animation, graphics)
                    }

                    ticks++

                    if (ticks % 3 != 0) {
                        delayScript(player, 1)
                        return@firemaking false
                    }

                    if (ticks % 12 == 0) {
                        playAnimation(player, animation, graphics)
                    }

                    if (!rollSuccess(player, fire, type)) {
                        delayScript(player, 1)
                        return@firemaking false
                    }

                    finishFire(
                        player,
                        fire,
                        node,
                        groundItem,
                        type,
                    )

                    true
                },
            )
        }

        private fun playAnimation(
            player: Player,
            animation: Animation?,
            graphics: Graphics?,
        ) {
            animation?.let(player::animate)
            graphics?.let(player::graphics)
        }

        private fun checkRequirements(
            player: Player,
            fire: LogItem,
            mode: FiremakingType,
        ): Boolean {
            if (mode == FiremakingType.BARBARIAN &&
                getAttribute(player, BarbarianTraining.FM_START, false)
            ) {
                sendDialogue(
                    player,
                    "You must begin the relevant section of Otto Godblessed's barbarian training.",
                )
                return false
            }

            if (RegionManager.getObject(player.location) != null ||
                inZone(player, "bank")
            ) {
                sendMessage(player, "You can't light a fire here.")
                return false
            }

            if (mode == FiremakingType.STANDARD &&
                !inInventory(player, Items.TINDERBOX_590, 1)
            ) {
                sendMessage(
                    player,
                    "You do not have the required items to light this.",
                )
                return false
            }

            val requiredLevel =
                if (mode == FiremakingType.BARBARIAN) {
                    fire.barbarianLevel
                } else {
                    fire.defaultLevel
                }

            if (getStatLevel(player, Skills.FIREMAKING) < requiredLevel) {
                sendMessage(
                    player,
                    "You need a Firemaking level of $requiredLevel to light this log.",
                )
                return false
            }

            return true
        }

        private fun rollSuccess(
            player: Player,
            fire: LogItem,
            mode: FiremakingType,
        ): Boolean {
            val level = 1 + getStatLevel(player, Skills.FIREMAKING)

            val requiredLevel =
                if (mode == FiremakingType.BARBARIAN) {
                    fire.barbarianLevel
                } else {
                    fire.defaultLevel
                }

            val req = requiredLevel.toDouble()
            val successChance = ceil((level * 50 - req * 15) / req / 3 * 4)

            return successChance >= RandomFunction.random(99)
        }

        private fun finishFire(
            player: Player,
            fire: LogItem,
            node: Item,
            groundItem: GroundItem,
            mode: FiremakingType,
        ) {
            if (!groundItem.isActive) {
                return
            }

            val scenery = Scenery(fire.fireId, player.location)

            SceneryBuilder.add(scenery, fire.life) {
                GroundItemManager.create(
                    getAsh(player, fire, scenery),
                )
            }

            GroundItemManager.destroy(groundItem)

            player.moveStep()
            player.faceLocation(scenery.getFaceLocation(player.location))

            rewardXP(player, Skills.FIREMAKING, fire.xp)

            setLastFire(player)
            player.dispatch(LitFireEvent(fire.logId))

            sendMessage(
                player,
                "The fire catches and the ${
                    if (node.id == Items.JOGRE_BONES_3125) "bones" else "logs"
                } begin to burn.",
            )

            if (mode == FiremakingType.BARBARIAN &&
                getAttribute(player, BarbarianTraining.FM_BASE, false) &&
                !player.savedData.activityData.barbarianFiremaking
            ) {
                removeAttribute(player, BarbarianTraining.FM_BASE)
                player.savedData.activityData.barbarianFiremaking = true

                sendDialogueLines(
                    player,
                    "You feel you have learned more of barbarian ways.",
                    "Otto might wish to talk to you more.",
                )
            }
        }

        private fun getLastFire(player: Player): Int =
            player.getAttribute("last-firemake", 0)

        private fun setLastFire(player: Player) {
            player.setAttribute(
                "last-firemake",
                GameWorld.ticks + 2,
            )
        }

        fun getAsh(
            player: Player,
            fire: LogItem,
            scenery: Scenery,
        ): GroundItem {
            val ash = GroundItem(
                Item(Items.ASHES_592),
                scenery.location,
                player,
            )

            ash.decayTime = fire.life + 200
            return ash
        }
    }

    enum class FiremakingType {
        STANDARD,
        BARBARIAN,
    }
}