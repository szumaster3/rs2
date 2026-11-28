package content.region.kandarin.witchaven.quest.seaslug.cutscene

import content.data.Quests

import core.api.*
import core.game.activity.Cutscene
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.Animations
import shared.consts.NPCs

/**
 * Represents repair boat cutscene.
 *
 * # Relations
 * - [Sea Slug quest][content.region.kandarin.witchaven.quest.seaslug.SeaSlugQuest]
 */
class HolgartCutscene(
    player: Player,
) : Cutscene(player) {
    override fun setup() {
        setExit(Location.create(2716, 3300, 0))
        if (player.settings.isRunToggled) {
            player.settings.toggleRun()
        }
        loadRegion(10803)// WITCHAVEN
        addNPC(NPCs.HOLGARTSUNKBOAT_698, 29, 36, Direction.EAST)
    }

    override fun runStage(stage: Int) {
        when (stage) {
            0 -> {
                fadeToBlack()
                timedUpdate(6)
            }

            1 -> {
                teleport(player, 28, 36)
                face(player, Location(2720, 3300, 0))
                fadeFromBlack()
                timedUpdate(3)
            }

            2 -> {
                sendChat(getNPC(NPCs.HOLGARTSUNKBOAT_698)!!, "Right me hearty. Let's get this boat fixed.")
                move(getNPC(NPCs.HOLGARTSUNKBOAT_698)!!, 30, 36)
                timedUpdate(3)
            }

            3 -> {
                teleport(getNPC(NPCs.HOLGARTSUNKBOAT_698)!!, 30, 36, 1)
                addNPC(NPCs.HOLGARTNOLEGS_4866, 30, 36, Direction.EAST)
                playAudio(player, 3026)
                animate(getNPC(NPCs.HOLGARTNOLEGS_4866)!!, Animations.SEASLUG_BOAT_REPAIR_4786)
                timedUpdate(8)
            }

            4 -> {
                sendChat(getNPC(NPCs.HOLGARTNOLEGS_4866)!!, "Huzzah! We've fixed the leaky old tub.")
                timedUpdate(3)
            }

            5 -> {
                end()
                setQuestStage(player, Quests.SEA_SLUG.questName, 5)
            }
        }
    }
}
