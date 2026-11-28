package content.global.random.event.prison

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.zone.ZoneRestriction
import shared.consts.*

/**
 * Handles the interactions for Prison pete random event.
 * https://www.youtube.com/watch?v=xJA2mNsEYeg
 * @author szu (January 3, 2024)
 */
class PrisonPetePlugin : InteractionListener, MapArea {

    /**
     * Gets current event progress (keys found).
     */
    private fun getScore(player: Player): Int =
        getVarbit(player, 1547)

    /**
     * Updates event progress (keys found).
     */
    private fun setScore(player: Player, value: Int) {
        setVarbit(player, 1547, value)
    }

    override fun defineListeners() {

        /*
         * Handles basic dialogues.
         */

        on(NPCs.PRISONPETE_BALLOON_L_3118, IntType.NPC, "talk-to") { player, _ ->
            findNPC(NPCs.PRISONPETE_BALLOON_L_3118)?.location?.let { face(player, it) }
            openDialogue(player, PrisonPeteDialogue(0))
            return@on true
        }

        /*
         * Handles key return dialogue.
         */

        on(Items.PRISON_KEY_6966, IntType.ITEM, "return") { player, _ ->
            openDialogue(player, PrisonPeteDialogue(1))
            return@on true
        }

        /*
         * Handles lock the south doors.
         */

        on(Scenery.LOCKED_DOOR_26188, IntType.SCENERY, "open") { player, _ ->
            sendMessage(player, "The doors won't open.")
            return@on true
        }

        /*
         * Opens balloon interface.
         */

        on(Scenery.CAVE_GOBLIN_26191, IntType.SCENERY, "pull") { player, _ ->
            animate(player, Animations.HUMAN_BOXLEVER_798)
            openInterface(player, Components.MACRO_PRISON_PETE_273)
            return@on true
        }

        /*
         * Handles interaction with balloons npc.
         */

        on(PrisonPeteUtils.ANIMAL_ID, IntType.NPC, "pop") { player, node ->
            val validTarget = getAttribute(player, PrisonPeteUtils.EXPECTED_NPC, -1)

            if (validTarget == -1) {
                sendMessage(player, "Pull the lever to see which balloon to pop.")
                return@on true
            }

            val currentScore = getScore(player)

            if (currentScore >= 3) {
                sendMessage(player, "You've found all the keys.")
                return@on true
            }

            removeAttribute(player, PrisonPeteUtils.EXPECTED_NPC)

            if (player.location.getDistance(node.location) > 1) {
                forceWalk(player, node.centerLocation, "smart")
                return@on true
            }

            val correct = node.id == validTarget

            if (correct) {
                setScore(player, currentScore + 1)
                removeAttribute(player, PrisonPeteUtils.POP_KEY_FALSE)
            } else {
                setAttribute(player, PrisonPeteUtils.POP_KEY_FALSE, true)
            }

            queueScript(player, 3, QueueStrength.SOFT) { tick ->
                when (tick) {
                    0 -> {
                        playAudio(player, Sounds.POP3_3252)
                        visualize(player, Animations.HUMAN_STAMP_794, Graphics.BALLOON_POP_524)
                        return@queueScript delayScript(player, 1)
                    }
                    1 -> {
                        animate(player, Animations.HUMAN_PICKUPFLOOR_827)
                        if (freeSlots(player) >= 1) {
                            node.asNpc().clear()
                            addItem(player, Items.PRISON_KEY_6966)
                            openDialogue(player, PrisonPeteDialogue(1))
                        } else {
                            sendMessage(player, "You don't have enough inventory space.")
                        }
                        return@queueScript stopExecuting(player)
                    }
                    else -> stopExecuting(player)
                }
            }
            return@on true
        }
    }

    override fun defineAreaBorders() = arrayOf(PrisonPeteUtils.PRISON_ZONE)

    override fun getRestrictions() = arrayOf(ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.CANNON, ZoneRestriction.FOLLOWERS)

    override fun areaLeave(entity: Entity, logout: Boolean) {
        if (entity is Player) {
            if (!logout) {
                PrisonTimer.stop(entity)
            }

            PrisonPeteUtils.cleanup(entity)
        }
    }
}

//    data class ScenerySpawn(val id: Int, val location: Location, val rotation: Int = 0, val height: Int = 1, val replaceWith: Int? = null, val replaceHeight: Int = -1)
//
//    private fun isNewer() = ServerConstants.PRISON_PETE_RANDOM_EVENT_REVISION == 592
//
//    private fun getScore(player: Player): Int =
//        if (isNewer()) getAttribute(player, PrisonPeteUtils.POP_KEY, 0)
//        else getVarbit(player, 1547)
//
//    private fun setScore(player: Player, value: Int) {
//        if (isNewer()) {
//            setAttribute(player, PrisonPeteUtils.POP_KEY, value)
//        } else {
//            setVarbit(player, 1547, value)
//        }
//    }
//
//    init {
//        if (isNewer()) {
//            init2010()
//        } else {
//            init2009()
//        }
//    }
//
//    private fun init2010() {
//        core.game.node.entity.npc.NPC.create(
//            NPCs.PRISONPETE_BALLOON_L_3118,
//            Location.create(2084, 4460)
//        )
//
//        val sceneryData = listOf(
//            ScenerySpawn(Scenery.GUTHIX_PORTAL_4408, Location(2085, 4457, 0), rotation = 10),
//            ScenerySpawn(Scenery.CAVE_GOBLIN_26191, Location(2083, 4460, 0), rotation = 10),
//
//            ScenerySpawn(26184, Location(2084, 4459, 0)),
//            ScenerySpawn(26186, Location(2085, 4459, 0)),
//            ScenerySpawn(26188, Location(2086, 4459, 0)),
//            ScenerySpawn(26184, Location(2087, 4459, 0)),
//
//            ScenerySpawn(
//                id = Scenery.FIREPLACE_10824,
//                location = Location.create(2077, 4466),
//                rotation = 10,
//                replaceWith = 26192
//            )
//        )
//
//        sceneryData.forEach { s ->
//            val base = core.game.node.scenery.Scenery(s.id, s.location, s.height, s.rotation)
//
//            if (s.replaceWith != null) {
//                replaceScenery(base, s.replaceWith, s.replaceHeight)
//            }
//
//            addScenery(s.id, s.location, s.height, s.rotation)
//        }
//
//        listOf(
//            12248 to Location(2094, 4472, 0),
//            12247 to Location(2094, 4473, 0),
//            12246 to Location(2092, 4473, 0),
//            12245 to Location(2079, 4472, 0),
//            12244 to Location(2077, 4473, 0),
//            12243 to Location(2076, 4471, 0)
//        ).forEach { (id, loc) ->
//            removeScenery(core.game.node.scenery.Scenery(id, loc))
//        }
//    }
//    on(Scenery.LOCKED_DOOR_26188, IntType.SCENERY, "open") { player, _ ->
//
//        val ttlScore = getScore(player)
//
//        WarningManager.trigger(player, WarningType.DROPPED_ITEMS_IN_RANDOM_EVENTS) {
//            if (ttlScore >= 3) {
//                PrisonPeteUtils.cleanup(player)
//                queueScript(player, 2, QueueStrength.SOFT) {
//                    PrisonPeteUtils.reward(player)
//                    unlock(player)
//                    stopExecuting(player)
//                }
//            }
//        }
//
//        return@on true
//    }

// https://imgur.com/tYdajWW