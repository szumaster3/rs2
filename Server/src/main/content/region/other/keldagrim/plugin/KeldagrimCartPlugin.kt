package content.region.other.keldagrim.plugin

import content.data.Quests

import content.data.GameAttributes
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.Components
import shared.consts.NPCs
import shared.consts.Scenery

class KeldagrimCartPlugin : InteractionListener {

    init {
        /*
         * Adds minecarts at the Keldagrim station.
         */
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2914, 10173, 0), 0, 10)
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2916, 10175, 0), 0, 10)
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2918, 10175, 0), 0, 10)
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2920, 10175, 0), 0, 10)
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2922, 10175, 0), 0, 10)
        addScenery(Scenery.TRAIN_CART_7028, Location.create(2924, 10175, 0), 0, 10)
    }

    companion object {
        private val CART_SCENERY = intArrayOf(
            Scenery.TRAIN_CART_7028,
            Scenery.TRAIN_CART_7029,
            Scenery.TRAIN_CART_7030
        )

        /**
         * Travel pulses.
         */
        private fun startTravelToKeldagrim(player: Player) {
            if (hasRequirement(player, Quests.THE_GIANT_DWARF.questName))
                submitWorldPulse(TravelToKeldagrimPulse(player))
        }

        private fun startTravelFromKeldagrim(player: Player, dest: Location) {
            if (hasRequirement(player, Quests.THE_GIANT_DWARF.questName))
                submitWorldPulse(TravelFromKeldagrimPulse(player, dest))
        }

        private class TravelFromKeldagrimPulse(val player: Player, val dest: Location) : Pulse() {
            private var counter = 0

            override fun pulse(): Boolean {
                when (counter++) {
                    0 -> start()
                    4 -> player.teleportWithCart(Location(2911, 10171, 0), true)
                    5 -> player.moveCartTo(2936, 10171)
                    6 -> fadeNormal()
                    14 -> fadeBlack()
                    21 -> player.teleportWithCart(dest, false)
                    23 -> fadeNormal()
                    25 -> return finish()
                }
                return false
            }

            override fun start() {
                lock(player, 25)
                openInterface(player, Components.FADE_TO_BLACK_120)
                setMinimapState(player, 2)
            }

            private fun fadeNormal() {
                closeInterface(player)
                openInterface(player, Components.FADE_FROM_BLACK_170)
            }

            private fun fadeBlack() {
                openInterface(player, Components.FADE_TO_BLACK_120)
            }

            private fun finish(): Boolean {
                unlock(player)
                setMinimapState(player, 0)
                closeInterface(player)
                return true
            }
        }

        private class TravelToKeldagrimPulse(val player: Player) : Pulse() {
            private var counter = 0
            private val cartNPC = NPC(NPCs.MINE_CART_1544)

            override fun pulse(): Boolean {
                when (counter++) {
                    0 -> start()
                    6 -> player.teleportWithCart(Location(2943, 10170, 0), true)
                    7 -> player.moveCartTo(2939, 10173)
                    8 -> player.moveCartTo(2914, 10173)
                    10 -> fadeNormal()
                    23 -> finish()
                    33 -> {
                        cartNPC.clear()
                        return true
                    }
                }
                return false
            }

            override fun start() {
                lock(player, 20)
                openInterface(player, Components.FADE_TO_BLACK_115)
                setMinimapState(player, 2)
            }

            private fun fadeNormal() {
                closeInterface(player)
                openInterface(player, Components.FADE_FROM_BLACK_170)
            }

            private fun finish() {
                closeInterface(player)
                setMinimapState(player, 0)
                unlock(player)
                player.appearance.rideCart(false)
                cartNPC.location = player.location
                cartNPC.direction = Direction.WEST
                cartNPC.init()
                player.properties.teleportLocation = player.location.transform(0, 1, 0)
            }
        }

        private fun Player.teleportWithCart(location: Location, ride: Boolean) {
            properties.teleportLocation = location
            appearance.rideCart(ride)
        }

        private fun Player.moveCartTo(x: Int, y: Int) {
            walkingQueue.reset()
            walkingQueue.addPath(x, y)
        }
    }

    override fun defineListeners() {
        on(CART_SCENERY, IntType.SCENERY, "ride") { player, node ->
            if (!getAttribute(player, GameAttributes.MINECART_TRAVEL_UNLOCK, false)) {
                sendMessage(player, "You must visit Keldagrim to use this shortcut.")
                return@on true
            }

            when (node.id) {
                Scenery.TRAIN_CART_7028 -> {
                    val questDone = isQuestComplete(player, Quests.FISHING_CONTEST.questName)

                    val options = if (questDone)
                        arrayOf("To the Grand Exchange", "To Ice Mountain", "To White Wolf Mountain", "Stay here.")
                     else
                        arrayOf("To the Grand Exchange", "To Ice Mountain", "Stay here.")

                    sendOptions(player, "Select an option", *options)
                    addDialogueAction(player) { p, choice ->
                        when (choice) {
                            2 -> startTravelFromKeldagrim(p, Location.create(3140, 3507, 0))
                            3 -> startTravelFromKeldagrim(p, Location.create(2997, 9837, 0))
                            4 -> if (questDone) startTravelFromKeldagrim(p, Location.create(2875, 9871, 0)) else closeDialogue(p)
                            else -> closeDialogue(p)
                        }
                    }
                }

                else -> {
                    sendOptions(player, "Select an option", "Travel to Keldagrim", "Stay here.")
                    addDialogueAction(player) { p, choice ->
                        when (choice) {
                            2 -> startTravelToKeldagrim(p)
                            3 -> closeDialogue(p)
                        }
                    }
                }
            }
            return@on true
        }

        on(Scenery.HIDDEN_TRAPDOOR_28094, IntType.SCENERY, "open") { player, _ ->
            if (!getAttribute(player, GameAttributes.MINECART_TRAVEL_UNLOCK, false)) {
                sendMessage(player, "You must visit Keldagrim to use this shortcut.")
                return@on true
            }
            sendDialogueLines(player, "This trapdoor leads to a small dwarven mine cart station.", "The mine cart will take you to Keldagrim.")
            addDialogueAction(player) { _, _ ->
                closeDialogue(player)
                sendOptions(player, "Select an option", "Travel to Keldagrim", "Stay here.")
                addDialogueAction(player) { p, choice ->
                    when (choice) {
                        2 -> startTravelToKeldagrim(p)
                        3 -> closeDialogue(p)
                    }
                }
            }

            return@on true
        }
    }

}