package core.net.packet

import core.game.interaction.InterfaceOperation

import content.data.GameAttributes
import content.global.plugins.iface.ge.StockMarket
import content.global.skill.magic.SpellListener
import content.global.skill.magic.SpellListeners
import content.global.skill.magic.SpellUtils
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.ServerConstants
import core.api.*
import core.api.utils.Vector
import core.cache.def.impl.ItemDefinition
import core.cache.def.impl.NPCDefinition
import core.cache.def.impl.SceneryDefinition
import core.game.container.Container
import core.game.container.impl.BankContainer
import core.game.event.ButtonClickEvent
import core.game.ge.GrandExchange.Companion.getOfferStats
import core.game.ge.GrandExchange.Companion.getRecommendedPrice
import core.game.ge.GrandExchangeOffer
import core.game.ge.PriceIndex
import core.game.interaction.*
import core.game.node.Node
import core.game.node.entity.combat.spell.MagicSpell
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.LogType
import core.game.node.entity.player.info.PlayerMonitor
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.info.login.LoginConfiguration
import core.game.node.entity.player.link.SpellButtonMapping
import core.game.node.entity.player.link.SpellBookManager
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.system.command.CommandSystem
import core.game.system.communication.ClanRank
import core.game.system.communication.CommunicationInfo
import core.game.system.communication.GlobalChat
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.repository.Repository
import core.game.world.update.flag.*
import core.game.world.update.flag.context.ChatMessage
import core.net.IoSession
import core.net.amsc.MSPacketRepository
import core.net.packet.`in`.Packet
import core.net.packet.`in`.RunScript
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.PlayerContext
import core.tools.Log
import core.worker.ManagementEvents
import proto.management.ClanMessage
import proto.management.JoinClanRequest
import proto.management.LeaveClanRequest
import shared.consts.Components
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Math.min
import java.util.*

object PacketProcessor {
    // The network reader and game thread share this queue. Bind each frame to
    // its original session so a reconnect cannot replay stale actions.
    private class QueuedPacket(val player: Player, val session: IoSession, val packet: Packet)
    private val queue = java.util.ArrayDeque<QueuedPacket>()

    @JvmStatic fun enqueue(player: Player, session: IoSession, pkt: Packet) {
        if (!session.isActive || player.session !== session || isLegacyOnly(pkt)) return
        if (pkt is Packet.IfAction && (pkt.optIndex !in 0..9 || ClientProt.interfaceOperationIndex(pkt.wireOpcode) != pkt.optIndex)) return
        if (pkt is Packet.ContinueOption && pkt.wireOpcode != ClientProt.IF_BUTTON_CONTINUE) return
        synchronized(queue) { queue.addLast(QueuedPacket(player, session, pkt)) }
    }

    private fun isLegacyOnly(pkt: Packet): Boolean = when (pkt) {
        is Packet.SlotSwitchSingleComponent, is Packet.SlotSwitchMultiComponent,
        is Packet.InteractWalk, is Packet.TrackingAfkTimeout,
        is Packet.PlayerPrefsUpdate, is Packet.PacketCountUpdate,
        is Packet.UnhandledOp, is Packet.DecodingError, is Packet.NoProcess,
        is Packet.TransmitVerifyAck -> true
        else -> false
    }

    @JvmStatic fun processQueue() {
        val countThisCycle = synchronized(queue) { queue.size }
        repeat(countThisCycle) {
            val queued = synchronized(queue) { queue.pollFirst() } ?: return
            if (!queued.session.isActive || queued.player.session !== queued.session) return@repeat
            val pkt = queued.packet
            try {
                processWithNativeRun(pkt)
            } catch (e: Exception) {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                log(this::class.java, Log.ERR, "Error Processing ${pkt::class.java.simpleName}: $sw")
            }
        }
    }

    private fun processWithNativeRun(pkt: Packet) {
        val player = when (pkt) {
            is Packet.NpcAction -> pkt.player
            is Packet.PlayerAction -> pkt.player
            is Packet.SceneryAction -> pkt.player
            is Packet.GroundItemAction -> pkt.player
            is Packet.UseWithNpc -> pkt.player
            is Packet.UseWithPlayer -> pkt.player
            is Packet.UseWithScenery -> pkt.player
            is Packet.UseWithGroundItem -> pkt.player
            is Packet.ComponentNpcAction -> pkt.player
            is Packet.ComponentPlayerAction -> pkt.player
            is Packet.ComponentSceneryAction -> pkt.player
            is Packet.ComponentGroundItemAction -> pkt.player
            else -> null
        }
        val previous = player?.currentMovement
        process(pkt)
        if (pkt.interactionRun == true && player != null) {
            val movement = player.currentMovement
            if (movement != null && movement !== previous && movement.isRunning) movement.enableInteractionRun()
        }
    }

    private fun process(pkt: Packet) {
        when (pkt) {
            is Packet.ItemAction -> processItemAction(pkt)
            is Packet.NpcAction -> processNpcAction(pkt)
            is Packet.SceneryAction -> processSceneryAction(pkt)
            is Packet.PlayerAction -> processPlayerAction(pkt)
            is Packet.GroundItemAction -> processGroundItemAction(pkt)
            is Packet.UseWithNpc,
            is Packet.UseWithItem,
            is Packet.UseWithScenery,
            is Packet.UseWithGroundItem,
            is Packet.UseWithPlayer -> processUseWith(pkt)

            is Packet.IfAction -> processIfAction(pkt)
            is Packet.CloseIface -> processCloseIface(pkt)
            is Packet.WorldspaceWalk,
            is Packet.MinimapWalk,
            is Packet.InteractWalk -> processWalkPacket(pkt)

            is Packet.AddFriend -> CommunicationInfo.add(pkt.player, pkt.username)
            is Packet.RemoveFriend -> CommunicationInfo.remove(pkt.player, pkt.username, false)
            is Packet.AddIgnore -> CommunicationInfo.block(pkt.player, pkt.username, pkt.temporary)
            is Packet.RemoveIgnore -> CommunicationInfo.remove(pkt.player, pkt.username, true)
            is Packet.ComponentItemAction,
            is Packet.ComponentPlayerAction,
            is Packet.ComponentNpcAction,
            is Packet.ComponentSceneryAction,
            is Packet.ComponentGroundItemAction -> processComponentUseWith(pkt)

            is Packet.NativeDrag -> processNativeDrag(pkt)
            is Packet.SlotSwitchSingleComponent,
            is Packet.SlotSwitchMultiComponent -> processSlotSwitch(pkt)

            is Packet.TrackingMouseClick,
            is Packet.TrackingAfkTimeout,
            is Packet.TrackingFocus,
            is Packet.TrackingCameraPos,
            is Packet.TrackingDisplayUpdate -> processTrackingPacket(pkt)

            is Packet.PlayerPrefsUpdate -> {/*TODO implement something that cares about this */
            }

            is Packet.Ping -> pkt.player.session.lastPing = System.currentTimeMillis()
            is Packet.JoinClan -> {
                if (pkt.clanName.isEmpty()) {
                    // ClanChat.leave sends an empty frame even when no channel is joined.
                    // It must never fall through to a JoinClanRequest with an empty name.
                    if (pkt.player.communication.currentClan.isEmpty()) return
                    val builder = LeaveClanRequest.newBuilder()
                    builder.clanName = pkt.player.communication.currentClan
                    builder.username = pkt.player.name
                    ManagementEvents.publish(builder.build())
                    return
                }
                sendMessage(pkt.player, "Attempting to join channel....:clan:")
                val builder = JoinClanRequest.newBuilder()
                builder.clanName = pkt.clanName
                builder.username = pkt.player.name
                ManagementEvents.publish(builder.build())
            }

            is Packet.SetClanRank -> {
                val rank = ClanRank.values().firstOrNull { it.value == pkt.rank } ?: return
                CommunicationInfo.updateClanRank(pkt.player, pkt.username, rank)
            }

            is Packet.KickFromClan -> {
                val clan = pkt.player.communication.clan ?: return
                val target = Repository.getPlayerByName(pkt.username) ?: return
                clan.kick(pkt.player, target)
            }

            is Packet.NativeQuickChat -> {
                if (pkt.player.details.isMuted) return
                val payload = QuickChat.encode(pkt.player, pkt.phrase, pkt.parameters)
                if (payload == null || payload.size > 255) {
                    pkt.player.sendMessage("This Quick Chat phrase is not yet supported.")
                    return
                }
                when (pkt.channel) {
                    0 -> {
                        val context = ChatMessage(pkt.player, "", 0, 0)
                        context.nativeQuickChat = payload
                        pkt.player.updateMasks.register(EntityFlag.Chat, context)
                    }
                    1 -> QuickChatDelivery.clan(pkt.player, payload)
                    2 -> QuickChatDelivery.privateMessage(pkt.player, pkt.recipient, payload)
                }
            }

            is Packet.InputPromptResponse -> {
                val script: ((Any) -> Boolean) =
                    pkt.player.getAttribute<((Any) -> Boolean)?>("runscript", null) ?: return
                if (pkt.player.locks.isInteractionLocked())
                    return
                try {
                    RunScript.processInput(pkt.player, pkt.response, script)
                } finally {
                    pkt.player.removeAttribute("parseamount")
                    pkt.player.removeAttribute("runscript")
                }
            }

            is Packet.ReportAbuse -> {
                if (pkt.target.isNullOrEmpty()) {
                    sendMessage(pkt.player, "Invalid player name.")
                    return
                }
                if (!GameWorld.accountStorage.checkUsernameTaken(pkt.target.lowercase())) {
                    sendMessage(pkt.player, "Invalid player name.")
                    return
                }
                if (pkt.target.equals(pkt.player.username, true)) {
                    pkt.player.sendMessage("You can't report yourself!")
                    return
                }
                // Revision 578 uses a different offence table. Preserve its wire ID.
                val offence = pkt.rule + 1
                if (offence !in setOf(5, 6, 7, 8, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22)) return
                val comment = pkt.comment.replace(Regex("[\\r\\n]"), " ").take(80)
                val muteRequested = pkt.modMute && pkt.player.details.rights != Rights.REGULAR_PLAYER
                PlayerMonitor.log(pkt.player, LogType.COMMAND,
                    "${pkt.player.name}-${pkt.target}-Abuse Report offence=$offence muteRequested=$muteRequested comment=$comment")
                sendMessage(pkt.player, "Thank you, your abuse report has been received.")
            }

            is Packet.TrackFinished -> pkt.player.musicPlayer.onTrackFinished(pkt.trackId)

            is Packet.GESetOfferItem -> {
                if (!pkt.player.interfaceManager.hasMainComponent(Components.STOCKMARKET_105) ||
                    pkt.player.interfaceManager.chatbox?.id != Components.EXCHANGE_SEARCH_389 ||
                    pkt.player.locks.isInteractionLocked()) return
                val index = pkt.player.getAttribute("ge-index", -1)
                if (index !in 0..5 || pkt.itemId == 65535 || ItemDefinition.getDefinitions()[pkt.itemId]?.id != pkt.itemId) return
                val offer = pkt.player.getAttribute("ge-temp", GrandExchangeOffer())
                if (!PriceIndex.canTrade(pkt.itemId)) {
                    sendMessage(pkt.player, "That item is blacklisted from the grand exchange.")
                    return
                }
                offer.itemID = pkt.itemId
                offer.sell = false
                offer.player = pkt.player
                offer.amount = 1
                offer.offeredValue = getRecommendedPrice(pkt.itemId, false)
                offer.index = index
                StockMarket.updateVarbits(pkt.player, offer, index, false)
                pkt.player.setAttribute("ge-temp", offer)
                pkt.player.packetDispatch.sendString(getOfferStats(pkt.itemId, false), 105, 142)
                pkt.player.interfaceManager.closeChatbox()
            }

            is Packet.Command -> {
                PlayerMonitor.log(pkt.player, LogType.COMMAND, pkt.commandLine)
                CommandSystem.commandSystem.parse(pkt.player, pkt.commandLine)
            }

            is Packet.ChatMessage -> {
                if (pkt.player.details.isMuted)
                    pkt.player.sendMessage("You have been muted due to breaking a rule.")
                else {
                    if (!pkt.clan && ServerConstants.ENABLE_GLOBALCHAT && pkt.message.startsWith("//")) {
                        if (getAttribute(pkt.player, GlobalChat.ATTR_GLOBAL_MUTE, false))
                            return

                        val messages = splitChatMessage(pkt.message.substring(2), pkt.player.name.length + 3, false)
                        for (message in messages) {
                            if (message.isNotBlank())
                                GlobalChat.process(pkt.player.username, message, Rights.getChatIcon(pkt.player))
                        }
                        return
                    } else if (pkt.clan || (pkt.message.startsWith("/") && pkt.player.communication.clan != null)) {
                        if (pkt.player.communication.clan == null) return
                        val messages = splitChatMessage(
                            if (pkt.clan) pkt.message else pkt.message.substring(1),
                            pkt.player.communication.clan.name.length + pkt.player.name.length,
                            pkt.player.details.rights.ordinal != 0
                        )
                        for (message in messages) {
                            if (message.isBlank()) continue
                            val builder = ClanMessage.newBuilder()
                            builder.sender = pkt.player.name
                            builder.clanName = pkt.player.communication.clan.owner.lowercase().replace(" ", "_")
                            builder.message = message
                            builder.rank = Rights.getChatIcon(pkt.player)
                            ManagementEvents.publish(builder.build())
                        }
                        return
                    }
                    PlayerMonitor.logChat(pkt.player, "public", pkt.message)
                    val ctx = ChatMessage(pkt.player, pkt.message, pkt.effects, pkt.message.length)
                    pkt.player.updateMasks.register(EntityFlag.Chat, ctx)
                }
            }

            is Packet.ChatSetting -> {
                MSPacketRepository.sendChatSetting(pkt.player, pkt.public, pkt.private, pkt.trade)
                pkt.player.settings.updateChatSettings(pkt.public, pkt.private, pkt.trade)
            }

            is Packet.PrivateMessage -> {
                if (pkt.player.details.isMuted)
                    pkt.player.sendMessage("You have been muted due to breaking a rule.")
                else
                    CommunicationInfo.sendMessage(pkt.player, pkt.username, pkt.message)
                PlayerMonitor.logPrivateChat(pkt.player, pkt.username, pkt.message)
            }

            is Packet.PacketCountUpdate -> {
                // Legacy 530-only event. The native TRANSMITVAR_VERIFYID acknowledgement
                // is recorded separately and must never change the outgoing verify counter.
                return
            }

            is Packet.ContinueOption -> {
                val player = pkt.player
                val component = player.interfaceManager.chatbox ?: return
                if (component.id != pkt.iface) return
                player.debug("[CONTINUE OPT]----------")
                player.debug("Iface: ${pkt.iface}")
                player.debug("Child: ${pkt.child}")
                player.debug("Slot: ${pkt.slot}")
                player.debug("------------------------")
                if (player.dialogueInterpreter.dialogue == null) {
                    player.interfaceManager.closeChatbox()
                    player.dialogueInterpreter.actions.removeFirstOrNull()?.handle(player, pkt.child)
                    if (!InterfaceListeners.run(player, component, InterfaceOperation.CONTINUE, pkt.child, pkt.slot, -1))
                        component.plugin?.handle(player, component, InterfaceOperation.CONTINUE, pkt.child, pkt.slot, -1)
                    return
                }
                player.dialogueInterpreter.handle(pkt.iface, pkt.child)
            }

            //if (!getAttribute(GameAttributes.TUTORIAL_COMPLETE, false)) {
            //    packetDispatch.sendRunScript(102, "s", messages[0]);
            is Packet.ItemExamine -> {
                val def = ItemDefinition.forId(pkt.id) ?: return
                pkt.player.debug("[ITEM] ID: ${pkt.id} Value: ${def.value} Model: ${def.interfaceModelId}")
                if (!pkt.player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false))
                    pkt.player.packetDispatch.sendRunScript(102, "s", def.examine)
                  else
                    pkt.player.sendMessage(def.examine)

            }

            is Packet.SceneryExamine -> {
                val def = SceneryDefinition.forId(pkt.id) ?: return
                pkt.player.debug("[SCENERY]---------------------")
                pkt.player.debug("ID: ${pkt.id}")
                def.configFile?.let { pkt.player.debug("Varbit: ${it.id}") }
                pkt.player.debug("------------------------------")
                if (!pkt.player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false))
                    pkt.player.packetDispatch.sendRunScript(102, "s", def.examine)
                else
                    pkt.player.sendMessage(def.examine)
            }

            is Packet.NpcExamine -> {
                val def = NPCDefinition.forId(pkt.id)
                pkt.player.debug("[NPC]-------------------------")
                pkt.player.debug("ID: ${pkt.id}")
                if (def.configFileId != -1)
                    pkt.player.debug("Varbit: ${def.configFileId}")
                pkt.player.debug("------------------------------")
                if (!pkt.player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false))
                    pkt.player.packetDispatch.sendRunScript(102, "s", def.examine)
                else
                    pkt.player.sendMessage(def.examine)
            }


            else -> log(this::class.java, Log.WARN, "Unprocessed Packet: ${pkt::class.java.simpleName}")
        }
    }

    private fun processTrackingPacket(pkt: Packet) {
        when (pkt) {
            is Packet.TrackingFocus -> {}
            is Packet.TrackingDisplayUpdate -> {
                pkt.player.session.clientInfo.screenWidth = pkt.screenWidth
                pkt.player.session.clientInfo.screenHeight = pkt.screenHeight
                pkt.player.session.clientInfo.displayMode = pkt.displayMode
                pkt.player.interfaceManager.switchWindowMode(pkt.windowMode)
            }

            is Packet.TrackingAfkTimeout -> {
               // pkt.player.packetDispatch.sendLogout()
            }

            is Packet.TrackingCameraPos -> {
                //TODO Refactor the player monitor to be actually useful and log this
            }

            is Packet.TrackingMouseClick -> {
                //TODO see above todo

            }

            else -> {}
        }
    }

    private fun activeWidget(player: Player, hash: Int): Boolean =
        player.interfaceManager.getComponent(hash ushr 16)?.isHidden == false

    // Cache 578: inventory_init / script1448 (inv_93), script1450 (inv_95), script787 (inv_94).
    private fun nativeContainer(player: Player, hash: Int): Container? {
        if (!activeWidget(player, hash)) return null
        return when (hash) {
            (149 shl 16), (763 shl 16) -> player.inventory
            ((762 shl 16) or 87) -> if (player.bank.isOpen) player.bank else null
            ((667 shl 16) or 14) -> player.equipment
            else -> null
        }
    }

    private fun processNativeDrag(pkt: Packet.NativeDrag) {
        val player = pkt.player
        if (player.locks.isInteractionLocked() || player.locks.isComponentLocked() ||
            !activeWidget(player, pkt.target)) return
        val container = nativeContainer(player, pkt.source) ?: return
        if (pkt.sourceSlot !in 0 until container.capacity()) return
        val item = container[pkt.sourceSlot] ?: return
        if (item.id != pkt.sourceItem) return
        if (pkt.source == pkt.target) {
            if (container !== player.inventory && container !== player.bank) return
            if (pkt.targetSlot !in 0 until container.capacity() || pkt.sourceSlot == pkt.targetSlot) return
            val target = container[pkt.targetSlot]
            if ((target?.id ?: 65535) != pkt.targetItem) return
            if (container === player.bank) {
                // Empty cells are hidden by script1456. Cross-tab inserts need boundary updates.
                if (target == null) return
                if (player.bank.isInsertItems && player.bank.getTabByItemSlot(pkt.sourceSlot) != player.bank.getTabByItemSlot(pkt.targetSlot)) {
                    player.bank.moveToTab(pkt.sourceSlot, player.bank.getTabByItemSlot(pkt.targetSlot), pkt.targetSlot)
                } else {
                    switchItem(pkt.sourceSlot, pkt.targetSlot, container, player.bank.isInsertItems, player)
                }
            } else if (pkt.source == (149 shl 16)) {
                switchItem(pkt.sourceSlot, pkt.targetSlot, container, false, player)
            }
            return
        }
        // Static bank tab headers: enum_1613; the target is not an inventory slot.
        if (pkt.source != ((762 shl 16) or 87) || (pkt.target ushr 16) != 762 ||
            pkt.targetSlot != 65535 || pkt.targetItem != 65535) return
        val tab = when (val child = pkt.target and 65535) {
            56 -> 10 // View all: move to the ungrouped bank section.
            54, 52, 50, 48, 46, 44, 42, 40 -> (56 - child) / 2
            else -> return
        }
        player.bank.moveToTab(pkt.sourceSlot, tab, -1)
    }

    private fun processSlotSwitch(pkt: Packet) {
        //TODO refactor this to function as callbacks to InterfaceListeners, e.g. onSlotSwitch
        //TODO See above TODO, I hate Arios. This was hardcoded in the packet decoder.
        if (pkt is Packet.SlotSwitchMultiComponent) {
            if (pkt.destIface == 762) {
                if (pkt.destChild == 73) {
                    val container = pkt.player.bank
                    switchItem(pkt.sourceSlot, pkt.destSlot, container, pkt.player.bank.isInsertItems, pkt.player)
                } else {
                    val tabIndex = BankContainer.getArrayIndex(pkt.destChild)
                    if (tabIndex > -1) {
                        val secondSlot =
                            if (tabIndex == 10) pkt.player.bank.freeSlot() else pkt.player.bank.tabStartSlot[tabIndex] + pkt.player.bank.getItemsInTab(
                                tabIndex
                            )
                        val inSlot: Item = pkt.player.bank.get(pkt.sourceSlot)
                        if (secondSlot == -1 && pkt.player.bank.remove(inSlot)) {
                            pkt.player.bank.add(inSlot)
                            return
                        }
                        val childId = pkt.player.bank.getTabByItemSlot(pkt.sourceSlot)
                        if (secondSlot > pkt.sourceSlot) {
                            pkt.player.bank.insert(pkt.sourceSlot, secondSlot - 1)
                        } else if (pkt.sourceSlot > secondSlot) {
                            pkt.player.bank.insert(pkt.sourceSlot, secondSlot)
                        }
                        pkt.player.bank.increaseTabStartSlots(tabIndex)
                        pkt.player.bank.decreaseTabStartSlots(childId)
                        pkt.player.bank.setTabConfigurations()
                        return
                    }
                }
                return
            } else {
                switchItem(pkt.sourceSlot, pkt.destSlot, pkt.player.inventory, false, pkt.player)
            }
        } else if (pkt is Packet.SlotSwitchSingleComponent) {
            val container = if (pkt.iface == 762) pkt.player.bank else pkt.player.inventory
            switchItem(pkt.sourceSlot, pkt.destSlot, container, pkt.isInsert, pkt.player)
        }
    }

    private fun processComponentUseWith(pkt: Packet) {
        val iface: Int
        val child: Int
        val target: Node
        val targetId: Int
        val player: Player
        val type: Int
        if (pkt is Packet.ComponentGroundItemAction) {
            player = pkt.player
            child = pkt.child
            iface = pkt.iface
            target = GroundItemManager.get(pkt.itemId, Location.create(pkt.x, pkt.y, player.location.z), player)
                ?: return sendClearMinimap(player).also { sendMessage(player, "Too late!") }
            targetId = pkt.itemId
            type = SpellListener.GROUND_ITEM
        } else if (pkt is Packet.ComponentPlayerAction) {
            player = pkt.player
            child = pkt.child
            iface = pkt.iface
            target = Repository.players[pkt.otherIndex] ?: return sendClearMinimap(player)
            targetId = target.id
            type = SpellListener.PLAYER
        } else if (pkt is Packet.ComponentSceneryAction) {
            player = pkt.player
            child = pkt.child
            iface = pkt.iface
            target = RegionManager.getObject(player.location.z, pkt.x, pkt.y, pkt.sceneryId) ?: return sendClearMinimap(player)
            targetId = pkt.sceneryId
            type = SpellListener.OBJECT
        } else if (pkt is Packet.ComponentNpcAction) {
            if (pkt.npcIndex !in 1 until ServerConstants.MAX_NPCS)
                return sendClearMinimap(pkt.player)
            player = pkt.player
            child = pkt.child
            iface = pkt.iface
            target = Repository.npcs[pkt.npcIndex] ?: return sendClearMinimap(player)
            targetId = target.id
            type = SpellListener.NPC
        } else {
            if (pkt !is Packet.ComponentItemAction) return
            player = pkt.player
            child = pkt.child
            iface = pkt.iface
            val container = pkt.nativeSelection?.target?.let { nativeContainer(player, it) }
                ?: if (pkt.nativeSelection == null) player.inventory else return
            // Item-target spell listeners operate on inventory slots; do not cast on bank/equipment.
            if (container !== player.inventory) return
            target = container[pkt.slot] ?: return
            targetId = pkt.itemId
            type = SpellListener.ITEM
        }

        val selection = pkt.nativeSelection
        if (selection != null && (!activeWidget(player, selection.source) ||
                (selection.target != null && !activeWidget(player, selection.target)))) return
        if (player.locks.isInteractionLocked() || player.locks.isComponentLocked()) return
        if (target !is Item && !target.isActive) return
        if (targetId != target.id)
            return
        if (player.getAttribute("magic:delay", -1) > GameWorld.ticks)
            return
        val book = SpellUtils.getBookNameFromInterface(iface)
        if (book != "none" && player.spellBookManager.spellBook != iface) return
        val spellButton = if (iface == 192 || iface == 193 || iface == 430)
            SpellButtonMapping.getSpellId(iface, child) ?: return
        else child
        if (book != "none")
            SpellListeners.run(spellButton, type, book, player, target)
        when (iface) {
            430, 192, 193 -> SpellBookManager.SpellBook.forInterface(iface)
                ?.let { MagicSpell.castSpell(player, it, spellButton, target) }

            662 -> {
                if (player.familiarManager.hasFamiliar())
                    player.familiarManager.familiar.executeSpecialMove(
                        FamiliarSpecial(
                            target,
                            iface,
                            child,
                            target as? Item
                        )
                    )
                else
                    player.sendMessage("You don't have a familiar.")
            }
        }
    }

    private fun processWalkPacket(pkt: Packet) {
        val player: Player
        val x: Int
        val y: Int
        val isRunning: Boolean
        if (pkt is Packet.WorldspaceWalk) {
            player = pkt.player
            x = pkt.destX
            y = pkt.destY
            isRunning = pkt.isRun
        } else if (pkt is Packet.InteractWalk) {
            player = pkt.player
            x = pkt.destX
            y = pkt.destY
            isRunning = pkt.isRun
        } else {
            if (pkt !is Packet.MinimapWalk) return
            player = pkt.player
            x = pkt.destX
            y = pkt.destY
            isRunning = pkt.isRun
            //there's more data in this packet, we're just not using it
        }

        var loc = Location.create(x, y, player.location.z)
        var canWalk = !player.locks.isMovementLocked()

        val vec = Vector.betweenLocs(player.location, loc)
        if (vec.magnitude() > ServerConstants.MAX_PATHFIND_DISTANCE) {
            val newVec = vec.normalized() * (ServerConstants.MAX_PATHFIND_DISTANCE - 1)
            loc = player.location.transform(newVec)
        }

        if (canWalk && player.interfaceManager.isOpened() && !player.interfaceManager.opened!!.definition!!.isWalkable)
            canWalk = canWalk && player.interfaceManager.close()
        if (canWalk && player.interfaceManager.hasChatbox() && !player.interfaceManager.chatbox!!.definition!!.isWalkable)
            player.interfaceManager.closeChatbox()

        if (!canWalk || !player.dialogueInterpreter.close()) {
            player.debug("[WALK ACTION]-- Action canceled: player locked, interfaces open, or distance too far.")
            return sendClearMinimap(player)
        }

        if (pkt is Packet.InteractWalk) {
            //Arios did this, so we're replicating it for now. Probably wrong.
            player.walkingQueue.isRunning = isRunning
            return
        }

        player.face(null)
        player.faceLocation(null)
        player.scripts.reset()

        player.pulseManager.run(object : MovementPulse(player, loc, isRunning) {
            override fun pulse(): Boolean {
                if (isRunning)
                    player.walkingQueue.isRunning = false
                return true
            }
        })
    }

    private fun processCloseIface(pkt: Packet.CloseIface) {
        val player = pkt.player
        if (player.getAttribute("logging_in", false)) {
            if (player.session.isActive && !player.isPlaying) LoginConfiguration.configureGameWorld(player)
            return
        }
        player.interfaceManager.close()
        if (player.getAttribute<Any?>("worldMap:viewing") != null) {
            player.removeAttribute("worldMap:viewing")
            player.packetDispatch.sendWindowsPane(if (player.interfaceManager.isResizable) 746 else 548, 2)
            player.unlock()
        }
    }

    private fun processIfAction(pkt: Packet.IfAction) {
        if (pkt.optIndex !in 0..9) return
        val listenerOperation = InterfaceOperation.listenerId(pkt.optIndex)
        val player = pkt.player
        if (player.getAttribute("logging_in", false)) return
        player.debug("[IF ACTION]--------------------------")
        player.debug("Iface: ${pkt.iface}, Button: ${pkt.child}")
        player.debug("Slot: ${pkt.slot}, ItemID: ${pkt.itemId}")
        player.debug("RCM Index: ${pkt.optIndex}, Op: ${ClientProt.name(pkt.wireOpcode)}")
        player.debug("-------------------------------------")
        if (pkt.iface != 900 && player.dialogueInterpreter.dialogue != null && pkt.iface != 64 && pkt.iface != 746 && pkt.iface != 548)
            player.dialogueInterpreter.close()
        if (player.locks.isComponentLocked())
            return
        if (player.zoneMonitor.clickButton(pkt.iface, pkt.child, pkt.slot, pkt.itemId, listenerOperation))
            return
        val c = player.interfaceManager.getComponent(pkt.iface) ?: return
        if (c.isHidden)
            return
        // Inventory 149:0 uses dynamic IF_BUTTON children in revision 578.
        // inventory_init maps item options 1,2,3,4,5 to widget operations 1,2,3,7,8.
        if (pkt.iface == Components.INVENTORY_149 && pkt.child == 0) {
            if (pkt.slot !in 0 until player.inventory.capacity()) return
            val item = player.inventory.get(pkt.slot) ?: return
            if (pkt.itemId != item.id) return
            if (pkt.optIndex == 9) {
                sendMessage(player, item.definition.examine)
                return
            }
            val itemOption = when (pkt.optIndex) {
                0, 1, 2 -> pkt.optIndex
                6 -> 3
                7 -> 4
                else -> return
            }
            if (itemOption !in item.interaction.options.indices) return
            processItemAction(Packet.ItemAction(player, itemOption, item.id, pkt.slot, pkt.iface, pkt.child))
            return
        }
        val plugin = c.plugin
        if (pkt.iface == content.region.other.tutorial_island.plugin.CharacterDesign.INTERFACE_ID) {
            if (pkt.optIndex == 0 && content.region.other.tutorial_island.plugin.CharacterDesign.handleButtons(player, pkt.child, pkt.slot)) {
                player.dispatch(ButtonClickEvent(c.id, pkt.child))
            }
            return
        }
        player.dispatch(ButtonClickEvent(c.id, pkt.child))
        if (core.game.node.entity.player.link.AutocastManager.handle(player, pkt.iface, pkt.child, pkt.optIndex)) return
        if (!InterfaceListeners.run(player, c, listenerOperation, pkt.child, pkt.slot, pkt.itemId))
            plugin?.handle(player, c, listenerOperation, pkt.child, pkt.slot, pkt.itemId)
    }

    private fun selectedContainer(pkt: Packet, player: Player, iface: Int, target: Boolean = false): Container? {
        val native = pkt.nativeSelection ?: return getLikelyContainerForIface(player, iface)
        val hash = if (target) native.target ?: return null else native.source
        val container = nativeContainer(player, hash) ?: return null
        // Native use-with content consumes inventory items; bank/equipment targets require explicit listeners.
        return container.takeIf { it === player.inventory }
    }

    private fun processUseWith(pkt: Packet) {
        val node: Node
        var childNode: Node? = null
        val item: Item
        val itemId: Int
        val nodeId: Int
        val type: IntType
        val player: Player
        if (pkt is Packet.UseWithNpc) {
            val container = selectedContainer(pkt, pkt.player, pkt.iface) ?: return sendClearMinimap(pkt.player)
            itemId = pkt.itemId
            val itemSlot = pkt.slot
            item = container[itemSlot] ?: return sendClearMinimap(pkt.player)
            node = Repository.npcs[pkt.npcIndex] ?: return sendClearMinimap(pkt.player)
            childNode = node.getShownNPC(pkt.player)
            nodeId = node.id
            type = IntType.NPC
            player = pkt.player
        } else if (pkt is Packet.UseWithScenery) {
            val source = pkt.nativeSelection?.source
            val container = if (source == null) pkt.player.inventory else nativeContainer(pkt.player, source) ?: return
            if (container !== pkt.player.inventory) return
            item = container[pkt.slot] ?: return sendClearMinimap(pkt.player)
            node = RegionManager.getObject(pkt.player.location.z, pkt.x, pkt.y, pkt.sceneryId) ?: return sendClearMinimap(pkt.player)
            if (node.id != pkt.sceneryId) return sendClearMinimap(pkt.player)
            childNode = node.asScenery().getChild(pkt.player)
            itemId = pkt.itemId
            nodeId = node.id
            type = IntType.SCENERY
            player = pkt.player
        } else if (pkt is Packet.UseWithItem) {
            val containerUsed = selectedContainer(pkt, pkt.player, pkt.usedIface) ?: return
            val containerWith = selectedContainer(pkt, pkt.player, pkt.usedWithIface, true) ?: return
            item = containerUsed[pkt.usedSlot] ?: return
            node = containerWith[pkt.usedWithSlot] ?: return
            itemId = pkt.usedId
            nodeId = pkt.usedWithId
            type = IntType.ITEM
            player = pkt.player
        } else if (pkt is Packet.UseWithGroundItem) {
            val container = selectedContainer(pkt, pkt.player, pkt.iface) ?: return
            item = container[pkt.slot] ?: return
            itemId = pkt.usedId
            node = GroundItemManager.get(pkt.withId, Location.create(pkt.x, pkt.y, pkt.player.location.z), pkt.player)
                ?: return
            nodeId = pkt.withId
            type = IntType.GROUND_ITEM
            player = pkt.player
        } else {
            if (pkt !is Packet.UseWithPlayer) return
            val container = selectedContainer(pkt, pkt.player, pkt.iface) ?: return sendClearMinimap(pkt.player)
            item = container[pkt.slot] ?: return sendClearMinimap(pkt.player)
            itemId = pkt.itemId
            node = Repository.players[pkt.otherIndex] ?: return sendClearMinimap(pkt.player)
            type = IntType.PLAYER
            nodeId = node.id
            player = pkt.player
        }

        if (node !is Item && !node.isActive) return sendClearMinimap(player)
        if (item.id != itemId)
            return sendClearMinimap(player)
        if (node.id != nodeId)
            return sendClearMinimap(player)

        if (player.locks.isInteractionLocked() || player.locks.isMovementLocked() || player.locks.isComponentLocked())
            return sendClearMinimap(player)

        player.scripts.reset()
        if (player.zoneMonitor.useWith(item, node))
            return
        if (InteractionListeners.run(item, node, type, player))
            return
        if (childNode != null && childNode.id != node.id) {
            if (InteractionListeners.run(item, childNode, type, player))
                return
        }
        val flipped = type == IntType.ITEM && item.id < node.id
        val event = if (flipped)
            NodeUsageEvent(player, 0, node, item)
        else
            NodeUsageEvent(player, 0, item, childNode ?: node)
        if (PluginInteractionManager.handle(player, event))
            return
        UseWithHandler.run(event)
    }

    private fun processGroundItemAction(pkt: Packet.GroundItemAction) {
        val item = GroundItemManager.get(pkt.id, Location.create(pkt.x, pkt.y, pkt.player.location.z), pkt.player)
        val player = pkt.player
        player.scripts.reset()

        if (item == null) {
            return sendClearMinimap(player)
        }
        val option = item.interaction[pkt.optIndex]
        if (option == null) {
            InteractPlugin.handleInvalidInteraction(player, item, Option.NULL)
            return sendClearMinimap(player)
        }
        if (PluginInteractionManager.handle(player, item, option))
            return
        if (InteractionListeners.run(item.id, IntType.GROUND_ITEM, option.name, player, item))
            return
        if (InteractionListeners.run(item.id, IntType.ITEM, option.name, player, item))
            return
        item.interaction.handle(player, option)
    }

    private fun processPlayerAction(pkt: Packet.PlayerAction) {
        val player = pkt.player
        if (pkt.otherIndex !in 1 until ServerConstants.MAX_PLAYERS) {
            return sendClearMinimap(player)
        }
        player.scripts.reset()
        val other = Repository.players[pkt.otherIndex]
        if (other == null || !other.isActive)
            return sendClearMinimap(player)
        val option = other.interaction[pkt.optIndex] ?: return sendClearMinimap(player)
        if (!InteractionListeners.run(-1, IntType.PLAYER, option.name.lowercase(), player, other)) {
            other.interaction.handle(player, option)
        }
    }

    private fun processSceneryAction(pkt: Packet.SceneryAction) {
        val player = pkt.player
        var scenery = RegionManager.getObject(player.location.z, pkt.x, pkt.y, pkt.id)
        var objId = pkt.id

        //what follows is a series of hardcoded crimes against humanity
        if (pkt.id == 6898)
            scenery = Scenery(6898, Location(3219, 9618))
        if (pkt.id == 6899)
            scenery = Scenery(6899, Location(3221, 9618))

        // Random Event Maze chests are overridden by the walls, which needs to be hacked in.
        // if ((scenery?.id == 3626 || scenery?.id == 3635) && (objId in 3635..3636)) {
        //     scenery = MazeInterface.overrideScenery(scenery, objId)
        // }

        // Family crest levers don't have varps associated with them, so their state is validated with attributes
        // instead, and they always appear as their down/odd variant in the server's map
        if (objId in 2421..2426 && objId % 2 == 0) {
            scenery = Scenery(objId - 1, Location(pkt.x, pkt.y, player.location.z))
            objId -= 1
        }

        if (scenery == null || scenery.id != objId || !scenery.isActive) {
            player.debug("[SCENERY INTERACT] NULL OR MISMATCH OR INACTIVE")
            InteractPlugin.handleInvalidInteraction(player, scenery, Option.NULL)
            return sendClearMinimap(player)
        }

        val wrapperChild = scenery.getChild(player)
        val option = wrapperChild.interaction[pkt.optIndex]

        if (option == null) {
            player.debug("[SCENERY INTERACT] NULL OPTION")
            InteractPlugin.handleInvalidInteraction(player, scenery, Option.NULL)
            return sendClearMinimap(player)
        }

        val hasWrapper = wrapperChild.id != scenery.id

        player.debug("[SCENERY INTERACT]------------------------------")
        player.debug("ID: ${wrapperChild.id}, Option: ${option.name}[${option.index}]")
        player.debug("Loc: ${scenery.location}, Dir: ${scenery.direction}")
        if (hasWrapper) {
            player.debug("WrapperID: ${scenery.id}, ${scenery.definition.configFile?.let { "Varbit: ${it.id}" } ?: "Varp: ${scenery.definition.configId}"}")
        }
        player.debug("------------------------------------------------")

        player.scripts.reset()
        if (InteractionListeners.run(wrapperChild.id, IntType.SCENERY, option.name, player, wrapperChild))
            return
        if (PluginInteractionManager.handle(player, wrapperChild))
            return
        wrapperChild.interaction.handle(player, option)
    }

    private fun processNpcAction(pkt: Packet.NpcAction) {
        if (pkt.npcIndex !in 1 until ServerConstants.MAX_NPCS)
            return sendClearMinimap(pkt.player)
        val npc = Repository.npcs[pkt.npcIndex] ?: return sendClearMinimap(pkt.player)

        val wrapperChild = npc.getShownNPC(pkt.player)
        val option = wrapperChild.interaction[pkt.optIndex]

        if (option == null) {
            InteractPlugin.handleInvalidInteraction(pkt.player, npc, Option.NULL)
            return sendClearMinimap(pkt.player)
        }

        val hasWrapper = wrapperChild.id != npc.id

        pkt.player.debug("[NPC INTERACT]-------------------")
        pkt.player.debug("ID: ${wrapperChild.id}, Index: ${pkt.npcIndex}")
        pkt.player.debug("Option: ${option.name}[${option.index}]")
        pkt.player.debug("SpawnLoc: ${npc.properties.spawnLocation}")
        if (hasWrapper) {
            pkt.player.debug("WrapperID: ${npc.id}, Varbit: ${npc.definition.configFileId}")
        }
        pkt.player.debug("---------------------------------")

        pkt.player.scripts.reset()
        if (InteractionListeners.run(wrapperChild.id, IntType.NPC, option.name, pkt.player, npc))
            return
        if (PluginInteractionManager.handle(pkt.player, wrapperChild, option))
            return
        npc.interaction.handle(pkt.player, option)
    }

    private fun processItemAction(pkt: Packet.ItemAction) {
        val container = getLikelyContainerForIface(pkt.player, pkt.iface) ?: return
        if (pkt.slot !in 0 until container.capacity())
            return
        val item = container.get(pkt.slot) ?: return
        if (item.id != pkt.itemId)
            return
        val option = item.interaction[pkt.optIndex] ?: return
        if (pkt.player.locks.isInteractionLocked())
            return
        item.interaction.handleItemOption(pkt.player, option, container)
        pkt.player.scripts.reset()
        pkt.player.debug("[ITEM INTERACT] ID: ${item.id}, Slot: ${pkt.slot}, Opt: ${option.name}")
    }

    private fun getLikelyContainerForIface(player: Player, iface: Int): Container? {
        return when (iface) {
            Components.INVENTORY_149 -> player.inventory
            Components.BANK_V2_MAIN_762 -> player.bank
            Components.EQUIP_SCREEN2_667 -> player.equipment
            else -> null
        }
    }

    private fun sendClearMinimap(player: Player) {
        PacketRepository.send(Encoders578.CLEAR_MINIMAP_FLAG, PlayerContext(player))
    }

    fun switchItem(slot: Int, secondSlot: Int, container: Container?, insert: Boolean, player: Player) {
        if (container == null || slot < 0 || slot >= container.toArray().size || secondSlot < 0 || secondSlot >= container.toArray().size) {
            return
        }
        if (player.interfaceManager.hasChatbox() && !getAttribute(player, "close_c_", false)) {
            player.interfaceManager.closeChatbox()
            if (player.interfaceManager.hasChatbox()) return
        }
        val item = container[slot]
        val second = container[secondSlot]
        if (item == null) {
            return
        }
        if (!insert) {
            container.replace(second, slot, false)
            container.replace(item, secondSlot, false)
            item.index = secondSlot
            if (second != null) {
                second.index = slot
            }
        } else {
            container.insert(slot, secondSlot, false)
        }
        container.refresh()
    }

    fun splitChatMessage(message: String, clanLength: Int, rankPresent: Boolean): ArrayList<String> {
        val messages = ArrayList<String>()

        val effectiveCutoff = BASE_CHAT_CUTOFF - (clanLength + if (rankPresent) 9 else 0)
        var counter = 0
        for (token in message.split(" ")) {
            if (counter + token.length > effectiveCutoff)
                break
            counter += token.length + 1
        }
        messages.add(message.substring(0, min(counter, message.length)))
        if (counter < message.length)
            messages.add(message.substring(counter, message.length))

        return messages
    }

    const val BASE_CHAT_CUTOFF = 81
}
