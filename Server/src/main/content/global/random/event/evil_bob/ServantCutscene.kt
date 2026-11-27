package content.global.random.event.evil_bob

import content.data.GameAttributes
import core.api.closeInterface
import core.api.openInterface
import core.api.setAttribute
import core.api.setMinimapState
import core.game.activity.Cutscene
import core.game.node.entity.player.Player
import shared.consts.Components

enum class ServantDirection(val camStartX: Int, val camStartY: Int, val camRotateX: Int, val camRotateY: Int, val camMoveX: Int, val camMoveY: Int, val attrSuffix: String) {
    N(30, 43, 30, 51, 30, 46, "n"),
    E(35, 41, 43, 41, 38, 41, "e"),
    S(29, 38, 29, 30, 29, 35, "s"),
    W(25, 40, 18, 40, 22, 40, "w");
}

class ServantCutscene(
    player: Player,
    private val dir: ServantDirection
) : Cutscene(player) {

    override fun setup() {
        setExit(player.location)
        setAttribute(player, "${GameAttributes.RE_BOB_OBJ}-${dir.attrSuffix}", true)
        loadRegion(13642)// SCAPE_RUNE
    }

    override fun runStage(stage: Int) {
        when (stage) {
            0 -> {
                fadeToBlack()
                timedUpdate(5)
            }

            1 -> {
                teleport(player, 29, 41)
                moveCamera(dir.camStartX, dir.camStartY)
                timedUpdate(2)
            }

            2 -> {
                timedUpdate(2)
                openInterface(player, Components.MACRO_EVIL_BOB_186)
                rotateCamera(dir.camRotateX, dir.camRotateY, 300, 100)
                fadeFromBlack()
            }

            3 -> {
                timedUpdate(9)
                moveCamera(dir.camMoveX, dir.camMoveY, 300, 2)
            }

            4 -> {
                setMinimapState(player, 0)
                endWithoutFade()
                closeInterface(player)
            }
        }
    }
}