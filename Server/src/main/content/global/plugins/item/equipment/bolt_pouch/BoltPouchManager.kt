package content.global.plugins.item.equipment.bolt_pouch

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import core.game.container.Container
import core.game.container.ContainerType
import core.game.container.impl.EquipmentContainer
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Items

class BoltPouchManager(private val player: Player) {

    companion object {
        const val SIZE = 255
        const val SLOTS = 4

        val ALLOWED_BOLT_IDS = intArrayOf(
            Items.BRONZE_BOLTS_877,
            Items.BRONZE_BOLTSP_878,
            Items.OPAL_BOLTS_879,
            Items.PEARL_BOLTS_880,
            Items.BARBED_BOLTS_881,
            Items.BOLT_RACK_4740,
            Items.BRONZE_BOLTSP_PLUS_6061,
            Items.BRONZE_BOLTSP_PLUS_PLUS_6062,
            Items.BONE_BOLTS_8882,
            Items.BLURITE_BOLTS_9139,
            Items.IRON_BOLTS_9140,
            Items.STEEL_BOLTS_9141,
            Items.MITHRIL_BOLTS_9142,
            Items.ADAMANT_BOLTS_9143,
            Items.RUNE_BOLTS_9144,
            Items.SILVER_BOLTS_9145,
            Items.OPAL_BOLTS_E_9236,
            Items.JADE_BOLTS_E_9237,
            Items.PEARL_BOLTS_E_9238,
            Items.TOPAZ_BOLTS_E_9239,
            Items.SAPPHIRE_BOLTS_E_9240,
            Items.EMERALD_BOLTS_E_9241,
            Items.RUBY_BOLTS_E_9242,
            Items.DIAMOND_BOLTS_E_9243,
            Items.DRAGON_BOLTS_E_9244,
            Items.ONYX_BOLTS_E_9245,
            Items.BLURITE_BOLTSP_9286,
            Items.IRON_BOLTS_P_9287,
            Items.STEEL_BOLTS_P_9288,
            Items.MITHRIL_BOLTS_P_9289,
            Items.ADAMANT_BOLTS_P_9290,
            Items.RUNITE_BOLTS_P_9291,
            Items.SILVER_BOLTS_P_9292,
            Items.BLURITE_BOLTSP_PLUS_9293,
            Items.IRON_BOLTSP_PLUS_9294,
            Items.STEEL_BOLTSP_PLUS_9295,
            Items.MITHRIL_BOLTSP_PLUS_9296,
            Items.ADAMANT_BOLTSP_PLUS_9297,
            Items.RUNITE_BOLTSP_PLUS_9298,
            Items.SILVER_BOLTSP_PLUS_9299,
            Items.BLURITE_BOLTSP_PLUS_PLUS_9300,
            Items.IRON_BOLTSP_PLUS_PLUS_9301,
            Items.STEEL_BOLTSP_PLUS_PLUS_9302,
            Items.MITHRIL_BOLTSP_PLUS_PLUS_9303,
            Items.ADAMANT_BOLTSP_PLUS_PLUS_9304,
            Items.RUNITE_BOLTSP_PLUS_PLUS_9305,
            Items.SILVER_BOLTSP_PLUS_PLUS_9306,
            Items.JADE_BOLTS_9335,
            Items.TOPAZ_BOLTS_9336,
            Items.SAPPHIRE_BOLTS_9337,
            Items.EMERALD_BOLTS_9338,
            Items.RUBY_BOLTS_9339,
            Items.DIAMOND_BOLTS_9340,
            Items.DRAGON_BOLTS_9341,
            Items.ONYX_BOLTS_9342,
            Items.KEBBIT_BOLTS_10158,
            Items.LONG_KEBBIT_BOLTS_10159,
            Items.BLACK_BOLTS_13083,
            Items.BLACK_BOLTSP_13084,
            Items.BLACK_BOLTSP_PLUS_13085,
            Items.BLACK_BOLTSP_PLUS_PLUS_13086,
            Items.BROAD_TIPPED_BOLTS_13280,
        )
    }

    private val container = Container(SLOTS, ContainerType.NEVER_STACK)

    fun hasBolts(slot: Int) =
        (container.get(slot)?.amount ?: 0) > 0

    fun getBolt(slot: Int) =
        container.get(slot)?.id ?: -1

    fun getAmount(slot: Int) =
        container.get(slot)?.amount ?: 0

    private fun clearSlot(slot: Int) {
        container.replace(null, slot)
        container.update()
    }

    fun clearAll() {
        for (i in 0 until container.capacity()) {
            container.replace(null, i)
        }
        container.update()
    }

    fun addBolts(id: Int, amount: Int): Int
    {
        if (id !in ALLOWED_BOLT_IDS) return 0

        for (i in 0 until container.capacity())
        {
            val item = container.get(i)
            if (item != null && item.id == id)
            {
                val space = SIZE - item.amount
                if (space <= 0) return 0
                val add = minOf(space, amount)
                container.replace(Item(id,item.amount+add),i)
                container.update()
                return add
            }
        }

        for (i in 0 until container.capacity())
        {
            if (container.get(i) == null)
            {
                val add = minOf(SIZE, amount)
                container.replace(Item(id, add), i)
                container.update()
                return add
            }
        }

        return 0
    }

    fun wieldBolts(slot: Int): Boolean
    {
        val item = container.get(slot) ?: return false
        val arrowSlot = EquipmentContainer.SLOT_ARROWS
        val current = player.equipment.get(arrowSlot)
        if (current != null && current.amount > 0) return false
        if (!player.equipment.add(item, true, arrowSlot))
            return false

        clearSlot(slot)
        return true
    }

    fun removeBolts(slot: Int): Boolean
    {
        val item = container.get(slot) ?: return false
        if (!player.inventory.hasSpaceFor(item)) return false
        player.inventory.add(item)
        clearSlot(slot)
        return true
    }

    fun save(root: JsonObject)
    {
        val arr = JsonArray()
        for (i in 0 until container.capacity())
        {
            val item = container.get(i)
            val obj = JsonObject()
            obj.addProperty("slot", i)
            obj.addProperty("id", item?.id ?: 0)
            obj.addProperty("amount", item?.amount ?: 0)
            arr.add(obj)
        }

        root.add("boltPouch", arr)
    }

    fun parse(data: JsonArray)
    {
        clearAll()
        data.forEach {
            val obj = it.asJsonObject
            val slot = obj["slot"].asInt
            val id = obj["id"].asInt
            val amount = obj["amount"].asInt
            if (id > 0 && amount > 0)
            {
                container.replace(Item(id, amount), slot)
            }
        }
    }
}