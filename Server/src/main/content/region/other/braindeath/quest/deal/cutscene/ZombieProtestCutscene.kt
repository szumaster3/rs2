package content.region.other.braindeath.quest.deal.cutscene

import content.region.other.braindeath.quest.deal.dialogue.CaptainBraindeathDialogue
import core.api.openDialogue
import core.api.openOverlay
import core.api.setMinimapState
import core.api.setQuestStageNonmonotonic
import core.game.activity.Cutscene
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.Components
import shared.consts.NPCs

class ZombieProtestCutscene(
    player: Player,
) : Cutscene(player) {
    override fun setup() {
        setExit(Location(2144, 5108, 1))
        if (player.settings.isRunToggled) {
            player.settings.toggleRun()
        }
        loadRegion(8527)
        setMinimapState(player, 2)
        addNPC(ZOMBIE_0, 6, 5, Direction.EAST)
    }

    override fun runStage(stage: Int) {
        when (stage) {
            0 -> {
                fadeToBlack()
                timedUpdate(6)
            }

            1 -> {
                teleport(player, 7, 0)
                moveCamera(7, 5)
                rotateCamera(7, 2, 300, 2)
                fadeFromBlack()
                timedUpdate(4)
            }

            2 -> {
                addNPC(ZOMBIE_1, 1, 3, Direction.NORTH)
                move(getNPC(ZOMBIE_0)!!, 0, 4)
                timedUpdate(4)
            }

            3 -> {
                addNPC(ZOMBIE_2, 7, 4, Direction.NORTH)
                addNPC(ZOMBIE_3, 7, 7, Direction.NORTH)
                addNPC(ZOMBIE_4, 1, 5, Direction.NORTH)
                openOverlay(player, Components.RUM_DEAL_TITLE_295)
                timedUpdate(1)
            }

            4 -> {
                moveCamera(0, 6)
                rotateCamera(3, 5, 300, 1)
                move(getNPC(ZOMBIE_2)!!, 7, 5)
                move(getNPC(ZOMBIE_3)!!, 0, 3)
                move(getNPC(ZOMBIE_4)!!, 6, 5)
                move(getNPC(ZOMBIE_0)!!, 3, 4)
                setQuestStageNonmonotonic(player, 101, 2)
                timedUpdate(6)
            }

            5 -> {
                end()
                setMinimapState(player, 0)
                openDialogue(player, CaptainBraindeathDialogue(2))
            }
        }
    }

    companion object {
        const val ZOMBIE_0 = NPCs.DEAL_ZOMBIE_PIRATES_1_2837
        const val ZOMBIE_1 = NPCs.DEAL_ZOMBIE_PIRATES_2_2838
        const val ZOMBIE_2 = NPCs.DEAL_ZOMBIE_PROTESTER_1_2831
        const val ZOMBIE_3 = NPCs.DEAL_ZOMBIE_PROTESTER_2_2832
        const val ZOMBIE_4 = NPCs.DEAL_ZOMBIE_PROTESTER_3_2833
    }
}
