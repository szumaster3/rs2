package core.cache.def.impl

import content.global.skill.summoning.familiar.BurdenBeast
import core.api.EquipmentSlot
import core.api.equipSlot
import core.api.log
import core.cache.Cache
import core.cache.def.Definition
import core.cache.misc.buffer.ByteBufferUtils
import core.game.container.Container
import core.game.interaction.OptionHandler
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.node.item.ItemPlugin
import core.game.system.config.ItemConfigParser
import core.tools.Log
import core.tools.StringUtils
import java.nio.ByteBuffer
import java.util.*
import shared.consts.Items

class ObjType : Definition<Item>() {
    /** ObjType opcode 65: searchable by the client's tradeable-only item finder. */
    var cacheTradeable: Boolean = false
        private set
    var modelRotationZ: Int = 0
        private set
    var groundCursorOptions: MutableMap<Int, Int> = HashMap()
        private set
    var inventoryCursorOptions: MutableMap<Int, Int> = HashMap()
        private set
    var questIds: IntArray? = null
        private set
    var interfaceModelId: Int = 0
    var zoom2d: Int = 0
    var xAngle2d: Int = 0
    var yAngle2d: Int = 0
    var modelOffset1: Int = 0
    var modelOffset2: Int = 0
    private var stackable: Boolean = false
    var cost: Int = 1
    /** Legacy server API; the cache stores a single cost value (opcode 12). */
    var value: Int
        get() = cost
        set(value) { cost = value }
    var isMembersOnly: Boolean = false
    var maleWornModelId1: Int = -1
    var femaleWornModelId1: Int = -1
    var maleWornModelId2: Int = -1
    var femaleWornModelId2: Int = -1
    var maleHeadModelId1: Int = -1
    var femaleHeadModelId1: Int = -1
    var maleHeadModelId2: Int = -1
    var femaleHeadModelId2: Int = -1
    var groundOptions: Array<String?>? = arrayOf<String?>(null, null, "take", null, null)
    var originalModelColors: ShortArray? = null
    var modifiedModelColors: ShortArray? = null
    var retextureSource: ShortArray? = null
    var retextureDestination: ShortArray? = null
    var recolourPaletteIndices: ByteArray? = null
    var unknownArray2: IntArray? = null
    var isUnnoted: Boolean = true
    var maleWornModelId3: Int = -1
    var femaleWornModelId3: Int = 0
    var noteId: Int = -1
    var noteTemplateId: Int = -1
    var stackIds: IntArray? = null
    var stackAmounts: IntArray? = null
    var teamId: Int = 0
    var lendId: Int = -1
    var lendTemplateId: Int = -1
    var recolourId: Int = -1
    var recolourTemplateId: Int = -1
    var equipId: Int = -1
    var itemRequirements: HashMap<Int, Int>? = null
    var clientScriptData: HashMap<Int, Any?>? = null
    fun getParam(key: Int, defaultValue: Int): Int = clientScriptData?.get(key) as? Int ?: defaultValue
    fun getParam(key: Int, defaultValue: String?): String? = clientScriptData?.get(key) as? String ?: defaultValue
    var dummyItemType: Int = 0
    val isAllowed: Boolean
        get() {
            var name = name.lowercase(Locale.getDefault())
            var id = id
            if (permittedItems.contains(id)) return true
            if (bannedItems.contains(id)) return false
            if (allowedNames.contains(name)) return true
            for (keyword in excludedKeywords) {
                if (name.contains(keyword)) return false
            }
            if (equipSlot(13) == EquipmentSlot.AMMO) return true
            for (substring in allowedSubstrings) {
                if (name.contains(substring)) return true
            }
            if (name.startsWith("ring") || name.startsWith("amulet")) return true
            var bonuses = getConfiguration<IntArray?>(ItemConfigParser.BONUS)
            if (bonuses != null) {
                for (bonus in bonuses) {
                    if (bonus != 0) return false
                }
            }
            return true
        }
    val renderAnimationId: Int
        get() = getConfiguration<Int>(ItemConfigParser.RENDER_ANIM_ID, 1426)
    val isPlayerType: Boolean
        get() = dummyItemType == 0
    var isStackable: Boolean
        get() = stackable || !this.isUnnoted
        set(stackable) {
            this.stackable = stackable
        }
    val maxValue: Int
        get() = (cost * 1.05).toInt().coerceAtLeast(1)
    val minValue: Int
        get() = (cost * .95).toInt().coerceAtLeast(1)
    var inventoryOptions: Array<String?>?
        get() = options
        set(inventoryOptions) {
            this.options = inventoryOptions ?: emptyArray()
        }
    val isAlchemizable: Boolean
        get() = getConfiguration(ItemConfigParser.ALCHEMIZABLE, false)

    val isTradeable: Boolean
        get() {
            if (hasDestroyAction() && !name.contains("impling jar")) {
                return false
            }
            if ((!getConfiguration<Boolean>(ItemConfigParser.TRADEABLE, false))) {
                return false
            }
            return true
        }
    val questId: Int
        get() {
            if (clientScriptData == null) {
                return -1
            }
            var questId = clientScriptData!!.get(861)
            if (questId != null && questId is Int) {
                return (questId as Int?)!!
            }
            return -1
        }
    val archiveId: Int
        get() = id.ushr(8)
    val fileId: Int
        get() = 0xff and id
    var itemPlugin: ItemPlugin?
        get() = getConfiguration<ItemPlugin?>("wrapper", null)
        set(plugin) {
            handlers.put("wrapper", plugin)
        }
    override val examine: String
        get() {
            val text = super.examine!!
            if (!isUnnoted) examineText = "Swap this note at any bank for the equivalent item."
            return examineText ?: text
        }

    init {
        options = arrayOf<String?>(null, null, null, null, "drop")
    }

    fun transferNoteDefinition(reference: ObjType?, templateReference: ObjType?) {
        isMembersOnly = reference!!.isMembersOnly
        interfaceModelId = templateReference!!.interfaceModelId
        originalModelColors = templateReference.originalModelColors
        name = reference.name
        modelOffset2 = templateReference.modelOffset2
        retextureSource = templateReference.retextureSource
        cost = reference.cost
        yAngle2d = templateReference.yAngle2d
        stackable = true
        isUnnoted = false
        modifiedModelColors = templateReference.modifiedModelColors
        xAngle2d = templateReference.xAngle2d
        zoom2d = templateReference.zoom2d
        retextureSource = templateReference.retextureSource
        handlers.put(ItemConfigParser.TRADEABLE, true)
    }

    fun transferLendDefinition(reference: ObjType?, templateReference: ObjType?) {
        femaleWornModelId1 = reference!!.femaleWornModelId1
        maleWornModelId2 = reference.maleWornModelId2
        isMembersOnly = reference.isMembersOnly
        interfaceModelId = templateReference!!.interfaceModelId
        retextureDestination = reference.retextureDestination
        groundOptions = reference.groundOptions
        recolourPaletteIndices = reference.recolourPaletteIndices
        xAngle2d = templateReference.xAngle2d
        yAngle2d = templateReference.yAngle2d
        originalModelColors = reference.originalModelColors
        name = reference.name
        maleWornModelId1 = reference.maleWornModelId1
        maleWornModelId3 = reference.maleWornModelId3
        teamId = reference.teamId
        modelOffset2 = templateReference.modelOffset2
        clientScriptData = reference.clientScriptData
        modifiedModelColors = reference.modifiedModelColors
        femaleWornModelId3 = reference.femaleWornModelId3
        modelOffset1 = templateReference.modelOffset1
        retextureSource = reference.retextureSource
        cost = 0
        zoom2d = templateReference.zoom2d
        options = arrayOfNulls<String>(5)
        femaleWornModelId2 = reference.femaleWornModelId2


            options = reference.options.clone()

    }

    fun transferRecolourDefinition(reference: ObjType?, templateReference: ObjType?) {
        femaleWornModelId2 = reference!!.femaleWornModelId2
        options = arrayOfNulls<String>(5)
        yAngle2d = templateReference!!.yAngle2d
        name = reference.name
        maleWornModelId1 = reference.maleWornModelId1
        modelOffset2 = templateReference.modelOffset2
        femaleWornModelId1 = reference.femaleWornModelId1
        maleWornModelId2 = reference.maleWornModelId2
        modelOffset1 = templateReference.modelOffset1
        recolourPaletteIndices = reference.recolourPaletteIndices
        stackable = reference.stackable
        xAngle2d = templateReference.xAngle2d
        retextureSource = reference.retextureSource
        maleWornModelId3 = reference.maleWornModelId3
        retextureDestination = reference.retextureDestination
        modifiedModelColors = reference.modifiedModelColors
        zoom2d = templateReference.zoom2d
        femaleWornModelId3 = reference.femaleWornModelId3
        teamId = reference.teamId
        cost = 0
        groundOptions = reference.groundOptions
        originalModelColors = reference.originalModelColors
        isMembersOnly = reference.isMembersOnly
        clientScriptData = reference.clientScriptData
        interfaceModelId = templateReference.interfaceModelId


            options = reference.options.clone()

    }

    fun hasRequirement(player: Player?, wield: Boolean, message: Boolean): Boolean {
        var requirements = getConfiguration<MutableMap<Int, Int>?>(ItemConfigParser.REQUIREMENTS)
        if (requirements == null) {
            return true
        }
        for (skill in requirements.keys) {
            if (skill < 0 || skill >= Skills.SKILL_NAME.size) {
                continue
            }
            var level = requirements.get(skill)!!
            if (player!!.getSkills()!!.getStaticLevel(skill) < level) {
                if (message) {
                    var name = Skills.SKILL_NAME[skill]
                    player.getPacketDispatch()!!.sendMessage("You need a" + (if (StringUtils.isPlusN(name)) "n " else " ") + name + " level of " + level + " to " + (if (wield) "wear " else "use ") + "this.")
                }
                return false
            }
        }
        return true
    }

    fun getRequirement(skillId: Int): Int {
        var requirements = getConfiguration<MutableMap<Int, Int>?>(ItemConfigParser.REQUIREMENTS)
        if (requirements == null) {
            return 0
        }
        var level = requirements.get(skillId)
        return if (level == null) 0 else level
    }

    fun hasShopCurrencyValue(currency: String?): Boolean = (handlers as MutableMap<String, Any?>?)!!.getOrDefault(currency, "0") !== "0"

    fun hasShopCurrencyValue(currency: Int): Boolean {
        when (currency) {
            Items.COINS_995 -> return isTradeable
            Items.TOKKUL_6529 -> return hasShopCurrencyValue(ItemConfigParser.TOKKUL_PRICE)
            Items.ARCHERY_TICKET_1464 -> return hasShopCurrencyValue(ItemConfigParser.ARCHERY_TICKET_PRICE)
            Items.CASTLE_WARS_TICKET_4067 -> return hasShopCurrencyValue(ItemConfigParser.CASTLE_WARS_TICKET_PRICE)
            else -> return false
        }
    }

    fun getAlchemyValue(highAlchemy: Boolean): Int {
        if (!isUnnoted && noteId > -1) {
            return forId(noteId).getAlchemyValue(highAlchemy)
        }
        if (highAlchemy) {
            return getConfiguration<Int>(ItemConfigParser.HIGH_ALCHEMY, Math.rint(value * 0.6).toInt())
        }
        return getConfiguration<Int>(ItemConfigParser.LOW_ALCHEMY, Math.rint(value * 0.4).toInt())
    }

    fun hasAction(optionName: String?): Boolean = options.any { it?.equals(optionName, ignoreCase = true) == true }

    fun hasDestroyAction(): Boolean = hasAction("destroy") || hasAction("dissolve")

    fun hasWearAction(): Boolean = options.any { option ->
        option.equals("wield", ignoreCase = true) || option.equals("wear", ignoreCase = true) || option.equals("equip", ignoreCase = true)
    }

    fun hasSpecialBar(): Boolean = (clientScriptData?.get(687) as? Int ?: 0) != 0

    fun hasPlugin(): Boolean = itemPlugin != null

    companion object {
        private val DEFINITIONS: MutableMap<Int, ObjType> = HashMap<Int, ObjType>()
        private val OPTION_HANDLERS: MutableMap<String, OptionHandler?> = HashMap<String, OptionHandler?>()

        @JvmStatic
        fun parse() {
            for (itemId in 0 until Cache.getItemDefinitionsSize()) {
                val data = Cache.getIndexes()[19]!!.getFileData(itemId.ushr(8), itemId and 0xFF, null)
                if (data == null) {
                    ObjType.getDefinitions().put(itemId, ObjType())
                    continue
                }
                var def = ObjType.parseDefinition(itemId, ByteBuffer.wrap(data))

                if (itemId == 14958)
                    def.isStackable = true
                ObjType.getDefinitions().put(itemId, def)
            }
            ObjType.defineTemplates()
        }

        @JvmStatic
        fun forId(itemId: Int): ObjType {
            var def = DEFINITIONS.get(itemId)
            if (def != null) {
                return def
            }
            return ObjType()
        }

        @JvmStatic
        fun parseDefinition(itemId: Int, buffer: ByteBuffer): ObjType {
            var def = ObjType()
            def.id = itemId
            while (true) {
                var opcode = buffer.get().toInt() and 0xFF
                if (opcode == 0) {
                    break
                } else if (opcode == 1) {
                    def.interfaceModelId = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 2) {
                    def.name = ByteBufferUtils.getString(buffer)
                } else if (opcode == 3) {
                    def.handlers.put("examine", ByteBufferUtils.getString(buffer))
                } else if (opcode == 4) {
                    def.zoom2d = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 5) {
                    def.xAngle2d = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 6) {
                    def.yAngle2d = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 7) {
                    def.modelOffset1 = buffer.getShort().toInt() and 0xFFFF
                    if (def.modelOffset1 > 32767)
                        def.modelOffset1 -= 65536
                } else if (opcode == 8) {
                    def.modelOffset2 = buffer.getShort().toInt() and 0xFFFF
                    if (def.modelOffset2 > 32767) {
                        def.modelOffset2 -= 65536
                    }
                } else if (opcode == 10)
                {} else if (opcode == 11) {
                    def.stackable = true
                } else if (opcode == 12) {
                    def.cost = ((buffer.get().toInt() and 0xFF) shl 24) + ((buffer.get().toInt() and 0xFF) shl 16) + ((buffer.get().toInt() and 0xFF) shl 8) + (buffer.get().toInt() and 0xFF)
                } else if (opcode == 16) {
                    def.isMembersOnly = true
                } else if (opcode == 23) {
                    def.maleWornModelId1 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 24) {
                    def.maleWornModelId2 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 25) {
                    def.femaleWornModelId1 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 26) {
                    def.femaleWornModelId2 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode >= 30 && opcode < 35) {
                    def.groundOptions!![opcode - 30] = ByteBufferUtils.getString(buffer)
                } else if (opcode >= 35 && opcode < 40) {
                    def.options[opcode - 35] = ByteBufferUtils.getString(buffer)
                } else if (opcode == 40) {
                    var length = buffer.get().toInt() and 0xFF
                    def.originalModelColors = ShortArray(length)
                    def.modifiedModelColors = ShortArray(length)
                    for (index in 0 until length) {
                        def.originalModelColors!![index] = buffer.getShort()
                        def.modifiedModelColors!![index] = buffer.getShort()
                    }
                } else if (opcode == 41) {
                    var length = buffer.get().toInt() and 0xFF
                    def.retextureSource = ShortArray(length)
                    def.retextureDestination = ShortArray(length)
                    for (index in 0 until length) {
                        def.retextureSource!![index] = buffer.getShort()
                        def.retextureDestination!![index] = buffer.getShort()
                    }
                } else if (opcode == 42) {
                    var length = buffer.get().toInt() and 0xFF
                    def.recolourPaletteIndices = ByteArray(length)
                    for (index in 0 until length)
                        def.recolourPaletteIndices!![index] = buffer.get()
                } else if (opcode == 65) {
                    def.cacheTradeable = true
                } else if (opcode == 78) {
                    def.maleWornModelId3 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 79) {
                    def.femaleWornModelId3 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 90) {
                    def.maleHeadModelId1 = buffer.getShort().toInt() and 0xffff
                } else if (opcode == 91) {
                    def.femaleHeadModelId1 = buffer.getShort().toInt() and 0xffff
                } else if (opcode == 92) {
                    def.maleHeadModelId2 = buffer.getShort().toInt() and 0xffff
                } else if (opcode == 93) {
                    def.femaleHeadModelId2 = buffer.getShort().toInt() and 0xffff
                } else if (opcode == 95) {
                    def.modelRotationZ = buffer.short.toInt() and 0xffff
                } else if (opcode == 96) {
                    def.dummyItemType = buffer.get().toInt() and 0xff
                } else if (opcode == 97) {
                    def.noteId = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 98) {
                    def.noteTemplateId = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode >= 100 && opcode < 110) {
                    if (def.stackIds == null) {
                        def.stackIds = IntArray(10)
                        def.stackAmounts = IntArray(10)
                    }
                    def.stackIds!![opcode - 100] = buffer.getShort().toInt() and 0xFFFF
                    def.stackAmounts!![opcode - 100] = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 110) {
                    buffer.getShort()
                } else if (opcode == 111) {
                    buffer.getShort()
                } else if (opcode == 112) {
                    buffer.getShort()
                } else if (opcode == 113) {
                    buffer.get()
                } else if (opcode == 114) {
                    buffer.get()
                } else if (opcode == 115) {
                    def.teamId = buffer.get().toInt() and 0xff
                } else if (opcode == 121) {
                    def.lendId = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 122) {
                    def.lendTemplateId = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 125) {
                    buffer.get()
                    buffer.get()
                    buffer.get()
                } else if (opcode == 126) {
                    buffer.get()
                    buffer.get()
                    buffer.get()
                } else if (opcode == 127) {
                    val option = buffer.get().toInt() and 0xff
                    def.groundCursorOptions[option] = buffer.short.toInt() and 0xffff
                } else if (opcode == 128) {
                    val option = buffer.get().toInt() and 0xff
                    def.groundCursorOptions[option] = buffer.short.toInt() and 0xffff
                } else if (opcode == 129) {
                    val option = buffer.get().toInt() and 0xff
                    def.inventoryCursorOptions[option] = buffer.short.toInt() and 0xffff
                } else if (opcode == 130) {
                    val option = buffer.get().toInt() and 0xff
                    def.inventoryCursorOptions[option] = buffer.short.toInt() and 0xffff
                } else if (opcode == 132) {
                    def.questIds = IntArray(buffer.get().toInt() and 0xff) { buffer.short.toInt() and 0xffff }
                } else if (opcode == 249) {
                    var length = buffer.get().toInt() and 0xFF
                    if (def.clientScriptData == null) {
                        def.clientScriptData = HashMap<Int, Any?>()
                    }
                    for (index in 0 until length) {
                        var string = (buffer.get().toInt() and 0xFF) == 1
                        var key = ByteBufferUtils.getMedium(buffer)
                        var value = if (string) ByteBufferUtils.getString(buffer) else buffer.getInt()
                        def.clientScriptData!!.put(key, value)
                    }
                } else {
                    error("Item $itemId: unknown opcode $opcode at ${buffer.position() - 1}")
                }
            }
            return def
        }

        @JvmStatic
        fun defineTemplates() {
            var equipId = 0
            for (i in 0 until Cache.getItemDefinitionsSize()) {
                var def = forId(i)
                if (def.noteTemplateId != -1) {
                    def.transferNoteDefinition(forId(def.noteId), forId(def.noteTemplateId))
                }
                if (def.lendTemplateId != -1) {
                    def.transferLendDefinition(forId(def.lendId), forId(def.lendTemplateId))
                }
                if (def.recolourTemplateId != -1) {
                    def.transferRecolourDefinition(forId(def.recolourId), forId(def.recolourTemplateId))
                }
            }
            // Equipment.init() in the 578 client enumerates resolved definitions in ID order.
            for (i in 0 until Cache.getItemDefinitionsSize()) {
                val def = DEFINITIONS[i] ?: continue
                def.equipId = if (def.maleWornModelId1 >= 0 || def.femaleWornModelId1 >= 0) equipId++ else -1
            }
        }

        @JvmStatic
        fun canEnterEntrana(player: Player?): Boolean {
            var containers: Array<Container?>?
            if (player!!.getFamiliarManager()!!.hasFamiliar() && player.getFamiliarManager()!!.getFamiliar()!!.isBurdenBeast()) {
                containers = arrayOf<Container?>(player.getInventory(), player.getEquipment(), (player.getFamiliarManager()!!.getFamiliar() as BurdenBeast).container)
            } else {
                containers = arrayOf<Container?>(player.getInventory(), player.getEquipment())
            }
            for (c in containers) {
                for (i in c!!.toArray()!!) {
                    if (i == null) {
                        continue
                    }
                    if (!i.definition.isAllowed) {
                        return false
                    }
                }
            }
            return true
        }
        private val permittedItems: HashSet<Int> = HashSet(Arrays.asList<Int>(
            Items.COMBAT_BRACELET1_11124,
            Items.COMBAT_BRACELET2_11122,
            Items.COMBAT_BRACELET3_11120,
            Items.COMBAT_BRACELET4_11118,
            Items.REGEN_BRACELET_11133,
            Items.BOOTS_OF_LIGHTNESS_88,
            Items.CLIMBING_BOOTS_3105,
            Items.BLUE_BERET_2633,
            Items.BLACK_BERET_2635,
            Items.WHITE_BERET_2637,
            Items.BROOMSTICK_14057,
            Items.SPOTTED_CAPE_10069,
            Items.SPOTTIER_CAPE_10071,
            Items.SARADOMIN_CAPE_2412,
            Items.ZAMORAK_CAPE_2414,
            Items.GUTHIX_CAPE_2413,
            Items.SARADOMIN_CLOAK_10446,
            Items.ZAMORAK_CLOAK_10450,
            Items.GUTHIX_CLOAK_10448,
            Items.TAN_CAVALIER_2639,
            Items.BLACK_CAVALIER_2643,
            Items.DARK_CAVALIER_2641,
            Items.DAVY_KEBBIT_HAT_12568,
            Items.FIXED_DEVICE_6082,
            Items.FLARED_TROUSERS_10394,
            Items.GIANTS_HAND_13666,
            Items.GNOME_SCARF_9470,
            Items.HOLY_BOOK_3840,
            Items.DAMAGED_BOOK_3839,
            Items.UNHOLY_BOOK_3842,
            Items.DAMAGED_BOOK_3841,
            Items.BOOK_OF_BALANCE_3844,
            Items.DAMAGED_BOOK_3843,
            Items.HAM_SHIRT_4298,
            Items.HAM_ROBE_4300,
            Items.HAM_HOOD_4302,
            Items.HAM_CLOAK_4304,
            Items.HAM_LOGO_4306,
            Items.GLOVES_4308,
            Items.BOOTS_4310,
            Items.ICE_GLOVES_1580,
            Items.MIND_HELMET_9733,
            Items.MONKS_ROBE_542,
            Items.MONKS_ROBE_544,
            Items.PRIEST_GOWN_426,
            Items.PRIEST_GOWN_428,
            Items.SHADE_ROBE_546,
            Items.SHADE_ROBE_548,
            Items.ZAMORAK_ROBE_1033,
            Items.ZAMORAK_ROBE_1035,
            Items.NURSE_HAT_6548,
            Items.OMNI_TIARA_13655,
            Items.PENANCE_GLOVES_10553,
            Items.PET_ROCK_3695,
            Items.SALVE_AMULETE_10588,
            Items.SKELETAL_BOOTS_6147,
            Items.SKELETAL_GLOVES_6153,
            Items.SKIRT_5048,
            Items.WARLOCK_TOP_14076,
            Items.WARLOCK_LEGS_14077,
            Items.WARLOCK_CLOAK_14081,
            Items.WIZARD_BOOTS_2579,
            Items.BONES_TO_BANANAS_8014,
            Items.BONES_TO_PEACHES_8015,
            Items.ENCHANT_SAPPHIRE_8016,
            Items.ENCHANT_EMERALD_8017,
            Items.ENCHANT_RUBY_8018,
            Items.ENCHANT_DIAMOND_8019,
            Items.ENCHANT_DRAGONSTN_8020,
            Items.ENCHANT_ONYX_8021,
            Items.ECTOPHIAL_4251
        ))
        private val bannedItems: HashSet<Int> = HashSet(Arrays.asList<Int>(
            Items.CAPE_OF_LEGENDS_1052,
            Items.FIRE_CAPE_6570,
            Items.AVAS_ATTRACTOR_10498,
            Items.AVAS_ACCUMULATOR_10499,
            Items.COOKING_GAUNTLETS_775,
            Items.CHAOS_GAUNTLETS_777,
            Items.GOLDSMITH_GAUNTLETS_776,
            Items.SILVER_SICKLE_2961,
            Items.SILVER_SICKLEB_2963,
            Items.SILVER_SICKLE_EMERALDB_13155,
            Items.KARAMJA_GLOVES_1_11136,
            Items.KARAMJA_GLOVES_2_11138,
            Items.KARAMJA_GLOVES_3_11140,
            Items.EXPLORERS_RING_1_13560,
            Items.EXPLORERS_RING_2_13561,
            Items.EXPLORERS_RING_3_13562,
            Items.FANCY_BOOTS_9005,
            Items.FIGHTING_BOOTS_9006,
            Items.VYREWATCH_TOP_9634,
            Items.VYREWATCH_LEGS_9636,
            Items.VYREWATCH_SHOES_9638,
            Items.BARB_TAIL_HARPOON_10129,
            Items.BUTTERFLY_NET_10010,
            Items.MIME_MASK_3057,
            Items.DWARF_CANNON_SET_11967,
            Items.CANNON_BARRELS_10,
            Items.CANNON_BASE_6,
            Items.CANNON_STAND_8,
            Items.CANNON_FURNACE_12,
            Items.HOLY_WATER_732,
            Items.ENCHANTED_WATER_TIARA_11969,
            Items.OMNI_TALISMAN_STAFF_13642,
            Items.FREMENNIK_SEA_BOOTS_1_14571,
            Items.FREMENNIK_SEA_BOOTS_2_14572,
            Items.FREMENNIK_SEA_BOOTS_3_14573,
            Items.ROBIN_HOOD_HAT_2581,
            Items.SAFETY_GLOVES_12629,
            Items.FALADOR_SHIELD_1_14577,
            Items.FALADOR_SHIELD_2_14578,
            Items.FALADOR_SHIELD_3_14579,
            Items.PROSYTE_HARNESS_M_9666,
            Items.INITIATE_HARNESS_M_9668,
            Items.PROSYTE_HARNESS_F_9670
        ))
        private val allowedNames: Set<String> = setOf<String>(
            "trousers", "tribal top", "woven top", "chompy bird hat", "cape"
        )
        private val excludedKeywords: List<String> = Arrays.asList<String>(
            "(class", "camo ", "larupia", "kyatt", " stole", "moonclan", "villager ", "tribal", "spirit ", "gauntlets"
        )
        private val allowedSubstrings: List<String> = Arrays.asList<String>(
            "satchel", "naval", " partyhat"
        )
        @JvmStatic
        fun getDefinitions(): MutableMap<Int, ObjType> = DEFINITIONS

        @JvmStatic
        fun getOptionHandler(nodeId: Int, name: String?): OptionHandler? =
            forId(nodeId).getConfiguration<OptionHandler>("option:$name") ?: OPTION_HANDLERS[name]


        @JvmStatic
        fun setOptionHandler(name: String, handler: OptionHandler?): Boolean = OPTION_HANDLERS.put(name, handler) != null
        @JvmStatic
        fun getOptionHandlers(): MutableMap<String, OptionHandler?> = OPTION_HANDLERS
    }
}
