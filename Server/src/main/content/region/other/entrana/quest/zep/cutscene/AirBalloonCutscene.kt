package content.region.other.entrana.quest.zep.cutscene

import core.api.faceLocation
import core.api.setVarbit
import core.game.activity.Cutscene
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import shared.consts.NPCs
import shared.consts.Vars

class AirBalloonCutscene(player: Player) : Cutscene(player) {

    override fun setup() {
        setExit(player.location)
        if (player.settings.isRunToggled) {
            player.settings.toggleRun()
        }
        loadRegion(11060)//ENTRANA
        addNPC(NPCs.AUGUSTE_5049, 57, 26, Direction.WEST)
    }

    override fun runStage(stage: Int) {
        when (stage) {
            0 -> {
                fadeToBlack()
                timedUpdate(6)
            }

            1 -> {
                teleport(player, 56, 26, 0)
                moveCamera(41, 25, 2000)
                rotateCamera(56, 27)
                val obj = getObject(55, 26)!!
                faceLocation(player, obj.location)
                fadeFromBlack()
                setVarbit(player, Vars.VARBIT_QUEST_ENLIGHTENED_JOURNEY_ENTRANA_BALLOON_2867, 2, true)
                timedUpdate(5)
            }

            2 -> {
                move(getNPC(NPCs.AUGUSTE_5049)!!, 56, 27)
                timedUpdate(1)
            }

            3 -> {
                moveCamera(39, 25, 2000, 2)
                timedUpdate(3)
            }

            4 -> {
                moveCamera(38, 24, 2000, 2)
                timedUpdate(3)
            }

            5 -> {
                end {}
            }
        }
    }
}