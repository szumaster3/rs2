package content.global.plugins.item.equipment

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.skill.Skills
import shared.consts.Items

class GlovesOfSilenceListener : InteractionListener
{

    override fun defineListeners()
    {

        /*
         * Handles repairing gloves of silence.
         */
        onUseWith(
            IntType.ITEM,
            Items.DARK_KEBBIT_FUR_10115,
            GlovesOfSilence.ITEM_ID
        )
        { player, _, _ ->

            /*
             * Crafting requirement.
             */
            if (getStatLevel(player, Skills.CRAFTING) < 64)
            {
                sendMessage(
                    player,
                    "You need a Crafting level of 64 to repair these gloves."
                )

                return@onUseWith true
            }

            /*
             * Gloves already new.
             */
            if (!GlovesOfSilence.isDamaged(player))
            {
                sendMessage(
                    player,
                    "These gloves are new."
                )

                return@onUseWith true
            }

            /*
             * Required items.
             */
            if (
                !allInInventory(
                    player,
                    Items.NEEDLE_1733,
                    Items.THREAD_1734,
                    Items.KNIFE_946,
                    Items.DARK_KEBBIT_FUR_10115,
                    GlovesOfSilence.ITEM_ID
                )
            )
            {
                sendMessage(
                    player,
                    "You need a needle, thread, knife and dark kebbit fur."
                )

                return@onUseWith true
            }

            /*
             * Repair gloves.
             */
            GlovesOfSilence.repair(player)

            /*
             * Consume materials.
             */
            removeItem(
                player,
                Items.DARK_KEBBIT_FUR_10115
            )

            removeItem(
                player,
                Items.THREAD_1734
            )

            sendMessage(
                player,
                "You carefully stitch the gloves back together."
            )

            return@onUseWith true
        }

        /*
         * Handles checking gloves condition.
         */
        on(
            GlovesOfSilence.ITEM_ID,
            IntType.ITEM,
            "operate",
            "check"
        )
        { player, _ ->

            sendMessage(
                player,
                GlovesOfSilence.getConditionMessage(player)
            )

            return@on true
        }
    }
}