package content.global.skill.hunter.imp

import core.api.*
import core.game.component.CloseEvent
import core.game.component.Component
import core.game.container.access.InterfaceContainer.generateItems
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components
import shared.consts.Items

class ImpBoxPlugin : InteractionListener, InterfaceListener {

    companion object {
        private const val SESSION_KEY = "imp_session"

        private val IMP_BOXES = intArrayOf(
            Items.IMP_IN_A_BOX1_10028,
            Items.IMP_IN_A_BOX2_10027
        )

        private fun isImpBox(id: Int): Boolean =
            id == Items.IMP_IN_A_BOX1_10028 ||
                    id == Items.IMP_IN_A_BOX2_10027 ||
                    id == Items.MAGIC_BOX_10025

        private fun downgrade(box: Item): Item = when (box.id) {
            Items.IMP_IN_A_BOX2_10027 -> Item(Items.IMP_IN_A_BOX1_10028)
            Items.IMP_IN_A_BOX1_10028 -> Item(Items.MAGIC_BOX_10025)
            else -> box
        }

        private fun findBox(player: Player): Pair<Int, Item>? {
            for (slot in 0 until player.inventory.capacity()) {
                val item = player.inventory.get(slot) ?: continue
                if (isImpBox(item.id)) {
                    return slot to item
                }
            }
            return null
        }

        private fun refresh(player: Player) {
            player.inventory.refresh()
        }

        fun open(player: Player) {
            if (findBox(player) == null) return

            if (player.inCombat()) {
                sendMessage(player, "You can't do this while in combat.")
                return
            }

            player.interfaceManager.open(Component(Components.IMP_BOX_478))?.apply {
                closeEvent = CloseEvent { p, _ ->
                    p.interfaceManager.openDefaultTabs()
                    true
                }
            }

            removeTabs(player, 0, 1, 2, 3, 4, 5, 6)

            player.generateItems(
                Components.IMP_BOX_478,
                14,
                listOf("Deposit"),
                5,
                7,
                93
            )

            sendString(
                player,
                "Select an item or stack of items to deposit.<br><col=ff7000>You can deposit 1 more item.",
                Components.IMP_BOX_478,
                13
            )
        }

        private fun handleDeposit(player: Player, slot: Int): Boolean {
            val item = player.inventory.get(slot) ?: return false

            if (isImpBox(item.id)) {
                sendMessage(player, "A magical force prevents you from banking this item.")
                return false
            }

            val copy = Item(item.id, item.amount)

            if (!player.bank.canAdd(copy)) {
                sendMessage(player, "The imp can't send that item to your bank.")
                return false
            }

            if (!player.inventory.remove(copy)) return false

            player.bank.add(copy)
            return true
        }

        private fun consumeCharge(player: Player) {
            val (slot, box) = findBox(player) ?: return
            player.inventory.replace(downgrade(box), slot)
            player.inventory.update()
        }
    }

    override fun defineListeners() {
        on(IMP_BOXES, IntType.ITEM, "bank", "talk-to") { player, _ ->
            open(player)
            return@on true
        }

        onUseWith(IntType.ITEM, IMP_BOXES) { player, used, _ ->
            val item = used.asItem()
            if (!player.bank.canAdd(item)) {
                sendMessage(player, "The imp can't send that item to your bank.")
                return@onUseWith true
            }
            runWorldTask {
                player.bank.add(Item(item.id, item.amount))
                player.inventory.remove(Item(item.id, item.amount))
                consumeCharge(player)
                refresh(player)
            }
            return@onUseWith true
        }
    }

    override fun defineInterfaceListeners() {
        onOpen(Components.IMP_BOX_478) { _, _ -> true }
        on(Components.IMP_BOX_478) { player, _, opcode, _, slot, _ ->
            when (opcode) {
                155 -> runWorldTask {
                    val success = handleDeposit(player, slot)
                    if (!success) return@runWorldTask

                    var count = player.getAttribute(SESSION_KEY, 0) as Int
                    count++

                    player.setAttribute(SESSION_KEY, count)
                    refresh(player)

                    if (count >= 2) {
                        player.removeAttribute(SESSION_KEY)
                        if (findBox(player) == null) return@runWorldTask
                        player.interfaceManager.close()
                        consumeCharge(player)
                    }
                }

                9 -> {
                    val item = player.inventory.get(slot) ?: return@on true
                    sendMessage(player, item.definition.examine)
                }
            }

            return@on true
        }

    }

}