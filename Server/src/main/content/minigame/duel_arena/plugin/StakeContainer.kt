package content.minigame.duel_arena.plugin

import core.api.ContainerListener
import core.api.IfaceSettingsBuilder
import core.game.component.Component
import core.game.container.Container
import core.game.container.ContainerEvent
import core.game.container.ContainerType
import core.game.container.SortType
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.login.PlayerParser
import core.net.packet.PacketRepository
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.ContainerContext
import shared.consts.Components

class StakeContainer(
    private val player: Player,
    val session: DuelSession,
) : Container(28, ContainerType.DEFAULT, SortType.ID) {
    var listener: StakeListener? = null
    private var released = false

    init {
        listeners.add(StakeListener().also { listener = it })
    }

    fun open() {
        player.interfaceManager.openSingleTab(OVERLAY)
        player.packetDispatch.sendRunScript(150, "IviiiIsssssssss", *INVY_PARAMS)
        val options = IfaceSettingsBuilder().enableOptions(0..4).enableOption(9).build()
        player.packetDispatch.sendIfaceSettings(options, 0, Components.TRADESIDE_336, 0, 27)
        player.packetDispatch.sendIfaceSettings(options, 102, 631, 0, 27)
        player.packetDispatch.sendIfaceSettings(IfaceSettingsBuilder().enableOption(0).build(), 103, 631, 0, 27)
        PacketRepository.send(Encoders578.CONTAINER_PACKET, ContainerContext(player, -1, -1, 93, player.inventory, false))
    }

    /** CS2 99/206: own and opponent stakes are the two namespaces of inv_134. */
    fun sync() {
        val opposite = session.getOppositeContainer(player) ?: return
        if (player.interfaceManager.getComponent(631) == null && player.interfaceManager.getComponent(626) == null) return
        PacketRepository.send(Encoders578.CONTAINER_PACKET, ContainerContext(player, -1, -1, KEY, this, false))
        PacketRepository.send(Encoders578.CONTAINER_PACKET, ContainerContext(player, -1, -1, KEY, opposite, true))
    }

    fun offer(slot: Int, amount: Int) = transfer(player.inventory, this, slot, amount)

    fun withdraw(slot: Int, amount: Int) = transfer(this, player.inventory, slot, amount)

    private fun transfer(source: Container, destination: Container, slot: Int, amount: Int) {
        if (released || session.acceptState !in 0..1 || amount <= 0 || slot !in 0 until source.capacity()) return
        val item = source[slot] ?: return
        if (!item.definition.isTradeable) {
            player.sendMessage("You can't offer that item.")
            return
        }
        // Never mutate the source item to fit the destination. Container.add may clamp amounts.
        val moved = item.copy()
        moved.amount = minOf(amount, source.getAmount(item), destination.getMaximumAdd(item))
        if (moved.amount <= 0) return
        if (!source.remove(moved, slot, false)) return
        if (!destination.add(moved, false)) {
            source.add(moved, false)
            source.refresh()
            return
        }
        session.resetAccept()
        source.refresh()
        destination.refresh()
    }

    fun release() {
        if (released) {
            return
        }
        released = true
        if (!player.inventory.hasSpaceFor(this)) {
            player.bank.addAll(this)
            player.sendMessage("Your stake was sent to your bank due to invalid inventory space.")
            return
        }
        player.inventory.addAll(this)
        PlayerParser.save(player)
    }

    inner class StakeListener : ContainerListener {
        private fun syncBoth() {
            sync()
            session.getOppositeContainer(player)?.sync()
        }
        override fun update(c: Container?, event: ContainerEvent?) = syncBoth()
        override fun refresh(c: Container?) = syncBoth()
    }

    companion object {
        const val KEY = 134
        private val INVY_PARAMS =
            arrayOf<Any>(
                "",
                "",
                "",
                "",
                "Stake-X",
                "Stake-All",
                "Stake-10",
                "Stake-5",
                "Stake",
                -1,
                0,
                7,
                4,
                93,
                336 shl 16,
            )
        val OVERLAY = Component(Components.TRADESIDE_336)
    }
}
