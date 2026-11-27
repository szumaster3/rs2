package core.game.interaction

import core.game.component.Component
import core.game.node.entity.player.Player

object InterfaceListeners {
    val buttonListeners = HashMap<String, (Player, Component, Int, Int, Int, Int) -> Boolean>(1000)
    private val nativeOperationListeners = HashSet<String>()
    val openListeners = HashMap<String, (Player, Component) -> Boolean>(100)
    val slotSwitchListeners = HashMap<Int, (Player, Component, Int, Int) -> Boolean>()

    @JvmStatic
    fun add(
        componentID: Int,
        buttonID: Int,
        handler: (Player, Component, Int, Int, Int, Int) -> Boolean,
    ) {
        val key = "$componentID:$buttonID"
        nativeOperationListeners.remove(key)
        buttonListeners[key] = handler
    }

    @JvmStatic
    fun add(
        componentID: Int,
        handler: (Player, Component, Int, Int, Int, Int) -> Boolean,
    ) {
        val key = "$componentID"
        nativeOperationListeners.remove(key)
        buttonListeners[key] = handler
    }

    @JvmStatic
    fun addNative(componentID: Int, buttonID: Int, handler: (Player, Component, Int, Int, Int, Int) -> Boolean) {
        add(componentID, buttonID, handler)
        nativeOperationListeners.add("$componentID:$buttonID")
    }

    @JvmStatic
    fun addNative(componentID: Int, handler: (Player, Component, Int, Int, Int, Int) -> Boolean) {
        add(componentID, handler)
        nativeOperationListeners.add("$componentID")
    }

    /** Preserve exact-button precedence and legacy plugin fallback while allowing native widget operation indices. */
    @JvmStatic
    fun runNative(player: Player, component: Component, operation: Int, buttonID: Int, slot: Int, itemID: Int): Boolean {
        require(operation in InterfaceOperation.OP1..InterfaceOperation.CONTINUE_INDEX)
        val exact = "${component.id}:$buttonID"
        val key = if (buttonListeners.containsKey(exact)) exact else "${component.id}"
        val handler = buttonListeners[key] ?: return false
        val listenerOperation = if (key in nativeOperationListeners) operation
            else if (operation == InterfaceOperation.CONTINUE_INDEX) InterfaceOperation.CONTINUE
            else InterfaceOperation.listenerId(operation)
        return handler.invoke(player, component, listenerOperation, buttonID, slot, itemID)
    }

    @JvmStatic
    fun addOpenListener(
        componentID: Int,
        handler: (Player, Component) -> Boolean,
    ) {
        openListeners["$componentID"] = handler
    }

    @JvmStatic
    fun addCloseListener(
        componentID: Int,
        handler: (Player, Component) -> Boolean,
    ) {
        openListeners["close:$componentID"] = handler
    }

    @JvmStatic
    fun get(
        componentID: Int,
        buttonID: Int,
    ): ((Player, Component, Int, Int, Int, Int) -> Boolean)? = buttonListeners["$componentID:$buttonID"]

    @JvmStatic
    fun get(componentID: Int): ((Player, Component, Int, Int, Int, Int) -> Boolean)? = buttonListeners["$componentID"]

    @JvmStatic
    fun getOpenListener(componentID: Int): ((Player, Component) -> Boolean)? = openListeners["$componentID"]

    @JvmStatic
    fun getCloseListener(componentID: Int): ((Player, Component) -> Boolean)? = openListeners["close:$componentID"]

    @JvmStatic
    fun onSlotSwitch(
        componentID: Int,
        handler: (Player, Component, Int, Int) -> Boolean,
    ) {
        slotSwitchListeners[componentID] = handler
    }

    @JvmStatic
    fun runSlotSwitch(
        player: Player,
        component: Component,
        sourceSlot: Int,
        destSlot: Int,
    ): Boolean {
        val method = slotSwitchListeners[component.id] ?: return false
        return method.invoke(player, component, sourceSlot, destSlot)
    }

    @JvmStatic
    fun runOpen(
        player: Player,
        component: Component,
    ): Boolean {
        val method = getOpenListener(component.id) ?: return false
        return method.invoke(player, component)
    }

    @JvmStatic
    fun runClose(
        player: Player,
        component: Component,
    ): Boolean {
        val method = getCloseListener(component.id) ?: return true
        return method.invoke(player, component)
    }

    @JvmStatic
    fun run(
        player: Player,
        component: Component,
        opcode: Int,
        buttonID: Int,
        slot: Int,
        itemID: Int,
    ): Boolean {
        val exact = "${component.id}:$buttonID"
        val key = if (buttonListeners.containsKey(exact)) exact else "${component.id}"
        val method = buttonListeners[key] ?: return false
        // Compatibility entry point for old callers; do not pass a legacy ID to a native listener.
        val operation = if (key in nativeOperationListeners) {
            if (opcode == InterfaceOperation.CONTINUE) InterfaceOperation.CONTINUE_INDEX
            else (InterfaceOperation.OP1..InterfaceOperation.OP10).firstOrNull { InterfaceOperation.listenerId(it) == opcode } ?: return false
        } else opcode
        return method.invoke(player, component, operation, buttonID, slot, itemID)
    }
}
