package content.global.plugins.iface.ge

import core.api.*
import core.cache.def.impl.EnumType
import core.game.component.Component
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import shared.consts.Components

/** Revision 578 exchange_interface_sets_setup (676), enum 1087 and inventory 93. */
class ExchangeItemSets : InterfaceListener {
    override fun defineInterfaceListeners() {
        onClose(Components.EXCHANGE_ITEMSETS_645) { player, _ ->
            if (player.interfaceManager.getComponent(Components.EXCHANGE_SETS_SIDE_644) != null)
                closeSingleTab(player)
            return@onClose true
        }
    }

    companion object {
        /** Client uses enum keys 1..N and zero-based dynamic child indices. */
        internal fun setId(slot: Int): Int? {
            if (slot !in 0..73) return null
            return EnumType.get(1087).dataStore[slot + 1] as? Int
        }

        @JvmStatic
        fun openFor(player: Player) {
            if (player.interfaceManager.open(Component(Components.EXCHANGE_ITEMSETS_645)) == null) return
            openSingleTab(player, Components.EXCHANGE_SETS_SIDE_644)
            val settings = IfaceSettingsBuilder().enableOptions(0..2).build()
            player.packetDispatch.sendIfaceSettings(settings, 16, Components.EXCHANGE_ITEMSETS_645, 0, 73)
            player.packetDispatch.sendRunScript(676, "")
            player.packetDispatch.sendIfaceSettings(
                IfaceSettingsBuilder().enableOptions(0, 1, 9).build(), 0, Components.EXCHANGE_SETS_SIDE_644, 0, 27)
            // Script 150: options are transmitted in reverse order. Inventory listener already updates inv 93.
            player.packetDispatch.sendRunScript(150, "IviiiIsssssssss", "", "", "", "", "", "", "", "Exchange", "Components",
                -1, 0, 7, 4, 93, Components.EXCHANGE_SETS_SIDE_644 shl 16)
            refreshInventory(player)
        }
    }
}
