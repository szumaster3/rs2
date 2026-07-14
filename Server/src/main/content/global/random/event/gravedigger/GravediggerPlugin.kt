package content.global.random.event.gravedigger

import content.data.GameAttributes
import content.data.RandomEvent
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.build.DynamicRegion
import core.game.world.map.zone.ZoneBorders
import core.game.world.update.flag.context.Animation
import shared.consts.*

/**
 * Handles interactions for Gravedigger random event.
 */
class GravediggerPlugin : InteractionListener, MapArea {

    override fun defineAreaBorders(): Array<ZoneBorders> {
        return arrayOf(ZoneBorders.forRegion(7758))
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        super.areaLeave(entity, logout)

        if (entity is Player && !entity.isArtificial) {
            val player = entity.asPlayer()

            removeCoffins(player)
            playerGraveMappings.remove(player)
            removeRegion(player)
        }
    }

    /**
     * Represents each coffin content for random event.
     */
    enum class CoffinSet(val coffinId: Int, val gravestoneId: Int, val graveId: Int, val emptyGraveId: Int, val item: Int, val content: List<Int>) {
        LUMBERJACK(
            Items.COFFIN_7587, Scenery.GRAVESTONE_12716,
            Scenery.GRAVE_12721, Scenery.GRAVE_12726, Items.ITEM_7614,
            listOf(Items.ITEM_7611, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7603, Items.ITEM_7598, Items.ITEM_7605, Items.ITEM_7612)
        ),
        COOKS(
            Items.COFFIN_7588, Scenery.GRAVESTONE_12717,
            Scenery.GRAVE_12722, Scenery.GRAVE_12727, Items.ITEM_7615,
            listOf(Items.ITEM_7604, Items.ITEM_7601, Items.ITEM_7598, Items.ITEM_7600, Items.ITEM_7598, Items.ITEM_7611, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598)
        ),
        MINER(
            Items.COFFIN_7589, Scenery.GRAVESTONE_12718,
            Scenery.GRAVE_12723, Scenery.GRAVE_12728, Items.ITEM_7616,
            listOf(Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7606, Items.ITEM_7598, Items.ITEM_7597, Items.ITEM_7598, Items.ITEM_7607, Items.ITEM_7598, Items.ITEM_7611)
        ),
        FARMER(
            Items.COFFIN_7590, Scenery.GRAVESTONE_12719,
            Scenery.GRAVE_12724, Scenery.GRAVE_12729, Items.ITEM_7617,
            listOf(Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7610, Items.ITEM_7611, Items.ITEM_7609, Items.ITEM_7598, Items.ITEM_7602, Items.ITEM_7598, Items.ITEM_7598)
        ),
        POTTER(
            Items.COFFIN_7591, Scenery.GRAVESTONE_12720,
            Scenery.GRAVE_12725, Scenery.GRAVE_12730, Items.ITEM_7618,
            listOf(Items.ITEM_7598, Items.ITEM_7599, Items.ITEM_7608, Items.ITEM_7613, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7611, Items.ITEM_7598)
        )
    }

    companion object {
        data class PlayerGraveMapping(
            val grave: CoffinSet,
            val coffin: CoffinSet
        )

        private val playerGraveMappings = mutableMapOf<Player, List<PlayerGraveMapping>>()
        private const val COFFIN_INTERFACE = Components.GRAVEDIGGER_COFFIN_141
        private const val GRAVESTONE_INTERFACE = Components.GRAVEDIGGER_GRAVE_143
        private const val MAUSOLEUM = Scenery.MAUSOLEUM_12731
        private val ANIMATION = Animation(Animations.HUMAN_BURYING_BONES_827)
        private val COFFIN_SETS = CoffinSet.values().toList()
        val COFFIN_IDS = COFFIN_SETS.map(CoffinSet::coffinId).toIntArray()

        private val GRAVESTONE_IDS = COFFIN_SETS.map(CoffinSet::gravestoneId).toIntArray()
        private val GRAVE_IDS = COFFIN_SETS.map(CoffinSet::graveId).toIntArray()
        private val EMPTY_GRAVE_IDS = COFFIN_SETS.map(CoffinSet::emptyGraveId).toIntArray()

        private val WOODCUTTING_TOOLS = intArrayOf(Items.INFERNO_ADZE_13661, Items.DRAGON_AXE_6739, Items.RUNE_AXE_1359, Items.ADAMANT_AXE_1357, Items.MITHRIL_AXE_1355, Items.BLACK_AXE_1361, Items.STEEL_AXE_1353, Items.IRON_AXE_1349, Items.BRONZE_AXE_1351)
        private val regions = mutableMapOf<Player, DynamicRegion>()
        private fun generateCoffinShuffle(player: Player) {
            val shuffledCoffins = COFFIN_SETS.shuffled()
            playerGraveMappings[player] = COFFIN_SETS.mapIndexed { index, grave ->
                PlayerGraveMapping(
                    grave = grave,
                    coffin = shuffledCoffins[index]
                )
            }
        }

        private fun getPlayerGraveMapping(player: Player, graveId: Int): PlayerGraveMapping? {
            return playerGraveMappings[player]
                ?.firstOrNull { it.grave.graveId == graveId }
        }

        fun cleanup(player: Player) {
            setMinimapState(player, 0)
            playerGraveMappings.remove(player)
            removeRegion(player)
        }

        fun createRegion(player: Player): DynamicRegion
        {
            generateCoffinShuffle(player)
            return regions.getOrPut(player){
                DynamicRegion.create(7758)
            }
        }

        fun getRegion(player: Player): DynamicRegion?
        {
            return regions[player]
        }

        private fun removeRegion(player: Player)
        {
            regions.remove(player)?.clear()
        }

        fun removeCoffins(player: Player)
        {
            COFFIN_IDS.forEach{
                removeAll(player, it)
            }
        }
    }

    override fun defineListeners() {

        /*
         * Handles inspect coffin.
         */

        on(COFFIN_IDS, IntType.ITEM, "check") { player, item ->
            COFFIN_SETS.firstOrNull { it.coffinId == item.id }?.let { set ->
                openInterface(player, COFFIN_INTERFACE)
                set.content.forEachIndexed { index, itemId ->
                    sendItemZoomOnInterface(player, COFFIN_INTERFACE, index + 3, itemId)
                }
            }
            return@on true
        }

        /*
         * Handles read the gravestone.
         */

        on(GRAVESTONE_IDS, IntType.SCENERY, "read") { player, node ->
            sendMessage(player, "You examine the gravestone. It shows...")
            queueScript(player, 1, QueueStrength.SOFT) {
                COFFIN_SETS.firstOrNull { it.gravestoneId == node.id }?.let { set ->
                    openInterface(player, GRAVESTONE_INTERFACE)
                    sendItemZoomOnInterface(player, GRAVESTONE_INTERFACE, 2, set.item)
                }
                return@queueScript stopExecuting(player)
            }
            return@on true
        }

        /*
         * Handles taking the coffin from grave.
         */

        on(GRAVE_IDS, IntType.SCENERY, "take-coffin") { player, node ->
            getPlayerGraveMapping(player, node.id)?.let { mapping ->
                val coffin = Item(mapping.coffin.coffinId)
                if (!hasSpaceFor(player, coffin)) {
                    sendMessage(player, "You need space in your inventory to take the coffin.")
                } else {
                    lock(player, 3)
                    player.animate(ANIMATION)
                    queueScript(player, 1, QueueStrength.SOFT) {
                        addItem(player, coffin.id, 1)
                        replaceScenery(node.asScenery(), mapping.grave.emptyGraveId, -1)
                        return@queueScript stopExecuting(player)
                    }
                }
            }
            return@on true
        }

        /*
         * Handles placing the coffin into correct empty grave.
         */

        onUseWith(IntType.SCENERY, COFFIN_IDS, *EMPTY_GRAVE_IDS) { player, used, target ->
            val mapping = playerGraveMappings[player]
                ?.firstOrNull { it.coffin.coffinId == used.id }
            mapping?.let {
                if (target.id == mapping.grave.emptyGraveId){
                    lock(player, 3)
                    player.animate(ANIMATION)
                    queueScript(player, 1, QueueStrength.SOFT) {
                        if (!removeItem(player, used.asItem())) return@queueScript stopExecuting(player)
                        player.incrementAttribute(GameAttributes.GRAVEDIGGER_SCORE, 1)
                        replaceScenery(target.asScenery(), mapping.grave.graveId, -1)
                        sendMessage(player, "You put the coffin into the grave.")
                        return@queueScript stopExecuting(player)
                    }
                }
            }
            return@onUseWith true
        }

        /*
         * Handles use woodcutting tools on dead tree.
         */

        onUseWith(IntType.SCENERY, WOODCUTTING_TOOLS, Scenery.DEAD_TREE_12732) { player, _, _ ->
            if (inBorders(player, ZoneBorders.forRegion(7758))) {
                sendMessages(player, "You don't need any wood.", "What are you planning on doing, making them a fresh coffin?")
            }
            return@onUseWith true
        }

        /*
         * Handles deposit at mausoleum.
         */

        on(MAUSOLEUM, IntType.SCENERY, "deposit") { player, _ ->
            player.bank.openDepositBox()
            player.bank.refreshDepositBoxInterface()
            return@on true
        }

        /*
         * Handles talk to Leo NPC.
         */

        on(NPCs.LEO_3508, IntType.NPC, "talk-to") { player, npc ->
            openDialogue(player, LeoDialogue(), npc)
            return@on true
        }
    }
}
