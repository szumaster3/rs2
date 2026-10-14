package content.region.morytania.port_phasmatys.quest.ahoy.plugin

import core.api.*
import core.game.component.Component
import core.game.interaction.QueueStrength
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.*

object GhostsAhoyUtils {
    const val signatures = 0
    const val shipModel = Items.MODEL_SHIP_4253
    const val silkShipModel = Items.MODEL_SHIP_4254
    const val rightShip = "/save:ghostsahoy:right-ship"
    const val colorMatching = "/save:ghostsahoy:paint-ship"
    const val lastMapScrap = "/save:ghostahoy:map-scrap"
    const val windSpeed = "/save:ghostsahoy:windspeed"
    const val logout = "/save:ghostsahoy:bedsheet-costume"
    const val petitionsigns = "/save:ghostsahoy:petition"
    const val petitionstart = "/save:ghostsahoy:petition-start"
    const val petitioncomplete = "/save:ghostsahoy:petition-complete"
    const val getSignedBow = "/save:ghostsahoy:signed-bow"
    const val shipFlag = "/save:ghostsahoy:ship-flag"
    const val shipBottom = "/save:ghostsahoy:ship-bottom"
    const val shipSkull = "/save:ghostsahoy:ship-skull"
    const val croneEnlightened = "/save:ghostsahoy:crone-enlightened"
    const val petitionPresented = "/save:ghostsahoy:petition-presented"
    const val akharanuAsked = "/save:ghostsahoy:akharanu-asked"
    const val robesFound = "/save:ghostsahoy:robes-found"
    const val bookFound = "/save:ghostsahoy:book-found"
    const val manualGiven = "/save:ghostsahoy:manual-given"
    const val crabSpawned = "ghostsAhoyCrabSpawned"
    const val signaturesNeeded = 10

    /**
     * Moves the quest stage forward only. Several tracks of this quest can be done in any
     * order, so a plain setQuestStage could drag a player backwards.
     */
    /**
     * Both amulets let the player understand ghosts: the plain amulet of ghostspeak (552)
     * and the enchanted one (4250), which never discharges completely.
     */
    fun hasGhostspeak(player: Player?): Boolean =
        player != null && (inEquipment(player, Items.GHOSTSPEAK_AMULET_552) || inEquipment(player, Items.GHOSTSPEAK_AMULET_4250))

    fun advanceQuest(player: Player, stage: Int) {
        if (getQuestStage(player, Quests.GHOSTS_AHOY) < stage) {
            setQuestStage(player, Quests.GHOSTS_AHOY, stage)
        }
    }

    val rockJumpMap: Map<Location, Location> = mapOf(
        Location(3605, 3550, 0) to Location(3602, 3550, 0),
        Location(3601, 3550, 0) to Location(3604, 3550, 0),
        Location(3600, 3552, 0) to Location(3597, 3552, 0),
        Location(3596, 3552, 0) to Location(3599, 3552, 0),
        Location(3595, 3553, 0) to Location(3595, 3556, 0),
        Location(3595, 3557, 0) to Location(3595, 3554, 0),
        Location(3597, 3558, 0) to Location(3597, 3561, 0),
        Location(3597, 3562, 0) to Location(3597, 3559, 0),
        Location(3598, 3564, 0) to Location(3601, 3564, 0),
        Location(3602, 3564, 0) to Location(3599, 3564, 0)
    )

    fun jumpRockPath(player: Player) {
        queueScript(player, 0, QueueStrength.SOFT) {
            if (RandomFunction.getRandom(3) == 2) {
                animate(player, Animations.HUMAN_JUMP_FAIL_767)
                playGlobalAudio(player.location, Sounds.JUMP_AND_FALL_2463)
                player.impactHandler.manualHit(player, 1, ImpactHandler.HitsplatType.NORMAL, 1)
            } else {
                animate(player, Animations.HUMAN_JUMP_SHORT_GAP_741)
                playGlobalAudio(player.location, Sounds.JUMP2_2462)
            }
            return@queueScript stopExecuting(player)
        }
    }

    fun collectSignature(player: Player) {
        if (!inInventory(player, Items.PETITION_FORM_4283)) {
            sendMessage(player, "You need a petition form for the ghost to sign.")
            return
        }
        if (getAttribute(player, petitioncomplete, false)) {
            sendMessage(player, "You already have all the signatures you need.")
            return
        }

        val signs = getAttribute(player, petitionsigns, 0) + 1
        setAttribute(player, petitionsigns, signs)

        if (signs >= signaturesNeeded) {
            // The form is kept so it can be presented to Necrovarus.
            setAttribute(player, petitioncomplete, true)
            sendItemDialogue(
                player,
                Items.PETITION_FORM_4283,
                "You have succeeded in obtaining 10 signatures on the petition form!",
            )
        } else {
            sendItemDialogue(
                player,
                Items.PETITION_FORM_4283,
                "The ghost signs your petition. You have obtained $signs " + if (signs == 1) "signature so far." else "signatures so far.",
            )
        }
    }

    fun travelToDragontoothIsland(player: Player) {
        lock(player, 6)
        queueScript(player, 1, QueueStrength.SOFT) { qstage: Int ->
            when (qstage) {
                0 -> {
                    openInterface(player, Components.AHOY_BLACKOUT_7)
                    return@queueScript delayScript(player, 5)
                }

                1 -> {
                    teleport(player, location(3792, 3559, 0))
                    player.interfaceManager.close(Component(Components.AHOY_BLACKOUT_7))
                    return@queueScript stopExecuting(player)
                }

                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    fun travelToPortPhasmatys(player: Player) {
        lock(player, 6)
        queueScript(player, 1, QueueStrength.SOFT) { qstage: Int ->
            when (qstage) {
                0 -> {
                    openInterface(player, Components.AHOY_BLACKOUT_7)
                    return@queueScript delayScript(player, 5)
                }

                1 -> {
                    teleport(player, location(3702, 3487, 0))
                    player.interfaceManager.close(Component(Components.AHOY_BLACKOUT_7))
                    return@queueScript stopExecuting(player)
                }

                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    fun handleDyeSelection(player: Player, buttonID: Int, dyeId: Int) {
        val attribute = when (buttonID) {
            1 -> shipFlag
            2 -> shipBottom
            3 -> shipSkull
            else -> return
        }
        val color = when (dyeId) {
            Items.YELLOW_DYE_1765 -> "yellow"
            Items.BLUE_DYE_1767 -> "blue"
            Items.RED_DYE_1763 -> "red"
            else -> return
        }
        if (!removeItem(player, Item(dyeId, 1))) {
            sendMessage(player, "You don't have that dye any more.")
            return
        }
        sendItemDialogue(player, shipModel, "You dye the flag $color.")
        setAttribute(player, attribute, color)
        updateRightShip(player)
    }

    /** The colours the old man remembers (the same ones the mast reveals). */
    private fun updateRightShip(player: Player) {
        var matching = 0
        if (getAttribute(player, shipFlag, "") == "red") matching++
        if (getAttribute(player, shipBottom, "") == "blue") matching++
        if (getAttribute(player, shipSkull, "") == "blue") matching++
        setAttribute(player, colorMatching, matching)
        setAttribute(player, rightShip, matching == 3)
    }
}
