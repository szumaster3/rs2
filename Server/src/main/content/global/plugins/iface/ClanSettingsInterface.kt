package content.global.plugins.iface

import core.api.openInterface
import core.api.sendInputDialogue
import core.api.sendMessage
import core.api.sendString
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.Player
import core.game.system.communication.ClanRank
import core.game.system.communication.ClanRepository
import core.net.amsc.MSPacketRepository
import core.net.amsc.WorldCommunicator
import core.tools.StringUtils
import shared.consts.Components

/**
 * Represents the clan setup interface interactions for players.
 * @author Vexia
 */
class ClanSettingsInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        onNative(Components.CLANJOIN_589) { player, _, operation, buttonID, _, _ ->
            if (operation != InterfaceOperation.OP1 || buttonID !in listOf(12, 17)) return@onNative false
            if (buttonID == 12) {
                if (player.interfaceManager.opened != null) {
                    sendMessage(player, "Please close the interface you have open before using your 'Clan Setup'")
                } else {
                    openSettings(player)
                }
            }

            if (buttonID == 17) {
                player.communication.toggleLootshare(player)
            }
            return@onNative true
        }

        onNative(Components.CLANSETUP_590) { player, _, operation, buttonID, _, _ ->
            // interface_590.if3 declares distinct operation ranges on each control.
            val permitted = when (buttonID) {
                22 -> operation in InterfaceOperation.OP1..InterfaceOperation.OP2
                23, 24 -> operation in InterfaceOperation.OP1..InterfaceOperation.OP9
                25 -> operation in InterfaceOperation.OP4..InterfaceOperation.OP9
                26 -> operation in InterfaceOperation.OP1..InterfaceOperation.OP8
                33 -> operation == InterfaceOperation.OP1
                else -> false
            }
            if (!permitted) return@onNative false
            val clan = ClanRepository.get(player.name, true)

            when (buttonID) {
                22 -> {
                    if (operation == InterfaceOperation.OP1) {
                        sendInputDialogue(player, false, "Enter clan prefix:") { value ->
                            val clanName = StringUtils.formatDisplayName(value.toString())

                            if (WorldCommunicator.isEnabled()) {
                                MSPacketRepository.sendClanRename(player, clanName)
                            }

                            if (clan.name == "Chat disabled") {
                                sendMessage(player, "Your clan channel has now been enabled!")
                                sendMessage(
                                    player,
                                    "Join your channel by clicking 'Join Chat' and typing: ${player.username}",
                                )
                            }

                            clan.name = clanName
                            player.communication.clanName = clanName
                            updateSettings(player)
                            clan.update()
                        }
                    }

                    if (operation == InterfaceOperation.OP2) {
                        clan.name = "Chat disabled"
                        player.communication.clanName = ""
                        if (WorldCommunicator.isEnabled()) {
                            MSPacketRepository.sendClanRename(player, player.communication.clanName)
                        }
                        updateSettings(player)
                        clan.delete()
                    }
                }

                23 -> {
                    clan.joinRequirement = rankForOperation(operation)
                    player.communication.joinRequirement = clan.joinRequirement
                    MSPacketRepository.setClanSetting(player, 0, clan.joinRequirement)
                }

                24 -> {
                    clan.messageRequirement = rankForOperation(operation)
                    player.communication.messageRequirement = clan.messageRequirement
                    MSPacketRepository.setClanSetting(player, 1, clan.messageRequirement)
                }

                25 -> {
                    clan.kickRequirement = rankForOperation(operation)
                    player.communication.kickRequirement = clan.kickRequirement
                    MSPacketRepository.setClanSetting(player, 2, clan.kickRequirement)
                }

                26 -> {
                    clan.lootRequirement = if (operation == InterfaceOperation.OP1) ClanRank.NO_ONE else rankForOperation(operation)
                    player.communication.lootRequirement = clan.lootRequirement
                    MSPacketRepository.setClanSetting(player, 3, clan.lootRequirement)
                }

                33 -> sendMessage(player, "CoinShare is not available.")
            }

            updateSettings(player)
            clan.update()
            return@onNative true
        }
    }

    /**
     * Converts an opcode integer to a corresponding [ClanRank].
     *
     * @param opcode The opcode representing a clan rank.
     * @return The corresponding [ClanRank].
     */
    @Deprecated("Historical listener ID; use native operation indices in handlers.")
    fun getRank(opcode: Int): ClanRank = rankForOperation(InterfaceOperation.operationIndex(opcode))

    private fun rankForOperation(operation: Int): ClanRank =
        when (operation) {
            InterfaceOperation.OP1 -> ClanRank.ANYONE
            InterfaceOperation.OP2 -> ClanRank.ANY_FRIEND
            InterfaceOperation.OP3 -> ClanRank.RECRUIT
            InterfaceOperation.OP4 -> ClanRank.CORPORAL
            InterfaceOperation.OP5 -> ClanRank.SERGEANT
            InterfaceOperation.OP6 -> ClanRank.LIEUTENANT
            InterfaceOperation.OP7 -> ClanRank.CAPTAIN
            InterfaceOperation.OP8 -> ClanRank.GENERAL
            InterfaceOperation.OP9 -> ClanRank.ONLY_ME
            else -> ClanRank.NO_ONE
        }

    /**
     * Updates the clan settings UI strings for the specified player.
     *
     * @param player The player whose clan settings should be updated.
     */
    private fun updateSettings(player: Player) {
        val clan = ClanRepository.get(player.name, true)
        sendString(player, clan.name, Components.CLANSETUP_590, 22)
        // player.getPacketDispatch().sendConfig(1083, (isCoinshare() ? 1 : 0) << 18 | (isLootshare() ? 0 : 1));
        sendString(player, clan.joinRequirement.info, Components.CLANSETUP_590, 23)
        sendString(player, clan.messageRequirement.info, Components.CLANSETUP_590, 24)
        sendString(player, clan.kickRequirement.info, Components.CLANSETUP_590, 25)
        sendString(player, clan.lootRequirement.info, Components.CLANSETUP_590, 26)
    }

    /**
     * Opens the clan settings for the player.
     *
     * @param player The player.
     */
    private fun openSettings(player: Player) {
        openInterface(player, Components.CLANSETUP_590)
        updateSettings(player)
    }
}
