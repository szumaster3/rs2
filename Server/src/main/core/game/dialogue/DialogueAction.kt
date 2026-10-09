package core.game.dialogue

import core.game.node.entity.player.Player

/**
 * The interface Dialogue action.
 */
fun interface DialogueAction {

    /**
     * Handle.
     *
     * @param player the player
     * @param buttonId the button id
     */
    fun handle(player: Player, buttonId: Int)
}