package content.global.skill.construction.decoration.costumeroom

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import core.api.*
import core.cache.def.impl.DataMap
import core.cache.def.impl.ItemDefinition
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.net.packet.PacketRepository
import core.net.packet.context.ContainerContext
import core.net.packet.out.ContainerPacket
import shared.consts.Items

class StorageBoxInterface : InterfaceListener {
    companion object {
        private const val INTERFACE = 467
        private const val COMPONENT = 164
        private const val SIZE = 30
        private const val BUTTON_MORE = Items.MORE_10165
        private const val BUTTON_BACK = Items.BACK_10166

        lateinit var instance: StorageBoxInterface
            private set

        fun openStorage(player: Player, type: StorableType) {
            instance.openStorageForType(player, type)
        }
    }

    init {
        instance = this
    }

    override fun defineInterfaceListeners() {
        on(INTERFACE) { player, _, _, buttonId, _, _ ->
            val typeName = getAttribute(player, "con:storage:type", null) as? String ?: return@on true
            val type = StorableType.valueOf(typeName)
            handleInteraction(player, buttonId, type)
            return@on true
        }
    }

    private fun getContainer(player: Player, type: StorableType): StorageContainer = player.getCostumeRoomState().getContainer(type)

    private fun getPageSlots(allItems: List<Storable>, pageIndex: Int): List<Any?> {
        val totalPages = (allItems.size + SIZE - 1) / SIZE
        val slots = MutableList<Any?>(SIZE) { null }

        val pageSize = when
        {
            totalPages <= 1 -> SIZE
            pageIndex == 0 -> SIZE - 1
            pageIndex == totalPages - 1 -> SIZE - 1
            else -> SIZE - 2
        }

        val pageItems = allItems.drop(pageIndex * pageSize).take(pageSize)
        var index = 0
        pageItems.forEach { slots[index++] = it }

        when {
            totalPages <= 1 -> { // Nothing.
            }
            pageIndex == 0 -> {
                slots[index++] = "MORE"
            }
            pageIndex in 1 until totalPages - 1 -> {
                slots[index++] = "MORE"
                slots[index++] = "BACK"
            }
            pageIndex == totalPages - 1 -> {
                slots[index++] = "BACK"
            }
        }

        return slots
    }

    private fun handleInteraction(player: Player, buttonId: Int, type: StorableType) {
        val container = getContainer(player, type)
        val tier = container.getTier(type)
        val allItems = StorableRepository.getItems(type, tier)
        val pageIndex = container.getPageIndex(type)
        val slots = getPageSlots(allItems, pageIndex)
        val slotIndex = when (buttonId) {
            in 56..(56 + (SIZE - 1) * 2) step 2 -> (buttonId - 56) / 2
            in 165..223 step 2 -> (buttonId - 165) / 2
            else -> return
        }

        when (val clicked = slots.getOrNull(slotIndex)) {
            "MORE" -> {
                container.nextPage(type, allItems.size, SIZE)
                openInterface(player, INTERFACE)
                updateInterface(player, type)
            }

            "BACK" -> {
                container.prevPage(type)
                openInterface(player, INTERFACE)
                updateInterface(player, type)
            }

            is Storable -> {
                processItem(player, clicked, type)
            }
        }
    }

    private fun processItem(player: Player, item: Storable, type: StorableType) {
        val container = getContainer(player, type)
        val storedItems = container.getItems(type).toSet()
        val actualId = item.takeIds.firstOrNull() ?: item.displayId
        val questUnlocked = Storable.isQuestUnlocked(player, item)
        val stored = item.takeIds.any { it in storedItems }

        /*
         * Quest books.
         */
        if (questUnlocked)
        {
            if (inInventory(player,actualId,1))
            {
                sendMessage(player, "You already have this book in your inventory.")
                return
            }

            if (freeSlots(player) <= 0)
            {
                sendMessage(player, "You don't have enough inventory space.")
                return
            }

            sendMessage(player, "You take the book from the ${boxName(type)}.")
            addItem(player, actualId, 1)
            updateInterface(player, type)
            return
        }

        /*
         * Normal stored item.
         */
        if (stored)
        {

            if (freeSlots(player) <= 0)
            {
                sendMessage(player, "You don't have enough inventory space.")
                return
            }
            sendMessage(player, "You take the item from the ${boxName(type)}.")
            addItem(player, actualId, 1)
            container.withdraw(type, item)
            updateInterface(player, type)
            return
        }

        /*
         * Nothing stored and no quest unlock.
         *
         * Try to deposit the item from inventory.
         */
        if (!player.inventory.contains(actualId, 1))
        {
            sendMessage(player, "You don't have that item in your inventory.")
            return
        }
        sendMessage(player, "You put the item into the box.")
        removeItem(player, Item(actualId))
        container.addItem(type, actualId)
        updateInterface(player, type)
    }

    private fun updateInterface(player: Player, type: StorableType) {
        val container = getContainer(player, type)
        val tier = container.getTier(type)
        val allItems = StorableRepository.getItems(type, tier)
        val stored = container.getItems(type).toSet()
        val pageIndex = container.getPageIndex(type)

        val slots = getPageSlots(allItems, pageIndex)

        sendString(player, boxTitle(player, type), INTERFACE, 225)

        val itemsArray = slots.mapNotNull { slot ->

            when (slot){
                is Storable -> Item(slot.displayId)
                "MORE" -> Item(BUTTON_MORE)
                "BACK" -> Item(BUTTON_BACK)
                else -> null
            }
        }.toTypedArray()

        PacketRepository.send(
            ContainerPacket::class.java,
            ContainerContext(player, INTERFACE, COMPONENT, SIZE, itemsArray, false)
        )

        repeat(SIZE) { index ->
            val obj = slots[index]
            val namesMap = DataMap.get(380)
            val result = when (obj) {
                is Storable -> {
                    val displayName = namesMap.getString(obj.displayId) ?: getItemName(obj.displayId)
                    val fullName = if (type == StorableType.BOOK) {
                        val examine = ItemDefinition.forId(obj.displayId)?.examine ?: ""
                        "$displayName<br>${core.tools.YELLOW}$examine</col>"
                    } else { displayName }
                    val available = Storable.isAvailable(player, obj, stored)
                    fullName to !available
                }
                "MORE" -> "More..." to false
                "BACK" -> "Back..." to false
                else -> "" to true
            }

            val nameComp = 55 + index * 2
            val iconComp = 165 + index * 2
            val hiddenIconComp = 166 + index * 2

            sendString(player, result.first, INTERFACE, nameComp)
            sendInterfaceConfig(player, INTERFACE, nameComp, false)

            if (obj is Storable) {
                sendInterfaceConfig(player, INTERFACE, iconComp, result.second)
                sendInterfaceConfig(player, INTERFACE, hiddenIconComp, !result.second)
            } else {
                sendInterfaceConfig(player, INTERFACE, iconComp, true)
                sendInterfaceConfig(player, INTERFACE, hiddenIconComp, true)
            }
        }
    }

    private fun openStorageForType(player: Player, type: StorableType) {
        setAttribute(player, "con:storage:type", type.name)
        openInterface(player, INTERFACE)
        updateInterface(player, type)
    }

    private fun boxName(type: StorableType): String = when (type) {
        StorableType.TRAILS -> "Treasure chest"
        StorableType.ARMOUR -> "Magic wardrobe"
        StorableType.ARMOUR_CASE -> "Armour case"
        StorableType.BOOK -> "Bookcase"
        StorableType.CAPE -> "Cape rack"
        StorableType.FANCY -> "Fancy dress box"
        StorableType.TOY -> "Toy box"
    }

    private fun boxTitle(player: Player, type: StorableType): String {
        if (type == StorableType.TRAILS) {
            val tier = getContainer(player, type).getTier(type)
            return when (tier) {
                0 -> "Low-level Treasure Trail rewards"
                1 -> "Medium-level Treasure Trail rewards"
                2 -> "High-level Treasure Trail rewards"
                else -> "Low-level Treasure Trail rewards"
            }
        }
        return boxName(type)
    }
}

class StorageState(val player: Player) {
    enum class ContainerGroup {
        BOOK, CAPE, FANCY, TOY, TRAILS, ARMOUR, ARMOUR_CASE;
        companion object {
            fun fromType(type: StorableType) = when (type) {
                StorableType.BOOK -> BOOK
                StorableType.CAPE -> CAPE
                StorableType.FANCY -> FANCY
                StorableType.TOY -> TOY
                StorableType.TRAILS -> TRAILS
                StorableType.ARMOUR -> ARMOUR
                StorableType.ARMOUR_CASE -> ARMOUR_CASE
            }
        }
    }
    private val containers = ContainerGroup.values().associateWith { StorageContainer() }.toMutableMap()
    fun getContainer(type: StorableType) = containers.getValue(ContainerGroup.fromType(type))
    fun toJson(): JsonObject = JsonObject().apply {
        add("containers", JsonObject().apply {
            containers.forEach { (group, container) -> add(group.name.lowercase(), container.toJson()) }
        })
    }

    fun readJson(data: JsonObject) {
        data.getAsJsonObject("containers")?.let { json ->
            ContainerGroup.values().forEach { group ->
                json.getAsJsonObject(group.name.lowercase())?.let {
                    containers[group] = StorageContainer.fromJson(it)
                }
            }
        }
    }
}

class StorageContainer {
    private val stored = mutableMapOf<StorableType, MutableList<Int>>()
    private val currentPage = mutableMapOf<StorableType, Int>()
    private val tiers = mutableMapOf<StorableType, Int>()
    fun addItem(type: StorableType, id: Int) = stored.getOrPut(type) { mutableListOf() }.add(id)
    fun withdraw(type: StorableType, item: Storable) {
        val id = item.takeIds.firstOrNull() ?: item.displayId
        stored[type]?.remove(id)
    }
    fun contains(type: StorableType, id: Int) = id in (stored[type] ?: emptyList())
    fun getItems(type: StorableType) = stored[type]?.toList() ?: emptyList()
    fun getTier(type: StorableType) = tiers.getOrDefault(type, 0)
    fun setTier(type: StorableType, tier: Int) {
        tiers[type] = tier.coerceAtLeast(0)
        currentPage[type] = 0
    }
    fun getPageIndex(type: StorableType) = currentPage.getOrDefault(type, 0)
    fun nextPage(type: StorableType, totalItems: Int, pageSize: Int) { currentPage[type] = (getPageIndex(type) + 1).takeIf { it * pageSize < totalItems } ?: getPageIndex(type) }
    fun prevPage(type: StorableType) { currentPage[type] = (getPageIndex(type) - 1).coerceAtLeast(0) }
    fun toJson(): JsonObject = JsonObject().apply {
        add("items", JsonObject().apply {
            stored.forEach { (type, list) ->
                add(type.name.lowercase(), JsonArray().apply { list.forEach(::add) })
            }
        })
        add("tiers", JsonObject().apply {
            tiers.forEach { (type, tier) -> addProperty(type.name.lowercase(), tier) }
        })
    }
    companion object {
        fun fromJson(json: JsonObject) = StorageContainer().apply {
            json.getAsJsonObject("items")?.entrySet()?.forEach { (key, value) ->
                val type = StorableType.valueOf(key.uppercase())
                stored[type] = value.asJsonArray.map { it.asInt }.toMutableList()
            }
            json.getAsJsonObject("tiers")?.entrySet()?.forEach { (key, value) ->
                val type = StorableType.valueOf(key.uppercase())
                tiers[type] = value.asInt
            }
        }
    }
}

object StorableRepository {
    private val byType = Storable.values().groupBy { it.type }
    fun getItems(type: StorableType): List<Storable> = byType[type].orEmpty()
    fun getItems(type: StorableType, tier: Int): List<Storable> = byType[type].orEmpty().filter { it.tier <= tier }
}

enum class StorableType {
    BOOK, CAPE, FANCY, TOY, TRAILS, ARMOUR, ARMOUR_CASE
}
