package content.minigame.duel_arena.plugin

import core.api.*
import core.game.component.Component
import core.game.component.ComponentDefinition
import core.game.component.ComponentPlugin
import core.game.interaction.InterfaceListeners
import core.game.interaction.InterfaceOperation
import core.game.container.ContainerType
import core.game.container.access.InterfaceContainer
import core.game.container.impl.EquipmentContainer
import core.game.global.action.EquipHandler.Companion.unequip
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.login.PlayerParser
import core.game.node.item.Item
import core.game.system.config.ItemConfigParser
import core.game.world.map.Location
import core.plugin.Plugin
import core.tools.RandomFunction
import java.text.DecimalFormat
import java.util.*

class DuelSession(
    val player: Player? = null,
    val other: Player? = null,
    val staked: Boolean,
) : ComponentPlugin() {
    val rules = arrayOfNulls<DuelRule>(DuelRule.values().size)
    var playerContainer: StakeContainer? = null
    var targetContainer: StakeContainer? = null
    var acceptState = 0
    var fightState = 0

    fun end(closing: Player? = null) {
        removeAttribute(player!!, "duel:partner")
        other!!.removeAttribute("duel:partner")
        removeAttribute(player, "duel:staked")
        other.removeAttribute("duel:staked")
        other.removeAttribute("duel:forfeit")
        removeAttribute(player, "duel:forfeit")
        if (player !== closing && player.interfaceManager.opened?.id in setOf(631, 637, 626, 639)) player.interfaceManager.close()
        if (staked && player.interfaceManager.getComponent(336) != null) player.interfaceManager.closeSingleTab()
        player.interfaceManager.restoreTabs()
        if (other !== closing && other.interfaceManager.opened?.id in setOf(631, 637, 626, 639)) other.interfaceManager.close()
        if (staked && other.interfaceManager.getComponent(336) != null) other.interfaceManager.closeSingleTab()
        other.interfaceManager.restoreTabs()
        if (staked) {
            for (p in listOf(player, other)) {
                p.packetDispatch.deleteClientContainer(StakeContainer.KEY, false)
                p.packetDispatch.deleteClientContainer(StakeContainer.KEY, true)
            }
        }
        other.locks.unlockMovement()
        player.locks.unlockMovement()
        player.hintIconManager.clear()
        other.hintIconManager.clear()
        heal(player)
        heal(other)
        for (rule in rules) {
            if (rule != null) {
                player.locks.unlock(rule.name.lowercase(), true, player)
                other.locks.unlock(rule.name.lowercase(), true, other)
            }
        }
    }

    fun heal(p: Player) {
        p.fullRestore()
        if (isPoisoned(p)) {
            curePoison(p)
        }
        p.getSkills().restore()
    }

    fun openRules() {
        setAttribute(player!!, "duel:partner", other)
        other!!.setAttribute("duel:partner", player)
        setAttribute(player, "duel:staked", staked)
        setAttribute(other, "duel:staked", staked)
        removeAttribute(player, "duel:accepted")
        removeAttribute(other, "duel:accepted")
        if (staked) {
            playerContainer = StakeContainer(player, this)
            targetContainer = StakeContainer(other, this)
        }
        if (!openRules(player, other) || !openRules(other, player)) {
            decline(player)
            return
        }
        if (staked) {
            playerContainer?.sync()
            targetContainer?.sync()
        }
    }

    private fun openRules(
        player: Player,
        opponent: Player,
    ): Boolean {
        setVarp(player, 286, 0)
        if (staked) {
            val container = getContainer(player) ?: return false
            if (player.interfaceManager.open(STAKED_INTER) == null) return false
            player.packetDispatch.sendString(opponent.username, 631, 25)
            player.packetDispatch.sendString(Integer.toString(opponent.properties.currentCombatLevel), 631, 27)
            player.packetDispatch.sendString("", 631, 28)
            container.open()
        } else {
            if (player.interfaceManager.open(FRIENDLY_INTER) == null) return false
            player.packetDispatch.sendString(opponent.username, 637, 15)
            player.packetDispatch.sendString(Integer.toString(opponent.properties.currentCombatLevel), 637, 17)
            player.packetDispatch.sendString("", 637, 44)
        }
        setVarp(player, 286, 0)
        return true
    }

    fun leave(
        p: Player,
        type: Int,
    ) {
        if (fightState == 2) {
            return
        }
        val o = getOpposite(p)
        o.impactHandler.disabledTicks = 6
        o.teleport(RandomFunction.getRandomElement<Location>(DuelArea.RESPAWN_LOCATIONS))
        var victory = type == 0 || type == 2 || type == 1 && p.impactHandler.playerImpactLog.containsKey(o.details.uid)
        fightState = 2
        p.removeExtension(DuelSession::class.java)
        end()
        if (victory) {
            if (type == 0) {
                o.sendMessage("Well done! " + p.username + " resigned!")
            } else if (type == 2) {
                o.sendMessage("Well done! You have defeated " + p.username + "!")
            }
            victory(o)
        } else if (type == 1) {
            getContainer(p)?.release()
            getContainer(o)?.release()
            o.removeExtension(DuelSession::class.java)
            o.dialogueInterpreter.sendDialogue("Your opponent timed out, there was no winner!")
        }
        DuelArenaActivity.insertEntry(o, p)
    }

    fun victory(player: Player) {
        val component: Component =
            if (staked) STAKE_VICTORY else FRIENDLY_VICTORY
        if (player.interfaceManager.open(component) == null) return
        player.packetDispatch.sendString(getOpposite(player).username, component.id, if (staked) 33 else 31)
        player.packetDispatch.sendString(
            Integer.toString(getOpposite(player).properties.currentCombatLevel),
            component.id,
            if (staked) 32 else 30,
        )
        if (staked) {
            getContainer(player)!!.release()
            val targetContainer = getOppositeContainer(player)
            if (targetContainer!!.itemCount() > 0) {
                InterfaceContainer.generateItems(
                    player,
                    targetContainer.toArray().toList(),
                    listOf("Examine"),
                    634,
                    28, 5, 6
                )
            }
            val c = core.game.container.Container(28, ContainerType.ALWAYS_STACK)
            c.addAll(targetContainer)
            var log = "defeated => " + getOpposite(player).name + " and receieved {"
            for (i in c.toArray()) {
                if (i == null) {
                    continue
                }
                log += i.name + " x " + i.amount + ","
            }
            if (log[log.length - 1] == ',') {
                log = log.substring(0, log.length - 1)
            }
            log += "}"
        } else {
            player.removeExtension(DuelSession::class.java)
        }
    }

    private fun accept() {
        val session = player!!.getExtension<DuelSession>(DuelSession::class.java) ?: return
        when (session.acceptState) {
            2 -> {
                if (!session.checkRules(session.player!!) || !session.checkRules(session.other!!)) {
                    resetAccept()
                    return
                }
                session.player.lock(1)
                session.other.lock(1)
                session.acceptState = 3
                session.player.removeAttribute("duel:accepted")
                session.other.removeAttribute("duel:accepted")
                val confirmation = if (session.staked) STAKED_RULE_INTER else FRIENDLY_RULE_INTER
                val opened = session.other.interfaceManager.open(confirmation) != null &&
                    session.player.interfaceManager.open(confirmation) != null
                session.acceptState = 4
                if (!opened) {
                    decline(player)
                    return
                }
                session.player.interfaceManager.closeSingleTab()
                session.other.interfaceManager.closeSingleTab()
                session.player.interfaceManager.removeTabs(0, 1, 2, 3, 4, 5, 6, 7, 11, 12)
                session.other.interfaceManager.removeTabs(0, 1, 2, 3, 4, 5, 6, 7, 11, 12)
                val before = StringBuilder()
                val during = StringBuilder()
                if (hasEquipmentRules()) {
                    before.append("Some worn items will be taken off.<br>")
                }
                if (hasRule(DuelRule.NO_WEAPON) || hasRule(DuelRule.NO_SHIELD)) {
                    during.append("You can't use 2H weapons such as bows.<br>")
                }
                if (hasRule(DuelRule.NO_DRINKS) || hasRule(DuelRule.NO_FOOD)) {
                    before.append("Boosted stats will be restored.<br>")
                }
                if (hasRule(DuelRule.NO_PRAYER)) {
                    before.append("Existing prayers will be stopped.<br>")
                }
                if (before.length == 0) {
                    before.append("Nothing will be changed.<br>")
                }
                if (during.length == 0) {
                    during.append("You will fight using normal combat.<br>")
                }
                for (rule in rules) {
                    if (rule != null && rule.equipmentSlot == -1 && rule.info.isNotEmpty()) {
                        during.append(rule.info + "<br>")
                    }
                }
                val interfaceId = if (session.staked) 626 else 639
                val childs =
                    if (staked) {
                        intArrayOf(28, 29, 30, 31, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45)
                    } else {
                        intArrayOf(16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32)
                    }
                clearChilds(session.player, interfaceId, *childs)
                clearChilds(session.other, interfaceId, *childs)
                var tokens: Array<String?> =
                    before
                        .toString()
                        .split("<br>".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                run {
                    var i = 0
                    while (i < minOf(tokens.size, if (staked) 4 else 5)) {
                        session.player.packetDispatch.sendString(
                            tokens[i],
                            interfaceId,
                            if (this.staked) 28 + i else 16 + i,
                        )
                        session.other.packetDispatch.sendString(
                            tokens[i],
                            interfaceId,
                            if (this.staked) 28 + i else 16 + i,
                        )
                        i++
                    }
                }
                tokens =
                    during
                        .toString()
                        .split("<br>".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                var i = 0
                while (i < minOf(tokens.size, if (staked) 12 else 11)) {
                    session.player.packetDispatch.sendString(tokens[i], interfaceId, if (staked) 33 + i else 22 + i)
                    session.other.packetDispatch.sendString(tokens[i], interfaceId, if (staked) 33 + i else 22 + i)
                    i++
                }
                if (staked) {
                    session.playerContainer?.sync()
                    session.targetContainer?.sync()
                }
                session.updateToolTip(player, "")
                session.updateToolTip(getOpposite(player), "")
            }

            6 -> {
                session.player!!.lock(2)
                session.other!!.lock(2)
                session.player.sendMessage("Accepted stake and duel options.")
                session.other.sendMessage("Accepted stake and duel options.")
                session.acceptState = 7
                DuelArenaActivity.getDuelArea(hasRule(DuelRule.OBSTACLES)).duel(this)
                session.applyRules(session.player)
                session.applyRules(session.other)
                session.acceptState = 8
            }
        }
    }

    fun applyRules(player: Player) {
        for (rule in rules) {
            if (rule != null) {
                if (rule.equipmentSlot != -1 && player.equipment[rule.equipmentSlot] != null) {
                    unequip(player, rule.equipmentSlot, player.equipment.getId(rule.equipmentSlot))
                }
                player.locks.lock(rule.name.lowercase(Locale.getDefault()), 100000)
            }
        }
        if (hasRule(DuelRule.NO_SHIELD)) {
            val i = player.equipment[EquipmentContainer.SLOT_WEAPON]
            if (i != null && i.definition.getConfiguration(ItemConfigParser.TWO_HANDED, false)) {
                unequip(player, EquipmentContainer.SLOT_WEAPON, i.id)
            }
        }
        if (hasRule(DuelRule.NO_DRINKS) || hasRule(DuelRule.NO_FOOD)) {
            player.getSkills().restore()
        }
        if (hasRule(DuelRule.NO_PRAYER)) {
            player.prayer.reset()
        }
    }

    private fun toggleRule(
        p: Player,
        index: Int,
    ) {
        val session = player!!.getExtension<DuelSession>(DuelSession::class.java) ?: return
        if (session.acceptState !in 0..1 || index !in rules.indices) return
        session.resetAccept()
        if (rules[index] != null) {
            rules[index] = null
        } else {
            if (index < 3) {
                var count = 0
                for (i in 0..2) {
                    if (rules[i] != null) {
                        count++
                    }
                }
                if (count == 2) {
                    session.updateToolTip(
                        p,
                        "You can't have No Ranged, No Melee AND No Magic, how would you fight?",
                    )
                    p.packetDispatch.sendMessage(
                        "You can't have No Ranged, No Melee AND No Magic, how would you fight?",
                    )
                    return
                }
            } else if (index == 8 && rules[9] != null || index == 9 && rules[8] != null) {
                rules[if (index == 8) 9 else 8] = null
                p.packetDispatch.sendMessage(
                    if (index ==
                        8
                    ) {
                        "You can't have obstacles if you want No Movement."
                    } else {
                        "You can't have No Movement in an area with obstacles."
                    },
                )
            }
            if (index == 15 || index == 16) {
                p.sendMessage("Beware: You won't be able to use two-handed weapons such as bows.")
            }
            rules[index] = DuelRule.values()[index]
        }
        var value = 0
        for (i in rules.indices) {
            if (rules[i] != null) {
                value = value or (1 shl rules[i]!!.configIndex)
            }
        }
        setVarp(player, 286, value)
        setVarp(other!!, 286, value)
    }

    private fun checkRules(player: Player): Boolean {
        val c = core.game.container.Container(60)
        for (rule in rules) {
            if (rule != null && rule.equipmentSlot != -1 && player.equipment[rule.equipmentSlot] != null) {
                c.add(player.equipment[rule.equipmentSlot])
            }
        }
        if (hasRule(DuelRule.FUN_WEAPONS) && !hasFunWeapon(player)) {
            updateToolTip(player, "Fun Weapons is selected but you don't have a 'fun weapon'.")
            updateToolTip(
                getOpposite(player),
                "Fun Weapons is selected but your opponent does not have a 'fun weapon'.",
            )
            return false
        }
        if (staked) {
            c.addAll(getContainer(player))
            c.addAll(getOppositeContainer(player))
        }
        if (!player.inventory.hasSpaceFor(c)) {
            updateToolTip(player, "You do not have enough space for the items removed and/or the stake.")
            updateToolTip(
                getOpposite(player),
                "Your opponent does not have enough space for the items removed and/or the stake.",
            )
            return false
        }
        return true
    }

    override fun handle(player: Player, component: Component, opcode: Int, button: Int, slot: Int, itemId: Int): Boolean {
        val operation = (InterfaceOperation.OP1..InterfaceOperation.OP10)
            .firstOrNull { InterfaceOperation.listenerId(it) == opcode } ?: return true
        return handleNative(player, component, operation, button, slot, itemId)
    }

    private fun handleNative(player: Player, component: Component, operation: Int, button: Int, slot: Int, itemId: Int): Boolean {
        val session = player.getExtension<DuelSession>(DuelSession::class.java) ?: return true
        if (player.interfaceManager.getComponent(component.id) !== component || component.isHidden) return true
        val rulesInterface = if (session.staked) 631 else 637
        val confirmInterface = if (session.staked) 626 else 639
        when (component.id) {
            confirmInterface -> {
                if (session.acceptState !in 4..5 || operation != InterfaceOperation.OP1) return true
                when (button) {
                    if (session.staked) 53 else 35 -> session.requestAccept(player, component)
                    if (session.staked) 55 else 37 -> player.interfaceManager.close()
                }
            }
            rulesInterface -> {
                if (session.acceptState !in 0..1) return true
                if (operation == InterfaceOperation.OP1) {
                    when (button) {
                        if (session.staked) 101 else 82 -> { session.requestAccept(player, component); return true }
                        if (session.staked) 106 else 85 -> { player.interfaceManager.close(); return true }
                    }
                    DuelRuleComponents.rule(component.id, button)?.let {
                        session.toggleRule(player, it.ordinal)
                        return true
                    }
                }
                if (session.staked && button == 102) session.handleStakeItem(player, component, operation, slot, itemId, false)
                if (session.staked && button == 103 && operation == InterfaceOperation.OP1) {
                    val item = session.getOppositeContainer(player)?.get(slot)
                    if (item != null && item.id == itemId) sendMessage(player, item.definition.examine)
                }
            }
            634 -> if (session.staked && session.fightState == 2 && button == 28 &&
                operation in intArrayOf(InterfaceOperation.OP1, InterfaceOperation.OP10)) {
                val item = session.getOppositeContainer(player)?.get(slot)
                if (item != null && item.id == itemId) sendMessage(player, item.definition.examine)
            }
            336 -> if (session.staked && session.acceptState in 0..1 && button == 0 &&
                player.interfaceManager.getComponent(631) != null) {
                session.handleStakeItem(player, component, operation, slot, itemId, true)
            }
        }
        return true
    }

    private fun handleStakeItem(player: Player, component: Component, operation: Int, slot: Int, itemId: Int, offer: Boolean) {
        val stake = getContainer(player) ?: return
        val source = if (offer) player.inventory else stake
        val item = source.get(slot) ?: return
        if (item.id != itemId) return
        val originalSession = player.session
        val main = player.interfaceManager.getComponent(631) ?: return
        fun transfer(amount: Int) {
            if (amount <= 0 || acceptState !in 0..1 || player.session !== originalSession ||
                player.getExtension<DuelSession>(DuelSession::class.java) !== this ||
                player.interfaceManager.getComponent(631) !== main || main.isHidden ||
                player.interfaceManager.getComponent(component.id) !== component || component.isHidden ||
                source.get(slot) !== item || item.id != itemId) return
            if (offer) stake.offer(slot, amount) else stake.withdraw(slot, amount)
        }
        when (operation) {
            InterfaceOperation.OP1 -> transfer(1)
            InterfaceOperation.OP2 -> transfer(5)
            InterfaceOperation.OP3 -> transfer(10)
            InterfaceOperation.OP4 -> transfer(source.getAmount(item))
            InterfaceOperation.OP5 -> sendInputDialogue(player, true, "Enter the amount:") { value ->
                (value as? Int)?.let { transfer(it) }
            }
            InterfaceOperation.OP10 -> sendMessage(player, item.definition.examine)
        }
    }

    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        for (id in intArrayOf(631, 637, 626, 639, 634)) {
            ComponentDefinition.put(id, this)
            InterfaceListeners.addNative(id) { player, component, operation, button, slot, itemId ->
                handleNative(player, component, operation, button, slot, itemId)
            }
        }
        // 336 is also used by TradeInterface; its native listener routes active duel stakes.
        StakeContainer.OVERLAY.plugin = this
        return this
    }

    fun handleStakeSide(player: Player, component: Component, operation: Int, button: Int, slot: Int, itemId: Int): Boolean =
        handleNative(player, component, operation, button, slot, itemId)

    fun requestAccept(
        player: Player,
        component: Component?,
    ) {
        val session = player.getExtension<DuelSession>(DuelSession::class.java) ?: return

        if (session.acceptState !in intArrayOf(0, 1, 4, 5)) return
        if (session.acceptState == (if (session.acceptState < 3) 1 else 5) &&
            session
                .getOpposite(player)
                .getAttribute("duel:accepted", false)
        ) {
            session.acceptState = if (session.acceptState == 1) 2 else 6
            session.accept()
            return
        }
        if (session.acceptState == 0 || session.acceptState == 4) {
            session.acceptState = if (session.acceptState == 0) 1 else 5
            setAttribute(player, "duel:accepted", true)
            session.updateToolTip(player, "Waiting for other player...")
            session.updateToolTip(session.getOpposite(player), "Other player accepted.")
        }
    }

    fun resetAccept() {
        acceptState = if (acceptState < 3) 0 else 4
        removeAttribute(player!!, "duel:accepted")
        removeAttribute(other!!, "duel:accepted")
        updateToolTip(player, "")
        updateToolTip(other, "")
    }

    fun clearChilds(
        player: Player,
        interfaceId: Int,
        vararg childs: Int,
    ) {
        for (i in childs) {
            player.packetDispatch.sendString("", interfaceId, i)
        }
    }

    private fun updateToolTip(
        player: Player,
        message: String,
    ) {
        val session = player.getExtension<DuelSession>(DuelSession::class.java) ?: return
        var interfaceId = 631
        var child = 28
        if (!staked) {
            interfaceId =
                if (acceptState < 4) FRIENDLY_INTER.id else FRIENDLY_RULE_INTER.id
            child = if (acceptState < 4) 44 else 33
        } else if (acceptState > 3) {
            interfaceId = STAKED_RULE_INTER.id
            child = 45
        }
        if (player.interfaceManager.getComponent(interfaceId) != null)
            player.packetDispatch.sendString(message, interfaceId, child)
    }

    private fun getDisplayMessage(items: Array<Item?>): String {
        var message = "Absolutely nothing!"
        if (items.size > 0) {
            message = ""
            for (i in items.indices) {
                if (items[i] == null) {
                    continue
                }
                message = message + "<col=FF9040>" + items[i]!!.name
                if (items[i]!!.amount > 1) {
                    message = "$message<col=FFFFFF> x "
                    message = message + "<col=FFFF00>" + getFormattedNumber(items[i]!!.amount) + "<br>"
                } else {
                    message = "$message<br>"
                }
            }
        }
        return message
    }

    fun hasFunWeapon(player: Player): Boolean {
        val c = core.game.container.Container(60)
        c.addAll(player.inventory)
        c.addAll(player.equipment)
        for (item in c.toArray()) {
            if (item == null) {
                continue
            }
            if (item.definition.getConfiguration("fun_weapon", false)) {
                return true
            }
        }
        return false
    }

    fun isRestrictedEquipment(item: Item?): Boolean {
        if (item == null) {
            return false
        }
        val slot = item.definition.getConfiguration(ItemConfigParser.EQUIP_SLOT, -1)
        if (slot == -1) {
            return false
        }
        val twoHanded = item.definition.getConfiguration(ItemConfigParser.TWO_HANDED, false)
        if (slot == EquipmentContainer.SLOT_WEAPON && twoHanded && hasRule(DuelRule.NO_SHIELD)) {
            return true
        }
        for (rule in rules) {
            if (rule == null) {
                continue
            }
            if (rule.equipmentSlot == slot) {
                return true
            }
        }
        return false
    }

    fun hasEquipmentRules(): Boolean {
        for (rule in rules) {
            if (rule != null && rule.ordinal >= DuelRule.NO_HATS.ordinal) {
                return true
            }
        }
        return false
    }

    fun hasRule(r: DuelRule): Boolean = rules[r.ordinal] != null

    fun getOppositeContainer(player: Player): StakeContainer? =
        if (player ===
            this.player
        ) {
            targetContainer
        } else {
            playerContainer
        }

    fun getContainer(player: Player): StakeContainer? = if (player === this.player) playerContainer else targetContainer

    private fun getFormattedNumber(amount: Int): String = DecimalFormat("#,###,##0").format(amount.toLong()).toString()

    val ruleSize: Int
        get() {
            var count = 0
            for (rule in rules) {
                if (rule != null) {
                    count++
                }
            }
            return count
        }

    fun getOpposite(player: Player): Player = if (this.player === player) other!! else this.player!!

    fun getOther(player: Player): Player = if (player === this.player) other!! else player

    companion object {
        private val FRIENDLY_INTER =
            Component(637).setUncloseEvent { player, c ->
                decline(player, true)
                true
            }
        private val STAKED_INTER =
            Component(631).setUncloseEvent { player, c ->
                decline(player, true)
                true
            }
        private val FRIENDLY_RULE_INTER =
            Component(639).setUncloseEvent { player, c ->
                decline(player, true)
                true
            }
        private val STAKED_RULE_INTER =
            Component(626).setUncloseEvent { player, c ->
                decline(player, true)
                true
            }
        private val FRIENDLY_VICTORY = Component(633)
        private val STAKE_VICTORY =
            Component(634).setUncloseEvent { player, c ->
                reward(player)
                true
            }

        fun decline(player: Player, closing: Boolean = false) {
            val session = player.getExtension<DuelSession>(DuelSession::class.java) ?: return
            if (session.acceptState == 3 || session.acceptState >= 6 || session.fightState != 0) {
                return
            }
            if (session.staked) {
                session.playerContainer!!.release()
                session.targetContainer!!.release()
                session.resetAccept()
            }
            session.player!!.removeExtension(DuelSession::class.java)
            session.other!!.removeExtension(DuelSession::class.java)
            session.end(if (closing) player else null)
            if (player === session.other) {
                session.player.packetDispatch.sendMessage(
                    "Other player declined " + (if (session.staked) "stake and " else "") + "duel options.",
                )
            } else {
                session.other.packetDispatch.sendMessage(
                    "Other player declined " + (if (session.staked) "stake and " else "") + "duel options.",
                )
            }
        }

        fun reward(player: Player) {
            val session = player.getExtension<DuelSession>(DuelSession::class.java)
            if (session == null || session.fightState != 2 || !session.staked) {
                return
            }
            val targetContainer = session.getOppositeContainer(player) ?: return
            session.fightState = 4
            if (!player.inventory.hasSpaceFor(targetContainer)) {
                player.bank.addAll(targetContainer)
                player.sendMessage("An error occured & the stake transfered to your bank.")
            } else {
                player.inventory.addAll(targetContainer)
            }
            player.removeExtension(DuelSession::class.java)
            PlayerParser.save(player)
        }
    }
}
