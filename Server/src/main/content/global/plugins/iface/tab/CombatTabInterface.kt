package content.global.plugins.iface.tab

import core.game.interaction.InterfaceListener
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.combat.CombatSwingHandler
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.combat.equipment.WeaponInterface.WeaponInterfaces
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.plugin.Plugin
import shared.consts.Components

/**
 * Handles the combat tab interface.
 *
 * @author Emperor, Vexia
 */
class CombatTabInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        WeaponInterfaces.values().forEach { inter ->
            on(inter.interfaceId) { player, _, _, buttonID, _, _ ->
                handleButton(player, buttonID)
            }
        }

        on(Components.WEAPON_FISTS_SEL_92) { player, _, _, buttonID, _, _ ->
            handleButton(player, buttonID)
        }
    }

    private fun handleButton(player: Player, buttonID: Int): Boolean {
        when (buttonID) {
            7, 9, 24, 26, 27 -> {
                GameWorld.Pulser.submit(object : Pulse(1, player) {
                    override fun pulse(): Boolean {
                        player.settings.toggleRetaliating()
                        return true
                    }
                })
            }

            8, 10, 11, 85 -> {
                GameWorld.Pulser.submit(object : Pulse(1, player) {
                    override fun pulse(): Boolean {
                        val inter = player.getExtension(WeaponInterface::class.java) as? WeaponInterface
                        if (inter != null && inter.isSpecialBar) {
                            player.settings.toggleSpecialBar()
                            if (player.settings.isSpecialToggled) {
                                val handler = CombatStyle.MELEE.swingHandler.getSpecial(player.equipment.getNew(3)?.id ?: -1)
                                if (handler != null) {
                                    @Suppress("UNCHECKED_CAST")
                                    val plugin = handler as? Plugin<Any>
                                    if (plugin?.fireEvent("instant_spec", player) == true) {
                                        handleInstantSpec(player, handler, plugin)
                                    }
                                }
                            }
                        }
                        return true
                    }
                })
            }

            0 -> return false
            else -> {
                val inter = player.getExtension(WeaponInterface::class.java) as? WeaponInterface ?: return false
                if (inter.setAttackStyle(buttonID)) {
                    when (buttonID) {
                        4, 5 -> inter.openAutocastSelect()
                        else -> {
                            if (player.properties.autocastSpell != null) {
                                inter.selectAutoSpell(-1, false)
                            }
                        }
                    }
                    return true
                }
                return false
            }
        }
        return true
    }

    companion object {
        private fun handleInstantSpec(player: Player, handler: CombatSwingHandler, plugin: Plugin<Any>) {
            handler.swing(player, player.properties.combatPulse.getVictim(), null)
        }
    }
}