package content.minigame.pest_control

import content.global.travel.charter.CharterShip
import content.minigame.pest_control.npc.*
import content.minigame.pest_control.reward.PCRewardInterface
import core.api.*
import core.cache.def.impl.NpcType
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.combat.ImpactHandler.HitsplatType
import core.game.node.item.Item
import core.game.world.map.RegionManager.getLocalNpcs
import core.game.world.update.flag.context.Graphics
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Graphics as Gfx

class PestControlListener : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles interaction with Void seal.
         */

        on(VOID_SEAL, IntType.ITEM, "rub", "operate") { player, node ->
            val operate = getUsedOption(player) == "operate"

            if (player.viewport.region?.regionId != 10536) {
                sendMessage(player, "You can only use the seal in Pest Control.")
                return@on true
            }

            val item = node as Item
            val isLastSeal = item.id == Items.VOID_SEAL_1_11673

            if (isLastSeal) {
                sendMessage(player, "The seal dissolves as the least of its power is unleashed.")
                removeItem(player, item)
            } else {
                sendMessage(player, "You unleash the power of the Void Knights!")
                val replacement = Item(item.id + 1)
                val container = if (operate) player.equipment else player.inventory
                container.replace(replacement, item.slot)
            }

            visualize(player, Animations.HUMAN_CASTHEAL_709, Gfx.SPIRAL_ATTACK_HUMAN_1177)
            animate(player, Animations.HUMAN_CASTHEAL_709)

            lock(player, 1)
            getLocalNpcs(player, 2)
                .filter {
                    it is PCDefilerNPC ||
                            it is PCRavagerNPC ||
                            it is PCShifterNPC ||
                            it is PCSpinnerNPC ||
                            it is PCSplatterNPC ||
                            it is PCTorcherNPC ||
                            it is PCBrawlerNPC
                }
                .forEach { npc ->
                    npc.impactHandler.manualHit(
                        player,
                        7 + RandomFunction.randomize(5),
                        HitsplatType.NORMAL,
                        1
                    )
                    npc.graphics(Graphics.create(Gfx.SPIRAL_ATTACK_1176))
                }

            return@on true
        }

        /*
         * Handles opening pest control interface with rewards.
         */

        on(VOID_KNIGHT, IntType.NPC, "exchange") { player, _ ->
            PCRewardInterface.open(player)
            return@on true
        }

        /*
         * Handles Squire NPC options near boat.
         */

        on(SQUIRE, IntType.NPC, "talk-to", "leave") { player, node ->
            val session =
                node
                    .asNpc()
                    .getExtension<PestControlSession>(
                        PestControlSession::class.java,
                    )
            when (getUsedOption(player)) {
                "talk-to" -> {
                    if (session == null) {
                        val handler = NpcType.getOptionHandlers()[getUsedOption(player)]
                        handler!!.handle(player, node, getUsedOption(player))
                        return@on true
                    } else {
                        openDialogue(player, SQUIRE, node, true)
                    }
                }
                "leave" -> {
                    if (session == null) {
                        CharterShip.PEST_TO_PORT_SARIM.sail(player)
                        return@on true
                    }
                    player.properties.teleportLocation = session.activity.leaveLocation
                }
            }
            return@on true
        }
    }

    companion object {
        const val SQUIRE = NPCs.PEST_SQUIRE_INSTANCE_3781

        private val VOID_SEAL =
            intArrayOf(
                Items.VOID_SEAL_8_11666,
                Items.VOID_SEAL_7_11667,
                Items.VOID_SEAL_6_11668,
                Items.VOID_SEAL_5_11669,
                Items.VOID_SEAL_4_11670,
                Items.VOID_SEAL_3_11671,
                Items.VOID_SEAL_2_11672,
                Items.VOID_SEAL_1_11673
            )
        private val VOID_KNIGHT =
            intArrayOf(
                NPCs.PEST_VOIDKNIGHT_1_3786,
                NPCs.PEST_VOIDKNIGHT_3_3788,
                NPCs.PEST_VOIDKNIGHT_4_3789,
                NPCs.PEST_VOIDKNIGHT_2_5956,
            )
    }
}
