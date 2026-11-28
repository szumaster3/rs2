package core.game.node.entity.player.link

import content.data.Quests

import content.data.GameAttributes
import content.region.other.tutorial_island.plugin.TutorialStage
import core.api.isQuestComplete
import core.api.log
import core.game.component.CloseEvent
import core.game.component.Component
import core.game.component.InterfaceType
import core.game.event.InterfaceCloseEvent
import core.game.event.InterfaceOpenEvent
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.world.GameWorld.settings
import core.net.IoSession
import core.net.packet.PacketRepository
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.InterfaceContext
import core.net.packet.out.PacketContext.WindowsPaneContext
import core.tools.Log
import shared.consts.Components

/**
 * Manages interface components for a player.
 *
 * @property player The player associated with this interface manager.
 */
class InterfaceManager(val player: Player) {
    private var packetCount = 0
    private var temporaryRoot: Int? = null
    private var attachmentSession: IoSession? = null
    private var logoutButtonHidden = false

    /**
     * Display a full-canvas modal without discarding the underlying gameframe.
     */
    fun openTemporaryRoot(component: Component): Component? {
        prepareAttachmentSession()
        if (!close()) return null
        temporaryRoot = component.id
        opened = component
        PacketRepository.send(Encoders578.IF_OPENTOP, WindowsPaneContext(player, component.id, 0))
        player.dispatch(InterfaceOpenEvent(component))
        return component
    }

    /**
     * The root windows pane interface currently open.
     */
    var windowsPane: Component? = null
        private set

    /**
     * The currently opened component, if any.
     */
    var opened: Component? = null

    /**
     * The set of tab interfaces.
     */
    @JvmField
    var tabs: Array<Component?> = arrayOfNulls(15)

    /**
     * The chatbox interface component.
     */
    @JvmField
    var chatbox: Component? = null


    /**
     * The single tab interface component.
     */
    var singleTab: Component? = null
        private set

    /**
     * The overlay interface component.
     */
    @JvmField
    var overlay: Component? = null

    private val wildyOverlay: Component? = null

    /**
     * The index of the current active tab.
     */
    @JvmField
    var currentTabIndex: Int = 3

    /**
     * Opens the specified windows pane interface.
     *
     * @param windowsPane The component to be set as the window pane.
     * @param overlap Whether to use overlap mode (default is false).
     * @return The component that was opened as the window pane.
     */
    @JvmOverloads
    fun openWindowsPane(windowsPane: Component, overlap: Boolean = false): Component {
        prepareAttachmentSession()
        this.windowsPane = windowsPane
        if (windowsPane.definition!!.type != InterfaceType.WINDOW_PANE) {
            log(
                this.javaClass,
                Log.WARN,
                "Set interface type to WINDOW_PANE for component " + windowsPane.id + ", definition requires updating!"
            )
            windowsPane.definition.type = InterfaceType.WINDOW_PANE
        }

        PacketRepository.send(
            Encoders578.IF_OPENTOP,
            WindowsPaneContext(player, windowsPane.id, if (overlap) 1 else 0)
        )
        windowsPane.open(player)

        if (opened != null) player.dispatch(InterfaceOpenEvent(opened!!))

        return windowsPane
    }

    /**
     * Opens the specified windows pane with a custom display type.
     *
     * @param windowsPane The component to be opened.
     * @param type The display type (e.g., resize mode or fixed).
     */
    fun openWindowsPane(windowsPane: Component, type: Int) {
        prepareAttachmentSession()
        this.windowsPane = windowsPane
        if (windowsPane.definition!!.type != InterfaceType.WINDOW_PANE) {
            log(
                this.javaClass,
                Log.WARN,
                "Set interface type to WINDOW_PANE for component " + windowsPane.id + ", definition requires updating!"
            )
            windowsPane.definition.type = InterfaceType.WINDOW_PANE
        }

        PacketRepository.send(Encoders578.IF_OPENTOP, WindowsPaneContext(player, windowsPane.id, type))
        windowsPane.open(player)

        if (opened != null) player.dispatch(InterfaceOpenEvent(opened!!))
    }

    /**
     * Opens a component using its ID.
     *
     * @param componentId The ID of the component to open.
     * @return The opened component or null if the open failed.
     */
    fun openComponent(componentId: Int): Component? {
        return open(Component(componentId))
    }

    /**
     * Opens the given component, closing any existing one.
     *
     * @param component The component to open.
     * @return The opened component, or null if the previous one couldn't be closed.
     */
    fun open(component: Component): Component? {
        if (!close()) {
            return null
        }
        // These two interfaces use script709 via their own viewport sizing hooks.
        if (component.id == 775 || component.id == 795) component.definition!!.type = InterfaceType.VIEWPORT
        component.open(player)

        opened = component
        player.dispatch(InterfaceOpenEvent(opened!!))

        return opened
    }

    /**
     * Checks if any component is currently opened.
     *
     * @return True if a component is open, false otherwise.
     */
    fun isOpened(): Boolean {
        return opened != null
    }

    /**
     * Checks if a valid chatbox component is active.
     *
     * @return True if chatbox is non-null and not the default, false otherwise.
     */
    fun hasChatbox(): Boolean {
        return chatbox != null && chatbox!!.id != DEFAULT_CHATBOX
    }

    /**
     * Closes the currently opened component and overlay if applicable.
     *
     * @return True if successfully closed or nothing was open, false if closure failed.
     */
    fun close(): Boolean {
        val wasOpened = opened

        // Component 333 is an immediate(no-fading) full-screen HD-mode black screen which auto-clears when interrupted.
        if (overlay != null && overlay!!.id == Components.BLACK_OVERLAY_333) {
            closeOverlay()
        }

        if (opened != null && opened!!.close(player)) {
            if (temporaryRoot == opened!!.id) {
                temporaryRoot = null
                val oldRoot = windowPaneId
                val newRoot = Gameframe.root(player.session?.clientInfo?.isResizable == true)
                // WorldMap switches the client renderer. IF_OPENTOP type 2 invokes
                // the native world-map cleanup and restores the previous renderer.
                val restoreType = if (opened!!.id == Components.WORLDMAP_755) 2 else 0
                relocateGameframe(oldRoot, newRoot, restoreType)
                player.dispatch(InterfaceCloseEvent(opened!!))
            } else {
                closeAttachment(opened!!)
                player.dispatch(InterfaceCloseEvent(opened!!))
            }
            opened = null
        }

        if (wasOpened != null && opened == null) {
            val tutorialComplete = player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false)
            val tutorialStage = player.getAttribute(GameAttributes.TUTORIAL_STAGE, -1)
            if (!tutorialComplete && tutorialStage < 72) {
                if (!player.isArtificial && !(tutorialStage == 0 && wasOpened?.id == 900 && player.getAttribute("char-design:accepted", false))) {
                    TutorialStage.load(player, tutorialStage, false)
                }
            } else {
                if (player.getAttribute<Any?>("runscript", null) != null) {
                    player.removeAttribute("runscript")
                    player.removeAttribute("input-type")
                    player.removeAttribute("parseamount")
                    player.packetDispatch.sendRunScript(101, "")
                }
            }
        }

        return wasOpened == null || opened == null
    }

    /**
     * Indicates whether the currently opened interface is walkable.
     *
     * @return True if no component is open or the current one is walkable, false otherwise.
     */
    val isWalkable: Boolean
        get() {
            if (opened != null) {
                if (opened!!.id == Components.EXCHANGE_SEARCH_389) {
                    return false
                }
                if (opened!!.definition!!.isWalkable) {
                    return true
                }
            }
            return true
        }

    /**
     * Closes the specified component.
     *
     * @param component The component to close.
     * @return True if successfully closed, false otherwise.
     */
    fun close(component: Component): Boolean {
        if (component.close(player)) {
            if (component.id == DEFAULT_CHATBOX) {
                return true
            }
            closeAttachment(component)
            return true
        }
        return false
    }

    /**
     * Closes the current chatbox and reopens the default one.
     */
    fun closeChatbox() {
        if (chatbox != null && chatbox!!.id != DEFAULT_CHATBOX) {
            if (close(chatbox!!)) {
                openChatbox(DEFAULT_CHATBOX)
                player.packetDispatch.sendRunScript(101, "")
            }
        }
    }

    /**
     * Opens a single-tab component.
     *
     * @param component The component to open.
     * @return The opened component.
     */
    fun openSingleTab(component: Component): Component {
        if (component.definition!!.type != InterfaceType.SINGLE_TAB) {
            log(
                this.javaClass,
                Log.WARN,
                "Set interface type to SINGLE_TAB for component " + component.id + ", definition requires updating!"
            )
            component.definition.type = InterfaceType.SINGLE_TAB
        }
        component.open(player)
        if (component.closeEvent == null) {
            component.closeEvent = CloseEvent { player: Player?, c: Component? -> true }
        }
        return component.also { singleTab = it }
    }

    /**
     * Closes the currently opened single-tab component.
     *
     * @return Always returns true.
     */
    fun closeSingleTab(): Boolean {
        if (singleTab != null && close(singleTab!!)) {
            singleTab = null
        }
        return true
    }

    /**
     * Removes the specified tabs.
     *
     * @param tabs The indices of the tabs to remove.
     */
    fun removeTutorialTabs() {
        removeTabs(*(0..13).toList().toIntArray())
        for (index in intArrayOf(3, 15)) {
            PacketRepository.send(Encoders578.IF_CLOSESUB, InterfaceContext(player, windowPaneId,
                (if (isResizable) 33 else 152) + index, 0, true))
        }
    }

    fun removeTabs(vararg tabs: Int) {
        var changeViewedTab = false
        for (slot in tabs) {
            if (slot == currentTabIndex) {
                changeViewedTab = true
            }
            val tab = this.tabs[slot]
            if (tab != null) {
                close(tab)
                this.tabs[slot] = null
            }
        }
        if (changeViewedTab) {
            var currentIndex = -1
            if (this.tabs[3] == null) {
                for (i in this.tabs.indices) {
                    if (this.tabs[i] != null) {
                        currentIndex = i
                        break
                    }
                }
            } else {
                currentIndex = 3
            }
            if (currentIndex > -1) {
                setViewedTab(currentIndex)
            }
        }
    }

    /**
     * Restores all default tabs if missing or hidden.
     */
    fun restoreTabs() {
        openExtraTab(3, 259)
        openExtraTab(15, 34)
        for (i in tabs.indices) {
            val tab = tabs[i]
            if (tab == null) {
                when (i) {
                    0 -> {
                        var inter = player.getExtension<WeaponInterface>(WeaponInterface::class.java)
                        if (inter == null) {
                            player.addExtension(
                                WeaponInterface::class.java,
                                WeaponInterface(player).also { inter = it })
                        }
                        openTab(0, inter)
                    }

                    6 -> openTab(6, Component(player.spellBookManager.spellBook)) // Magic
                    7 -> openTab(7, Component(891))

                    else -> openTab(i, Component(DEFAULT_TABS[i]))
                }
            } else if (tab.isHidden) {
                openTab(i, tab)
                tab.isHidden = false
            }
        }
    }

    /**
     * Opens the default tab components for the player.
     */
    fun openDefaultTabs() {
        var inter = player.getExtension<WeaponInterface>(WeaponInterface::class.java)
        if (inter == null) {
            player.addExtension(WeaponInterface::class.java, WeaponInterface(player).also { inter = it })
        }
        openTab(0, inter)
        openTab(1, Component(Components.STATS_320))
        openTab(2, Component(190))
        openTab(3, Component(Components.INVENTORY_149))
        openTab(4, Component(Components.WORNITEMS_387))
        openTab(5, Component(Components.PRAYER_271))
        openTab(6, Component(player.spellBookManager.spellBook))
        openTab(7, Component(891))
        openExtraTab(3, 259)
        openExtraTab(15, 34)
        openTab(8, Component(Components.FRIENDS2_550))
        openTab(9, Component(Components.IGNORE2_551))
        openTab(10, Component(Components.CLANJOIN_589))
        openTab(11, Component(Components.OPTIONS_261))
        openTab(12, Component(Components.EMOTES_464))
        openTab(13, Component(Components.MUSIC_V3_187))
        openTab(14, Component(Components.LOGOUT_182))
        core.api.sendIfaceSettings(player, 2, 6, Components.LOGOUT_182, -1, -1)
        if (player.properties.autocastSpell != null) {
            AutocastManager.restore(player)
        }
    }

    /**
     * Opens status orbs and top bar interfaces.
     */
    fun openInfoBars() {
        //Hp orb
        PacketRepository.send(
            Encoders578.IF_OPENSUB,
            InterfaceContext(player, windowPaneId, if (isResizable) 169 else 134, Components.TOPSTAT_HITPOINTS_748, true)
        )
        //Prayer orb
        PacketRepository.send(
            Encoders578.IF_OPENSUB,
            InterfaceContext(player, windowPaneId, if (isResizable) 170 else 136, Components.TOPSTAT_PRAYER_749, true)
        )
        //Energy orb
        PacketRepository.send(
            Encoders578.IF_OPENSUB,
            InterfaceContext(player, windowPaneId, if (isResizable) 171 else 137, Components.TOPSTAT_RUN_750, true)
        )
        //Summoning bar
        if (isQuestComplete(player, Quests.WOLF_WHISTLE.questName) && settings?.isMembers == true) {
            PacketRepository.send(
                Encoders578.IF_OPENSUB,
                InterfaceContext(player, windowPaneId, if (isResizable) 172 else 139, Components.TOPSTAT_LORE_747, true)
            )
        }
        setMultiwayIndicator(player.properties.isMultiZone)
        // Split PM parent is not verified for revision 578.
    }

    /**
     * Closes all default tab components.
     */
    fun closeDefaultTabs() {
        // Keep logical tab state consistent with the actual client attachments;
        // restoreTabs() must be able to reopen tabs after trade and random events.
        for (slot in 0..13) {
            val tab = tabs[slot] ?: continue
            if (close(tab)) tabs[slot] = null
        }
        for (index in intArrayOf(3, 15)) {
            val child = Gameframe.tabChild(windowPaneId, index)
            val attachment = attachments[attachmentKey(windowPaneId, child)] ?: continue
            PacketRepository.send(Encoders578.IF_CLOSESUB, InterfaceContext(player, windowPaneId,
                child, attachment.id, attachment.walkable))
        }
        // Logout remains available unless the caller explicitly hides it.
    }

    /**
     * Opens a tab component in a given slot.
     *
     * @param slot The tab index.
     * @param component The component to open.
     */
    fun openTab(slot: Int, component: Component?) {
        check(!(component!!.id == 884 && component !is WeaponInterface)) { "Attack tab can only be instanced as " + WeaponInterface::class.java.canonicalName + "!" }
        if (component.definition!!.tabIndex != slot) {
            log(this.javaClass, Log.WARN, "Set tab index to " + slot + " for component " + component.id + ", definition requires updating!")
            component.definition.tabIndex = slot
        }
        if (component.definition.type != InterfaceType.TAB) {
            log(this.javaClass, Log.WARN, "Set interface type to TAB for component " + component.id + ", definition requires updating!")
            component.definition.type = InterfaceType.TAB
        }
        component.open(player)
        tabs[slot] = component
        if (component.id == 190) core.api.sendIfaceSettings(player, 2, 18, 190, 0, 166)
        if (component.id == 149) {
            val mask = core.api.IfaceSettingsBuilder().enableOptions(0, 1, 2, 6, 7, 9)
                .setUseOnSettings(true, true, true, true, false, true)
                .enableSlotSwitch().enableNullSlotSwitch().build()
            core.api.sendIfaceSettings(player, mask, 0, 149, 0, 27)
        }
        val button = Gameframe.tabButton(windowPaneId, if (slot == 14) 99 else protocolTabIndex(slot))
        if (button != null) core.api.sendIfaceSettings(player, 2, button, windowPaneId, -1, -1)
    }

    /**
     * Opens a tab using the component's defined tab index.
     *
     * @param component The component to open.
     */
    fun openTab(component: Component) {
        if (component.definition!!.tabIndex < 0) {
            log(this.javaClass, Log.WARN, "No component definitions found for tab " + component.id + "!")
            return
        }
        openTab(component.definition.tabIndex, component)
    }

    /**
     * Opens a chatbox interface by id.
     *
     * @param componentId The ID of the component to open.
     */
    fun openChatbox(componentId: Int) {
        openChatbox(Component(componentId))
    }

    /**
     * Opens a chatbox interface.
     *
     * @param component The component to open.
     */
    fun openChatbox(component: Component) {
        if (component.id == DEFAULT_CHATBOX) {
            openDefaultChatFrame()
            chatbox = component
        } else {
            val previous = chatbox
            if (previous != null && previous.id != DEFAULT_CHATBOX && previous.id != component.id) {
                closeAttachment(previous)
            }
            chatbox = component
            if (chatbox!!.definition!!.type != InterfaceType.DIALOGUE && chatbox!!.definition!!.type != InterfaceType.CHATBOX && chatbox!!.definition!!.type != InterfaceType.CS_CHATBOX) {
                log(this.javaClass, Log.WARN, "Set interface type to CHATBOX for component " + component.id + ", definition requires updating!")
                chatbox!!.definition!!.type = InterfaceType.DIALOGUE
            }
            if (component.id == 372 || component.id == 421) component.definition!!.type = InterfaceType.FULL_CHATBOX
            chatbox!!.open(player)
        }
    }

    // Track queued server subinterfaces, including direct packet callers. A reconnect
    // creates a different client interface tree even when the Player object is reused.
    private data class Attachment(val id: Int, val walkable: Boolean)
    private val attachments = linkedMapOf<Int, Attachment>()

    private fun prepareAttachmentSession() {
        val session = player.session
        if (attachmentSession !== session) {
            attachments.clear()
            attachmentSession = session
            temporaryRoot = null
            logoutButtonHidden = false
        }
    }

    private fun attachmentKey(parent: Int, child: Int): Int = (parent shl 16) or child

    fun recordInterfaceAttachment(parent: Int, child: Int, id: Int, walkable: Boolean) {
        prepareAttachmentSession()
        val key = attachmentKey(parent, child)
        if (attachments.containsKey(key)) removeInterfaceAttachment(parent, child)
        attachments[key] = Attachment(id, walkable)
    }

    fun removeInterfaceAttachment(parent: Int, child: Int) {
        prepareAttachmentSession()
        removeAttachment(attachmentKey(parent, child))
    }

    private fun removeAttachment(key: Int) {
        val old = attachments.remove(key) ?: return
        // Static276.method4655 closes nested type-3 subinterfaces of the removed group.
        attachments.filter { (hash, value) -> hash ushr 16 == old.id && value.walkable }
            .keys.toList().forEach { removeAttachment(it) }
    }

    fun recordInterfaceMove(source: Int, destination: Int) {
        prepareAttachmentSession()
        if (source == destination) return
        val moving = attachments[source]
        removeAttachment(destination)
        if (moving != null) {
            attachments.remove(source)
            attachments[destination] = moving
        }
    }

    private fun isAttached(parent: Int, child: Int, id: Int): Boolean =
        attachments[attachmentKey(parent, child)]?.id == id

    private fun closeAttachment(component: Component) {
        prepareAttachmentSession()
        val definition = component.definition ?: return
        val parent = definition.getWindowPaneId(isResizable)
        val child = definition.getChildId(isResizable) +
            if (definition.type == InterfaceType.TAB) protocolTabIndex(definition.tabIndex) else 0
        val preferred = attachmentKey(parent, child)
        // Close the actual parent, not a stale definition or another interface that
        // has since replaced this one. This also handles an unmapped parked attachment.
        val key = if (attachments[preferred]?.id == component.id) preferred else
            attachments.entries.firstOrNull { it.value.id == component.id }?.key ?: return
        val attachment = attachments[key] ?: return
        PacketRepository.send(Encoders578.IF_CLOSESUB, InterfaceContext(player,
            key ushr 16, key and 65535, attachment.id, attachment.walkable))
    }

    /** Apply client WINDOW_STATUS to the gameframe when a game root is active. */
    fun switchWindowMode(windowMode: Int) {
        if (windowMode !in 1..3) return
        val info = player.session.clientInfo ?: return
        info.windowMode = windowMode
        if (player.getAttribute("logging_in", false) || !player.isPlaying || temporaryRoot != null) return
        val oldRoot = windowPaneId
        val newRoot = Gameframe.root(info.isResizable)
        // Resizable and fullscreen use the same root; changes in canvas size are
        // handled by the client's onresize hook, not by reopening the gameframe.
        if (oldRoot != newRoot && Gameframe.isGameframe(oldRoot)) relocateGameframe(oldRoot, newRoot)
    }

    private fun relocateGameframe(oldRoot: Int, newRoot: Int, restoreType: Int = 0) {
        prepareAttachmentSession()
        val moves = if (oldRoot == newRoot) emptyList() else attachments.keys
            .filter { it ushr 16 == oldRoot }.map { source ->
                source to Gameframe.destination(oldRoot, source and 65535)
            }
        windowsPane = Component(newRoot).also { it.definition!!.type = InterfaceType.WINDOW_PANE }
        // IF_OPENTOP loads the destination group; IF_MOVESUB keeps the existing
        // SubInterface and nested children instead of invoking server open/close hooks.
        PacketRepository.send(Encoders578.IF_OPENTOP, WindowsPaneContext(player, newRoot, restoreType))
        for ((source, child) in moves) {
            if (child == null) {
                log(javaClass, Log.WARN, "Unmapped window attachment ${source ushr 16}:${source and 65535}")
                continue
            }
            PacketRepository.send(Encoders578.IF_MOVESUB, core.net.packet.out.PacketContext.MoveInterfaceContext(
                player, source, attachmentKey(newRoot, child)))
        }
        // Include extra tabs (diaries and notes), not just the historical 15 server slots.
        for (index in (0..15).toList() + 99) {
            if (!attachments.containsKey(attachmentKey(newRoot, Gameframe.tabChild(newRoot, index)))) continue
            val button = Gameframe.tabButton(newRoot, index) ?: continue
            core.api.sendIfaceSettings(player, 2, button, newRoot, -1, -1)
        }
        // subchanged/script71 restores the client-selected Varc 168, including collapsed tabs.
        if (logoutButtonHidden) applyLogoutButtonVisibility()
        player.packetDispatch.sendRunEnergy()
    }

    fun setLogoutButtonHidden(hidden: Boolean) {
        logoutButtonHidden = hidden
        applyLogoutButtonVisibility()
    }

    private fun applyLogoutButtonVisibility() {
        if (!Gameframe.isGameframe(windowPaneId)) return
        val button = Gameframe.tabButton(windowPaneId, 99) ?: return
        player.packetDispatch.sendInterfaceConfig(windowPaneId, button, logoutButtonHidden)
    }

    fun setMultiwayIndicator(active: Boolean) {
        if (!Gameframe.isGameframe(windowPaneId)) return
        val child = Gameframe.statusIcons(windowPaneId)
        if (!isAttached(windowPaneId, child, 745)) {
            PacketRepository.send(Encoders578.IF_OPENSUB, InterfaceContext(player, windowPaneId, child, 745, true))
        }
        // CS2 script1951 reads Varc 616 on load and on varc transmit.
        player.packetDispatch.sendVarcUpdate(616, if (active) 1 else 0)
    }

    /**
     * Gets a currently active component by its id.
     *
     * @param componentId The id of the component.
     * @return The matching component, or `null` if not found.
     */
    fun getComponent(componentId: Int): Component? {
        if (opened != null && opened!!.id == componentId) {
            return opened
        }
        if (chatbox != null && chatbox!!.id == componentId) {
            return chatbox
        }
        if (singleTab != null && singleTab!!.id == componentId) {
            return singleTab
        }
        if (overlay != null && overlay!!.id == componentId) {
            return overlay
        }
        if ((temporaryRoot ?: windowsPane?.id) == componentId && windowsPane?.id == componentId) {
            return windowsPane
        }
        for (c in tabs) {
            if (c != null && c.id == componentId) {
                return c
            }
        }
        if (componentId == 259 && isAttached(windowPaneId, Gameframe.tabChild(windowPaneId, 3), 259)) {
            return Component(259)
        }
        if (componentId == Components.FILTERBUTTONS_751 || componentId == Components.TOPSTAT_RUN_750 || componentId == Components.TOPSTAT_LORE_747) {
            //Chatbox settings, run orb & summoning orb.
            return Component(componentId)
        }
        return null
    }

    /**
     * Sets the currently viewed tab.
     *
     * @param tabIndex The tab index to view.
     */
    fun setViewedTab(tabIndex: Int) {
        if (tabIndex !in tabs.indices || tabs[tabIndex] == null) return
        currentTabIndex = tabIndex
        player.packetDispatch.sendRunScript(115, "i", if (tabIndex == 14) 99 else protocolTabIndex(tabIndex))
    }

    /**
     * Checks if the main (opened) component matches the given ID.
     *
     * @param id The component ID to check.
     * @return True if the currently opened component matches the given ID.
     */
    fun hasMainComponent(id: Int): Boolean {
        return opened != null && opened!!.id == id
    }

    /**
     * Opens an overlay interface.
     *
     * @param component The overlay component to open.
     */
    fun openOverlay(component: Component) {
        if (component.id == Components.TUTORIAL_PROGRESS_371 &&
            (temporaryRoot == 900 || (!player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false) &&
                player.getAttribute(GameAttributes.TUTORIAL_STAGE, 0) == 0))) return
        if (overlay != null && !overlay!!.close(player)) {
            return
        }
        overlay = component
        if (overlay!!.definition!!.type != InterfaceType.OVERLAY && overlay!!.definition!!.type != InterfaceType.OVERLAY_B) {
            log(
                this.javaClass,
                Log.WARN,
                "Set interface type to OVERLAY for component " + component.id + ", definition requires updating!"
            )
            overlay!!.definition!!.type = InterfaceType.OVERLAY
            overlay!!.definition!!.isWalkable = true
        }
        overlay!!.open(player)
    }

    /**
     * Closes the currently opened overlay interface.
     */
    fun closeOverlay() {
        if (overlay != null && close(overlay!!)) {
            overlay = null
        }
    }

    /**
     * Gets the player's current weapon tab interface.
     */
    val weaponTab: WeaponInterface
        get() = player.getExtension(WeaponInterface::class.java)

    /**
     * Gets the current window pane id.
     *
     * @return The id of the current window pane, or the default if not set.
     */
    val windowPaneId: Int
        get() {
            if (windowsPane == null) {
                return Gameframe.root(player.session?.clientInfo?.isResizable == true)
            }
            return windowsPane!!.id
        }


    /**
     * Gets the default child id used for interface placement.
     *
     * @return 8 if resizable, otherwise 16.
     */
    val defaultChildId: Int
        get() = InterfaceType.DEFAULT.let { if (isResizable) it.resizableChildId else it.fixedChildId }

    /**
     * Checks if the client is in resizable mode.
     *
     * @return `True` if resizable mode is active, `false` otherwise.
     */
    val isResizable: Boolean
        get() {
            val info = player.session?.clientInfo ?: return false
            // Use the attached gameframe until a deferred temporary-root switch is applied.
            val root = windowsPane?.id
            return if (root != null && Gameframe.isGameframe(root)) root == Gameframe.LARGE
                else info.isResizable
        }

    /**
     * Increments and returns the current packet count.
     *
     * @param increment The amount to increment by.
     * @return The packet count before incrementing.
     */
    fun getPacketCount(increment: Int): Int {
        val count = packetCount
        packetCount += increment
        return count
    }

    fun openDiaryTab() {
        openExtraTab(3, 259)
    }

    private fun openExtraTab(index: Int, id: Int) {
        PacketRepository.send(Encoders578.IF_OPENSUB, InterfaceContext(player, windowPaneId,
            (if (isResizable) 33 else 152) + index, id, true))
        Gameframe.tabButton(windowPaneId, index)?.let {
            core.api.sendIfaceSettings(player, 2, it, windowPaneId, -1, -1)
        }
        if (id == 259) {
            for (child in 2..34) {
                if (child in 6..9) continue
                core.api.sendIfaceSettings(player, 2, child, 259, -1, -1)
            }
            player.achievementDiaryManager.diaries.forEach { it.drawStatus(player) }
        }
    }

    private fun openDefaultChatFrame() {
        prepareAttachmentSession()
        val filtersChild = if (isResizable) 15 else 20
        val chatChild = if (isResizable) 18 else 142
        if (!isAttached(windowPaneId, filtersChild, 751))
            PacketRepository.send(Encoders578.IF_OPENSUB, InterfaceContext(player, windowPaneId, filtersChild, 751, true))
        if (!isAttached(windowPaneId, chatChild, 752))
            PacketRepository.send(Encoders578.IF_OPENSUB, InterfaceContext(player, windowPaneId, chatChild, 752, true))
        if (!isAttached(752, 9, 137))
            PacketRepository.send(Encoders578.IF_OPENSUB, InterfaceContext(player, 752, 9, 137, true))
    }

    companion object {
        // Preserve logical server slots used by existing content.
        @JvmStatic
        fun protocolTabIndex(slot: Int): Int = when (slot) {
            in 0..2 -> slot
            in 3..13 -> slot + 1
            14 -> 18
            else -> slot
        }

        /**
         * The default windows pane component id.
         */
        val WINDOWS_PANE: Int = Components.TOPLEVEL_548

        /**
         * The default chatbox component id.
         */
        @JvmField
        val DEFAULT_CHATBOX: Int = Components.CHATDEFAULT_137

        /**
         * Default tab component ids.
         */
        val DEFAULT_TABS: IntArray = intArrayOf(
            884,
            Components.STATS_320,
            190,
            Components.INVENTORY_149,
            Components.WORNITEMS_387,
            Components.PRAYER_271,
            Components.MAGIC_192,
            891,
            Components.FRIENDS2_550,
            Components.IGNORE2_551,
            Components.CLANJOIN_589,
            Components.OPTIONS_261,
            Components.EMOTES_464,
            Components.MUSIC_V3_187,
            Components.LOGOUT_182
        )
    }
}
