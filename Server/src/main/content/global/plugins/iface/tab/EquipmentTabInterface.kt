package content.global.plugins.iface.tab

import content.global.skill.summoning.familiar.BurdenBeast
import core.api.*

import core.game.container.access.InterfaceContainer
import core.game.container.impl.EquipmentContainer
import core.game.global.action.EquipHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListeners
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.combat.DeathTask
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.prayer.PrayerType
import core.tools.Log
import shared.consts.Components

class EquipmentTabInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        onOpen(ITEMS_KEPT_ON_DEATH_102) { player, component ->
            val zoneType = player.zoneMonitor.type
            val itemsKeptOnDeath = DeathTask.getContainers(player).getOrNull(0)

            val amountKeptOnDeath = if (!player.skullManager.isSkulled && (itemsKeptOnDeath?.itemCount() ?: 0) < 3) {
                if (player.prayer[PrayerType.PROTECT_ITEMS]) 4 else 3
            } else {
                itemsKeptOnDeath?.itemCount() ?: 0
            }

            val slot0 = itemsKeptOnDeath?.getId(0) ?: 0
            val slot1 = itemsKeptOnDeath?.getId(1) ?: 0
            val slot2 = itemsKeptOnDeath?.getId(2) ?: 0
            val slot3 = itemsKeptOnDeath?.getId(3) ?: 0

            val hasSkull = if (player.skullManager.isSkulled) 1 else 0

            val beast: BurdenBeast? = if (player.familiarManager.hasFamiliar() && player.familiarManager.familiar.isBurdenBeast)
                player.familiarManager.familiar as BurdenBeast else null

            val hasBob = if (beast != null && !beast.container.isEmpty) 1 else 0
            val message = "You are skulled."

            val cs2Args = arrayOf<Any>(
                hasBob,
                hasSkull,
                slot3,
                slot2,
                slot1,
                slot0,
                amountKeptOnDeath,
                zoneType,
                message
            )

            if (amountKeptOnDeath > 4 && zoneType == 0) {
                log(this::class.java, Log.ERR, "Items kept on death interface should not contain more than 4 items when not in a safe zone!")
            }

            player.packetDispatch.sendRunScript(118, "siiooooii", *cs2Args)

            val settings = IfaceSettingsBuilder().enableAllOptions().build()
            player.packetDispatch.sendIfaceSettings(settings, 18, component.id, 0, itemsKeptOnDeath?.itemCount() ?: 0)
            player.packetDispatch.sendIfaceSettings(settings, 21, component.id, 0, DeathTask.getContainers(player).getOrNull(1)?.itemCount() ?: 0)

            return@onOpen true
        }

        // Cache 578, interface_387 and wornitems_slot: static components, sub-ID -1.
        onNative(Components.WORNITEMS_387) { player, _, operation, buttonID, slot, itemID ->
            val equipmentSlot = WORN_SLOTS[buttonID]
            if (equipmentSlot != null) {
                if (slot == -1) handleEquipmentOperation(player, operation, equipmentSlot, itemID)
            } else if (operation == InterfaceOperation.OP1) {
                when (buttonID) {
                    39 -> openInterface(player, Components.EQUIP_SCREEN2_667)
                    45 -> openInterface(player, ITEMS_KEPT_ON_DEATH_102)
                    // 42 is Price-checker; migrate separately, not to the old death screen.
                }
            }
            return@onNative true
        }

        onOpen(Components.EQUIP_SCREEN2_667) { player, component ->
            val settings = IfaceSettingsBuilder().enableAllOptions().build()
            player.packetDispatch.sendIfaceSettings(settings, 14, component.id, 0, 13)
            EquipmentContainer.update(player)
            openSingleTab(player, Components.INVENTORY_WEAR2_670)
            return@onOpen true
        }

        onNative(Components.EQUIP_SCREEN2_667) { player, _, operation, buttonID, slot, itemID ->
            if (buttonID == 14) handleEquipmentOperation(player, operation, slot, itemID)
            return@onNative true
        }

        onClose(Components.EQUIP_SCREEN2_667) { player, _ ->
            closeTabInterface(player)
            return@onClose true
        }

        onOpen(Components.INVENTORY_WEAR2_670) { player, component ->
            InterfaceContainer.generateItems(player, player.inventory.toArray().toList(), listOf("Equip"), component.id, 0, 4, 7, 93)
            return@onOpen true
        }

        onNative(Components.INVENTORY_WEAR2_670) { player, _, operation, buttonID, slot, itemID ->
            if (buttonID != 0) return@onNative true
            val item = player.inventory.get(slot) ?: return@onNative true
            if (item.id != itemID) return@onNative true
            when (operation) {
                InterfaceOperation.OP1 -> equipItem(player, slot)
                InterfaceOperation.OP10 -> sendMessage(player, item.definition.examine)
            }
            return@onNative true
        }
    }

    private fun handleEquipmentOperation(player: Player, operation: Int, slot: Int, itemID: Int) {
        val item = player.equipment.get(slot) ?: return
        if (item.id != itemID) return
        when (operation) {
            InterfaceOperation.OP1 -> EquipHandler.unequip(player, slot, itemID)
            InterfaceOperation.OP10 -> sendMessage(player, item.definition.examine)
            in InterfaceOperation.OP2..InterfaceOperation.OP5 -> {
                val option = item.definition.getParam(528 + operation - InterfaceOperation.OP2, "") ?: return
                if (option.isBlank()) return
                if (option.equals("operate", ignoreCase = true)) operateItem(player, slot)
                else InteractionListeners.run(item.id, IntType.ITEM, option.lowercase(), player, item)
            }
        }
    }

    private fun equipItem(player: Player, slot: Int) {
        val item = player.inventory.get(slot) ?: return

        if (item.definition.options.any { it in arrayOf("Equip", "Wield", "Wear") }) {
            InteractionListeners.run(item.id, IntType.ITEM, "equip", player, item)
        } else {
            sendMessage(player, "You can't wear that.")
        }
    }

    private fun operateItem(player: Player, slot: Int) {
        val item = player.equipment.get(slot) ?: return

        when {
            InteractionListeners.run(item.id, IntType.ITEM, "operate", player, item) -> return
            item.operateHandler?.handle(player, item, "operate") == true -> return
            else -> sendMessage(player, "You can't operate that.")
        }
    }

    companion object {
        private const val ITEMS_KEPT_ON_DEATH_102 = 102
        private val WORN_SLOTS = mapOf(8 to 0, 11 to 1, 14 to 2, 17 to 3, 20 to 4, 23 to 5,
            26 to 7, 29 to 9, 32 to 10, 35 to 12, 38 to 13)
    }
}