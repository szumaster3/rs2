package content.global.plugins.item.equipment

import core.api.Container
import core.api.getAttribute
import core.api.inEquipment
import core.api.removeItem
import core.api.setAttribute
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Items

/**
 * Gloves of silence manager.
 */
object GlovesOfSilence {

    /**
     * Gloves item id.
     */
    const val ITEM_ID =
        Items.GLOVES_OF_SILENCE_10075

    /**
     * Maximum failures before breaking.
     */
    private const val MAX_FAILURES = 50

    /**
     * Player attribute key.
     */
    private const val ATTRIBUTE =
        "/save:gloves-of-silence-failures"

    /**
     * Gets current failure count.
     */
    fun getFailures(player: Player): Int {
        return getAttribute(
            player,
            ATTRIBUTE,
            0
        )
    }

    /**
     * Sets failure count.
     */
    private fun setFailures(
        player: Player,
        amount: Int
    ) {
        setAttribute(
            player,
            ATTRIBUTE,
            amount
        )
    }

    /**
     * Remaining durability.
     */
    fun getRemainingUses(
        player: Player
    ): Int {
        return MAX_FAILURES -
                getFailures(player)
    }

    /**
     * Checks if gloves are damaged.
     */
    fun isDamaged(player: Player): Boolean {
        return getFailures(player) > 0
    }

    /**
     * Handles failed pickpocket.
     *
     * @return true if gloves broke.
     */
    fun onFailedPickpocket(
        player: Player,
        item: Item
    ): Boolean {

        if (item.id != ITEM_ID) {
            return false
        }

        val failures =
            getFailures(player) + 1

        /*
         * Gloves destroyed.
         */
        if (failures >= MAX_FAILURES) {

            if (inEquipment(player, ITEM_ID, 1)) {
                removeItem(
                    player,
                    item,
                    Container.EQUIPMENT
                )
            } else {
                removeItem(player, item)
            }

            /*
             * Reset all future gloves
             * to new condition.
             */
            setFailures(player, 0)

            return true
        }

        setFailures(player, failures)
        return false
    }

    /**
     * Repairs gloves.
     */
    fun repair(player: Player) {
        setFailures(player, 0)
    }

    /**
     * Gets condition message.
     */
    fun getConditionMessage(
        player: Player
    ): String {

        return when (getFailures(player)) {

            0 ->
                "These gloves are new."

            in 1..10 ->
                "These gloves are in good condition."

            in 11..20 ->
                "These gloves are starting to look quite shabby."

            in 21..30 ->
                "These gloves are starting to need repair."

            in 31..40 ->
                "These gloves are in need of repair."

            in 41..49 ->
                "These gloves are about to fall apart."

            else ->
                "These gloves are falling apart."
        }
    }
}