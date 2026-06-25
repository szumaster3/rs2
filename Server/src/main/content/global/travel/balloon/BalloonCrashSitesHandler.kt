package content.global.travel.balloon

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.game.world.update.flag.context.Animation
import core.tools.END_DIALOGUE
import shared.consts.Components

/**
 * Handles balloon crash site travel and dialogue.
 * @author Edith
 */
object BalloonCrashHandler {
    const val CRASH_SITE_REGION = 7244
    const val SCENERY_OCEAN_PLANK = 19126
    const val SCENERY_WOODLAND_DIRT = 19125

    const val NPC_OCEAN_AUGUSTE = 5051
    const val NPC_WOODLAND_AUGUSTE = 5052

    private const val SOUND_CRASH = 3244
    private const val RENDER_ANIMATION_SWIM = 188

    private val OCEAN_CRASH_LANDING = Location.create(1805, 4889, 0)
    private val OCEAN_CRASH_FACE_LOCATION = Location.create(1804, 4889, 0)
    private val OCEAN_CRASH_EXIT = Location.create(2867, 3368, 0)
    private val OCEAN_CRASH_EXIT_FACE_LOCATION = Location.create(2866, 3368, 0)

    private val WOODLAND_CRASH_LANDING = Location.create(1844, 4920, 0)
    private val WOODLAND_CRASH_FACE_LOCATION = Location.create(1843, 4921, 0)
    private val WOODLAND_FALADOR_EXIT = Location.create(3068, 3358, 0)
    private val WOODLAND_FALADOR_EXIT_FACE_LOCATION = Location.create(3068, 3357, 0)
    private val WOODLAND_ARDOUGNE_EXIT = Location.create(2625, 3243, 0)
    private val WOODLAND_ARDOUGNE_EXIT_FACE_LOCATION = Location.create(2625, 3242, 0)

    private const val ATTRIBUTE_WOODLAND_EXIT = "/save:enlightenedjourney_woodland_crash_exit"

    enum class WoodlandExit { FALADOR, ARDOUGNE }

    /**
     * Handles a balloon crash at the ocean crash site.
     *
     * @param player player affected by the crash.
     */
    fun crashAtOceanSite(player: Player) {
        playAudio(player, SOUND_CRASH)
        fadeTransition(player) {
            closeFlightInterface(player)
            teleport(player, OCEAN_CRASH_LANDING)
            oceanSwimmingAnimations(player)
            faceLocation(player, OCEAN_CRASH_FACE_LOCATION)
            sendMessage(player, "The balloon crashes.")
        }
    }

    /**
     * Handles leaving the ocean crash site and returning to shore.
     *
     * @param player player leaving the crash site.
     */
    fun leaveOceanCrashSite(player: Player) {
        sendMessage(player, "You climb onto the plank and swim to shore.")
        fadeTransition(player, fadeToBlack = true) {
            player.appearance.setAnimations()
            player.appearance.sync()
            teleport(player, OCEAN_CRASH_EXIT)
            faceLocation(player, OCEAN_CRASH_EXIT_FACE_LOCATION)
            sendMessage(player, "You manage to swim back to shore.")
        }
    }

    /**
     * Handles a balloon crash at the woodland crash site.
     *
     * @param player player affected by the crash.
     * @param exit selected exit location after the crash.
     */
    fun crashAtWoodlandSite(player: Player, exit: WoodlandExit) {
        playAudio(player, SOUND_CRASH)
        setAttribute(player, ATTRIBUTE_WOODLAND_EXIT, exit.name)
        fadeTransition(player) {
            closeFlightInterface(player)
            teleport(player, WOODLAND_CRASH_LANDING)
            faceLocation(player, WOODLAND_CRASH_FACE_LOCATION)
            sendMessage(player, "The balloon crashes.")
        }
    }

    /**
     * Handles leaving the woodland crash site through the selected exit.
     *
     * @param player player leaving the crash site.
     */
    fun leaveWoodlandCrashSite(player: Player) {
        val exit = getWoodlandExit(player)
        sendMessage(player, "You walk away from the crash site.")
        fadeTransition(player, fadeToBlack = true) {
            when (exit) {
                WoodlandExit.FALADOR -> {
                    teleport(player, WOODLAND_FALADOR_EXIT)
                    faceLocation(player, WOODLAND_FALADOR_EXIT_FACE_LOCATION)
                    sendPlayerDialogue(player, "The crash must have really sent us off course; I'm near Falador!")
                }

                WoodlandExit.ARDOUGNE -> {
                    teleport(player, WOODLAND_ARDOUGNE_EXIT)
                    faceLocation(player, WOODLAND_ARDOUGNE_EXIT_FACE_LOCATION)
                    // TODO: Placeholder message, find 09 source.
                    sendPlayerDialogue(player, "The crash must have really sent us off course; I'm near Ardougne!")
                }
            }
            removeAttribute(player, ATTRIBUTE_WOODLAND_EXIT)
        }
    }

    /**
     * Returns the stored woodland crash exit.
     *
     * @param player player with stored crash data.
     * @return selected woodland exit.
     */
    private fun getWoodlandExit(player: Player): WoodlandExit {
        return when (getAttribute(player, ATTRIBUTE_WOODLAND_EXIT, WoodlandExit.FALADOR.name)) {
            WoodlandExit.ARDOUGNE.name -> WoodlandExit.ARDOUGNE
            else -> WoodlandExit.FALADOR
        }
    }

    /**
     * Teleports players outside the crash area after logging out.
     *
     * @param player player leaving the game inside crash area.
     */
    /*
    fun leaveCrashOnLogout(player: Player) {
        when {
            withinDistance(player, OCEAN_CRASH_LANDING, 10) -> {
                teleport(player, OCEAN_CRASH_EXIT)
                faceLocation(player, OCEAN_CRASH_EXIT_FACE_LOCATION)
            }

            withinDistance(player, WOODLAND_CRASH_LANDING, 10) -> {
                val exit = getWoodlandExit(player)

                when (exit) {
                    WoodlandExit.FALADOR -> {
                        teleport(player, WOODLAND_FALADOR_EXIT)
                        faceLocation(player, WOODLAND_FALADOR_EXIT_FACE_LOCATION)
                    }

                    WoodlandExit.ARDOUGNE -> {
                        teleport(player, WOODLAND_ARDOUGNE_EXIT)
                        faceLocation(player, WOODLAND_ARDOUGNE_EXIT_FACE_LOCATION)
                    }
                }

                removeAttribute(player, ATTRIBUTE_WOODLAND_EXIT)
            }
        }
    }
    */

    /**
     * Closes balloon-related interfaces.
     *
     * @param player player with open balloon interface.
     */
    private fun closeFlightInterface(player: Player) {
        closeInterface(player)
        closeTabInterface(player)
        restoreTabs(player)
        showMinimap(player)
    }

    /**
     * Performs a screen fade transition while executing an action.
     *
     * @param player target player.
     * @param fadeToBlack whether to fade to black before action.
     * @param whileBlack action executed during fade.
     */
    private fun fadeTransition(player: Player, fadeToBlack: Boolean = false, whileBlack: () -> Unit) {
        if (fadeToBlack) {
            player.lock()
            openOverlay(player, Components.FADE_TO_BLACK_120)
            submitIndividualPulse(player, object : Pulse() {
                var counter = 0
                override fun pulse(): Boolean {
                    when (counter++) {
                        4 -> {
                            whileBlack()
                            closeOverlay(player)
                            openOverlay(player, Components.FADE_FROM_BLACK_170)
                        }

                        7 -> {
                            closeOverlay(player)
                            unlock(player)
                            return true
                        }
                    }
                    return false
                }
            })
            return
        }
        whileBlack()
        closeOverlay(player)
        openOverlay(player, Components.FADE_FROM_BLACK_170)
        submitIndividualPulse(player, object : Pulse() {
            var counter = 0

            override fun pulse(): Boolean {
                when (counter++) {
                    3 -> {
                        closeOverlay(player)
                        unlock(player)
                        return true
                    }
                }
                return false
            }
        })
    }

    /**
     * Applies swimming animation to a player after ocean crash.
     *
     * @param player player receiving animation
     */
    fun oceanSwimmingAnimations(player: Player) {
        player.appearance.setAnimations(Animation(RENDER_ANIMATION_SWIM))
        player.appearance.sync()
    }

}

/**
 * Handles balloon crash site interactions and logout cleanup.
 */
class BalloonCrashSiteListener : InteractionListener, MapArea {
    override fun defineListeners() {
        on(BalloonCrashHandler.SCENERY_OCEAN_PLANK, IntType.SCENERY, "use") { player, _ ->
            BalloonCrashHandler.leaveOceanCrashSite(player)
            return@on true
        }

        on(BalloonCrashHandler.SCENERY_WOODLAND_DIRT, IntType.SCENERY, "leave") { player, _ ->
            BalloonCrashHandler.leaveWoodlandCrashSite(player)
            return@on true
        }

        on(intArrayOf(BalloonCrashHandler.NPC_OCEAN_AUGUSTE,BalloonCrashHandler.NPC_WOODLAND_AUGUSTE), IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, BalloonCrashDialogue(), node.asNpc())
            return@on true
        }

    }

    override fun defineAreaBorders(): Array<ZoneBorders> {
        return arrayOf(getRegionBorders(BalloonCrashHandler.CRASH_SITE_REGION))
    }

    override fun getRestrictions(): Array<ZoneRestriction> {
        return arrayOf(ZoneRestriction.TELEPORT, ZoneRestriction.CANNON, ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.FIRES)
    }

    override fun areaEnter(entity: Entity)
    {
        if (entity is Player)
        {
            val p = entity.asPlayer()
            if(inBorders(p,1810, 4898, 1795, 4883))
            {
                BalloonCrashHandler.oceanSwimmingAnimations(p)
            }
        }
        super.areaEnter(entity)
    }

    /**
     * Handles player logout inside the crash region.
     */
    /*override fun areaLeave(entity: Entity, logout: Boolean)
    {
        if (logout && entity is Player)
        {
            when (entity.location.regionId)
            {
                BalloonCrashHandler.CRASH_SITE_REGION -> {
                    BalloonCrashHandler.leaveCrashOnLogout(entity)
                }
            }
        }

        super.areaLeave(entity, logout)
    }
    */
}

/**
 * Represents the Auguste dialogue at crash region.
 */
class BalloonCrashDialogue : DialogueFile()
{

    override fun handle(componentID: Int, buttonID: Int)
    {
        when(npc!!.id)
        {

            BalloonCrashHandler.NPC_OCEAN_AUGUSTE -> {
                when(stage)
                {
                    0 -> npcl(FaceAnim.SCARED, "Great Scott! Ocean! Ahhh!").also { stage++ }
                    1 -> playerl(FaceAnim.HALF_ASKING, "What happened?").also { stage++ }
                    2 -> npcl(FaceAnim.NEUTRAL, "Crashed, off course. Ocean! Oh dear... I feel very...").also { stage++ }
                    3 -> playerl(FaceAnim.THINKING, "Contemplative? Concerned for your life? Desperate?").also { stage++ }
                    4 -> npcl(FaceAnim.NEUTRAL, "No... more along the lines of ill... You might want to swim away now. Use the plank...").also { stage++ }
                    5 -> playerl(FaceAnim.ASKING, "How are you going to get back? What about the balloon?").also { stage++ }
                    6 -> npcl(FaceAnim.NEUTRAL, "I'm sure a fishing trawler will come by and give me a lift. Don't worry, I shall return.").also { stage = END_DIALOGUE }
                }
            }

            BalloonCrashHandler.NPC_WOODLAND_AUGUSTE -> {
                when(stage)
                {
                    0 -> npcl(FaceAnim.NEUTRAL, "Well, that didn't go as planned.").also { stage++ }
                    1 -> playerl(FaceAnim.ASKING, "So what now? How will we get the balloon back?").also { stage++ }
                    2 -> npcl(FaceAnim.NEUTRAL, "I'll have to get one of the monks to come pick it up. You head back on your own; just follow the path. I'll meet you back at Entrana.").also { stage = END_DIALOGUE }
                }
            }
        }
    }
}