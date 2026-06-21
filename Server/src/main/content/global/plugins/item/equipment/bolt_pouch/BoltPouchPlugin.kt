package content.global.plugins.item.equipment.bolt_pouch

import core.api.*
import core.cache.def.impl.ItemDefinition
import core.game.container.impl.EquipmentContainer
import core.game.event.EventHook
import core.game.event.ItemEquipEvent
import core.game.event.ItemUnequipEvent
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.system.config.ItemConfigParser
import core.tools.colorize
import shared.consts.Components
import shared.consts.Items

private const val BOLT_POUCH = Items.BOLT_POUCH_9433
private const val MAX_SLOTS = 4
private const val ARROWS_SLOT = EquipmentContainer.SLOT_ARROWS
private val WIELD_BOLT_SLOTS = intArrayOf(3, 7, 11, 15)
private val REMOVE_BOLT_SLOTS = intArrayOf(4, 8, 12, 16)
private const val UNWIELD_BOLT_SLOT = 19

private val boltQtyVarbits = intArrayOf(2469,2470,2471,2472)
private val boltNameTextIds = intArrayOf(25, 26, 27, 28)
private val modelComponentIds = intArrayOf(2, 6, 10, 14)

class BoltPouchPlugin : InterfaceListener, InteractionListener
{
    private val equipHook =
        object : EventHook<ItemEquipEvent>
        {
            override fun process(entity: Entity, event: ItemEquipEvent)
            {
                val p = entity as Player
                if (event.slotId == ARROWS_SLOT) updateBoltPouchDisplay(p)
            }
        }

    private val unequipHook =
        object : EventHook<ItemUnequipEvent>
        {
            override fun process(entity: Entity, event: ItemUnequipEvent)
            {
                val p = entity as Player
                if (event.slotId == ARROWS_SLOT) updateBoltPouchDisplay(p)
            }
        }

    override fun defineListeners() {
        on(BOLT_POUCH, IntType.ITEM, "open") { player, _ ->
            openInterface(player, Components.XBOWS_POUCH_433)
            player.hook(ItemEquipEvent::class.java, equipHook)
            player.hook(ItemUnequipEvent::class.java, unequipHook)
            return@on true
        }

        /*
         * Handles destroy of bolt pouch.
         */

        on(BOLT_POUCH, IntType.ITEM, "destroy") { player, item ->
            val itemDef = ItemDefinition.forId(item.id)
            sendDestroyItemDialogue(
                player,
                itemDef.id,
                itemDef.getConfiguration(ItemConfigParser.DESTROY_MESSAGE)
            )

            addDialogueAction(player) { p, button ->
                if (button == 3)
                {
                    closeDialogue(p)
                    val manager = player.boltPouchManager

                    for (i in 0 until 4)
                    {
                        val id = manager.getBolt(i)
                        val amount = manager.getAmount(i)

                        if (id > 0)
                        {
                            GroundItemManager.create(
                                Item(id, amount),
                                player.location,
                                player
                            )
                        }
                    }

                    manager.clearAll()
                    removeItem(p, BOLT_POUCH)
                    updateBoltPouchDisplay(p)
                }
            }
            return@on true
        }

        /*
         * Handles add the bolts to bolt pouch.
         */

        onUseWith(IntType.ITEM, BoltPouchManager.ALLOWED_BOLT_IDS, BOLT_POUCH) { player, used, _ ->
            val bolts = used.asItem()
            val manager = player.boltPouchManager

            val added = manager.addBolts(bolts.id, bolts.amount)

            if (added > 0)
            {
                player.inventory.remove(Item(bolts.id, added))
                sendMessage(player, "You add some bolts into the bolt pouch.")
            }
            else
            {
                sendMessage(player, "You can't hold any more of those bolts.")
            }
            updateBoltPouchDisplay(player)
            return@onUseWith true
        }
    }

    override fun defineInterfaceListeners()
    {
        on(Components.XBOWS_POUCH_433) { player, _, _, buttonID, _, _ ->
            val manager = player.boltPouchManager
            when (buttonID)
            {
                in WIELD_BOLT_SLOTS -> {
                    val pouchSlot = WIELD_BOLT_SLOTS.indexOf(buttonID)
                    when {
                        !manager.hasBolts(pouchSlot) -> sendMessage(player, "You don't have any bolts in this slot.")
                        manager.wieldBolts(pouchSlot) -> sendMessage(player, "You wield some bolts from your bolt pouch.")
                    }
                    updateBoltPouchDisplay(player)
                }

                in REMOVE_BOLT_SLOTS -> {
                    val pouchSlot = REMOVE_BOLT_SLOTS.indexOf(buttonID)
                    when {
                        !manager.hasBolts(pouchSlot) -> sendMessage(player, "There's nothing to remove in this slot.")
                        freeSlots(player) == 0 -> sendMessage(player, "You don't have enough space in your inventory.")
                        manager.removeBolts(pouchSlot) -> {
                            val ordinal = arrayOf("first", "second", "third", "fourth")[pouchSlot]
                            sendMessage(player, "You remove all the bolts from the $ordinal slot of your bolt pouch.")
                        }
                    }
                    updateBoltPouchDisplay(player)
                }

                UNWIELD_BOLT_SLOT -> {
                    val ammo = player.equipment.get(ARROWS_SLOT)

                    when {
                        ammo == null || ammo.amount == 0 -> sendMessage(player, "You're not wielding any bolts.")
                        freeSlots(player) == 0 -> sendMessage(player, "You don't have enough space in your inventory.")
                        else -> {
                            player.equipment.remove(ammo)
                            player.inventory.add(ammo)
                        }
                    }

                    updateBoltPouchDisplay(player)
                }
                else -> return@on false
            }

            return@on true
        }

        onOpen(Components.XBOWS_POUCH_433) { player, _ ->
            updateBoltPouchDisplay(player)
            return@onOpen true
        }

        onClose(Components.XBOWS_POUCH_433) { player, _ ->
            player.unhook(equipHook)
            player.unhook(unequipHook)
            return@onClose true
        }
    }

    fun updateBoltPouchDisplay(player: Player)
    {
        val manager = player.boltPouchManager
        val current = player.equipment.get(ARROWS_SLOT)
        val iface = Components.XBOWS_POUCH_433

        /*
         * Equipped bolts.
         */

        sendItemOnInterface(player,iface,18,current?.id?:-1,90)
        sendString(player,current?.name?:"Nothing",iface,29)

        val ammoAmount =current?.amount?:0

        if (ammoAmount == 0)
        {
            sendString(player, "0", iface, 24)
        }
        else
        {
            sendString(player,colorize("%G${current.amount}"),iface,24)
        }

        /*
         * Pouch slots.
         */

        for (i in 0 until MAX_SLOTS)
        {
            val boltId = manager.getBolt(i)
            val amount = manager.getAmount(i)
            setVarbit(player,boltQtyVarbits[i],amount,true)
            if (boltId<=0)
            {
                sendString(player,"Nothing",iface,boltNameTextIds[i])
                sendItemOnInterface(player, iface, modelComponentIds[i], -1)
            }
            else
            {
                sendString(player,getItemName(boltId),iface,boltNameTextIds[i])
                sendItemOnInterface(player, iface, modelComponentIds[i],boltId,90)
            }
        }
    }
}