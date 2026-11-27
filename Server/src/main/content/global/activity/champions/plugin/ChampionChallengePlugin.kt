package content.global.activity.champions.plugin

import content.data.GameAttributes
import content.global.activity.champions.dialogue.LarxusDialogueFile
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.item.Item
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import shared.consts.*

/**
 * Champion's Challenge plugin
 * Based on 10.03.2009 source.
 * @author szu
 */
class ChampionChallengePlugin : InteractionListener, MapArea {

    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(getRegionBorders(CHAMPION_CHALLENGE_REGION))

    override fun getRestrictions(): Array<ZoneRestriction> =
        arrayOf(ZoneRestriction.CANNON, ZoneRestriction.FIRES, ZoneRestriction.RANDOM_EVENTS)

    override fun defineListeners() {

        /*
         * Handles climbing down ladder inside the Champion's Guild.
         */

        on(Scenery.CHAMPION_STATUE_10557, IntType.SCENERY, "climb-down") { player, _ ->
            val activeScroll = getActiveChampionScroll(player)

            if (activeScroll == null) {
                sendNPCDialogue(player, NPCs.LARXUS_3050, "You need to arrange a challenge with me before you enter the arena.", FaceAnim.NEUTRAL)
                return@on true
            }

            if (activeScroll == Items.CHAMPION_SCROLL_6805 && player.equipment.toArray().any { it != null }) {
                sendNPCDialogue(player, NPCs.LARXUS_3050, "For this fight you're not allowed to wear armour or weapons; only inventory items allowed.", FaceAnim.NEUTRAL)
                return@on true
            }

            teleport(player, Location.create(3182, 9758, 0), TeleportManager.TeleportType.INSTANT)
            return@on true
        }

        /*
         * Handles climbing up ladder inside the Champion's Guild.
         */

        on(Scenery.LADDER_10554, IntType.SCENERY, "climb-up") { player, _ ->
            teleport(player, Location.create(3185, 9758, 0), TeleportManager.TeleportType.INSTANT)
            return@on true
        }

        /*
         * Handles read the champion scroll.
         */

        on(ChampionScrollsDropHandler.SCROLLS, IntType.ITEM, "read") { player, node ->
            displayScroll(player, node.asItem())
            return@on true
        }

        /*
         * Handles trapdoor to champions guild basement.
         */

        on(Scenery.TRAPDOOR_10558, IntType.SCENERY, "open") { player, node ->
            playAudio(player, Sounds.TRAP_DOOR_OPEN_91)
            replaceScenery(node.asScenery(), Scenery.TRAPDOOR_10559, 100, node.location)
            return@on true
        }

        /*
         * Handles closing the champions guild trapdoor.
         */

        on(Scenery.TRAPDOOR_10559, IntType.SCENERY, "close") { player, node ->
            playAudio(player, Sounds.TRAP_DOOR_CLOSE_90)
            replaceScenery(node.asScenery(), Scenery.TRAPDOOR_10558, -1, node.location)
            return@on true
        }

        /*
         * Handles using champion scroll on Larxus NPC to start dialogue.
         */

        onUseWith(IntType.NPC, ChampionScrollsDropHandler.SCROLLS, NPCs.LARXUS_3050) { player, scroll, _ ->
            openDialogue(player, LarxusDialogueFile(true, scroll.asItem()))
            return@onUseWith true
        }

        /*
         * Handles opening the champion statue.
         */

        on(Scenery.CHAMPION_STATUE_10556, IntType.SCENERY, "open") { player, _ ->
            sendNPCDialogue(player, NPCs.LARXUS_3050, "You need to arrange a challenge with me before you enter the arena.", FaceAnim.NEUTRAL)
            return@on true
        }

        /*
         * Handles opening the portcullis gate to start the challenge.
         */

        on(Scenery.PORTCULLIS_10553, IntType.SCENERY, "open") { player, node ->
            handlePortcullisInteraction(player, node)
            return@on true
        }
    }

    /**
     * Handles passing through the gate and initiating the challenge.
     */
    private fun handlePortcullisInteraction(player: Player, node: Node) {
        if (player.location.x == 3181 && player.location.y == 9758) {
            player.impactHandler.disabledTicks = 3
            animateScenery(node.asScenery(), 2976)
            playAudio(player, Sounds.PORTCULLIS_OPEN_83)
            forceMove(player, player.location, Location(3182, 9758, 0), 0, 30, Direction.EAST, 819)
            return
        }

        if (!player.musicPlayer.hasUnlocked(Music.VICTORY_IS_MINE_528)) {
            player.musicPlayer.unlock(Music.VICTORY_IS_MINE_528, true)
        }

        player.lock(3)
        player.impactHandler.disabledTicks = 3
        animateScenery(node.asScenery(), 2976)
        playAudio(player, Sounds.PORTCULLIS_CLOSE_82, 6)
        forceMove(player, player.location, Location.create(3180, 9758, 0), 0, 60, Direction.WEST, 819)
        initChampionSpawn(player)
    }

    /**
     * Spawns a champion NPC based on scroll.
     * @see getActiveChampionScroll
     */
    private fun initChampionSpawn(player: Player) {
        val scrollId = getActiveChampionScroll(player)
        val defeatAll = getAttribute(player, GameAttributes.ACTIVITY_CHAMPIONS_CHALLENGE_DEFEAT_ALL, false)

        val entry = when {
            scrollId != null -> ChampionDefinition.fromScroll(scrollId)
            defeatAll -> ChampionDefinition.LEON
            else -> null
        } ?: return

        if ((entry == ChampionDefinition.GHOUL || entry == ChampionDefinition.LEON) && freeSlots(player) != 28) {
            sendNPCDialogue(player, NPCs.LARXUS_3050, "His special rule is that no items in inventory can be brought to arena, only equipped items are allowed.")
            return
        }

        ChampionChallengeNPC.spawnChampion(player, entry)
    }

    /**
     * Show text for each of the champion scroll.
     */
    private fun displayScroll(player: Player, item: Item) {
        val content = championScrollsContent[item.id] ?: return
        openInterface(player, Components.BLANK_SCROLL_222)
        sendString(player, content.joinToString("<br>"), Components.BLANK_SCROLL_222, 4)
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, intArrayOf(Scenery.TRAPDOOR_10559), "climb-down") { _, _ ->
            return@setDest Location(3191, 3355, 0)
        }
    }

    companion object {
        /**
         * The location of activity.
         */
        private const val CHAMPION_CHALLENGE_REGION = 12696

        /**
         * Displays the champion scroll content.
         */
        private val championScrollsContent = mapOf(
            Items.CHAMPION_SCROLL_6798 to arrayOf("I challenge you to a duel, come to the arena", "beneath the Champion's Guild and fight me if you", "dare.", "", "Champion of Earth Warriors"),
            Items.CHAMPION_SCROLL_6799 to arrayOf("Come and duel me at the Champions' Guild, I'll", "make sure nothing goes to waste.", "", "Champion of Ghouls"),
            Items.CHAMPION_SCROLL_6800 to arrayOf("Get yourself to the Champions' Guild, if you dare", "to face me puny human.", "", "Champion of Giants"),
            Items.CHAMPION_SCROLL_6801 to arrayOf("Fight me if you think you can human, I'll wait", "for you in the Champion's Guild.", "", "Champion of Goblins"),
            Items.CHAMPION_SCROLL_6802 to arrayOf("You won't defeat me, though you're welcome to", "try at the Champions' Guild.", "", "Champion of Hobgoblins"),
            Items.CHAMPION_SCROLL_6803 to arrayOf("How about picking on someone your own size? I'll", "see you at the Champion's Guild.", "", "Champion of Imps"),
            Items.CHAMPION_SCROLL_6804 to arrayOf("You think you can defeat me? Come to the", "Champion's Guild and prove it!", "", "Champion of Jogres"),
            Items.CHAMPION_SCROLL_6805 to arrayOf("Come to the Champion's Guild so I can banish", "you mortal!", "", "Champion of Lesser Demons"),
            Items.CHAMPION_SCROLL_6806 to arrayOf("I'll be waiting at the Champions' Guild to collect", "your bones.", "", "Champion of Skeletons"),
            Items.CHAMPION_SCROLL_6807 to arrayOf("You come to Champions' Guild, you fight me, I", "squish you, I get brains!", "", "Champion of Zombies"),
        )

        /**
         * Checking if a scroll has the same number of charges as its id.
         *
         * @param player the player.
         * @return the scroll id.
         */
        @JvmStatic
        fun getActiveChampionScroll(player: Player): Int? {
            for (id in ChampionScrollsDropHandler.SCROLLS) {
                val item = player.inventory.getItem(id.asItem())
                if (item != null && item.charge == id) {
                    return id
                }
            }
            return null
        }
    }
}
