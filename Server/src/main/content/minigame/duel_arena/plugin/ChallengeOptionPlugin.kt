package content.minigame.duel_arena.plugin

import core.api.setAttribute
import core.api.setVarbit
import core.game.component.Component
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.plugin.Plugin
import java.util.*

class ChallengeOptionPlugin : OptionHandler() {
    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        DuelArenaActivity.CHALLENGE_OPTION.setHandler(this)
        return this
    }

    override fun handle(
        player: Player,
        node: Node,
        option: String,
    ): Boolean {
        val other = node as Player
        if (other.interfaceManager.isOpened() || other.getExtension<Any?>(DuelSession::class.java) != null) {
            player.packetDispatch.sendMessage("Other player is busy at the moment.")
            return true
        }
        if (other.requestManager.target === player && other.getAttribute<Any>("duel:partner") === player) {
            player.requestManager.request(
                other,
                if (other.getAttribute<Boolean>(
                        "duel:staked",
                        false,
                    )
                ) {
                    DuelArenaActivity.STAKE_REQUEST
                } else {
                    DuelArenaActivity.FRIEND_REQUEST
                },
            )
            return true
        }
        if (player.interfaceManager.open(Component(DuelArenaActivity.DUEL_TYPE_SELECT.id)) == null) return true
        setAttribute(player, "duel:staked", false)
        setAttribute(player, "duel:partner", other)
        setVarbit(player, 4157, 1)
        return true
    }
}
