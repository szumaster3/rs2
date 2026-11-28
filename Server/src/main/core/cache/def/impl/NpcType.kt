package core.cache.def.impl

import core.api.getVarp
import core.cache.Cache
import core.cache.def.Definition
import core.cache.misc.buffer.ByteBufferUtils
import core.game.interaction.OptionHandler
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.drop.NPCDropTables
import core.game.node.entity.player.Player
import core.game.system.config.NPCConfigParser
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.tools.StringUtils
import java.nio.ByteBuffer
import java.util.HashMap

class NpcType(id: Int): Definition<NPC>() {
    private var cacheParams: MutableMap<Int, Any>? = null

    fun getParam(key: Int, defaultValue: Int): Int = cacheParams?.get(key) as? Int ?: defaultValue
    fun getParam(key: Int, defaultValue: String?): String? = cacheParams?.get(key) as? String ?: defaultValue
    fun getCacheParams(): Map<Int, Any> = cacheParams?.toMap() ?: emptyMap()

    var optionCursors: MutableMap<Int, Int> = HashMap()
        private set
    var attackCursorId: Int = -1
        private set
    var hitBarSpriteId: Int = -1
        private set
    var timerBarSpriteId: Int = -1
        private set
    var mapElementId: Int = -1
        private set
    var questIds: IntArray? = null
        private set
    @JvmField var size: Int = 1
    var combatLevel: Int = 0
    @JvmField var headIcons: Int = -1
    @JvmField var isVisibleOnMap: Boolean = true
    var dropTables: NPCDropTables = NPCDropTables(this)
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt833: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt836: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt837: Int = -1
    @JvmField var opcode107Enabled: Boolean = true
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt842: Int = -1
    @JvmField var configFileId: Int = -1
    @JvmField var childNPCIds: IntArray? = null
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt846: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt850: Int = 255
    @JvmField var shadowTransparency1: Byte = (-96).toByte()
    @JvmField var opcode109Enabled: Boolean = true
    @JvmField var turnSpeed: Int = 32
    @JvmField var shadowTransparency2: Byte = (-16).toByte()
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var aBoolean856: Boolean = false
    @JvmField var characterShadowEnabled: Boolean = true
    @JvmField var recolourDestination: ShortArray? = null
    @JvmField var recolourPaletteIndices: ByteArray? = null
    @JvmField var shadowColour1: Short = 0.toShort()
    @JvmField var alwaysOnTop: Boolean = false
    @JvmField var resizeHorizontal: Int = 128
    @JvmField var retextureDestination: ShortArray? = null
    @JvmField var modelIds: IntArray? = IntArray(0)
    @JvmField var ambient: Int = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt870: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt871: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt872: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt874: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt875: Int = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt876: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt879: Int = -1
    @JvmField var retextureSource: ShortArray? = null
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt884: Int = -1
    @JvmField var configId: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt889: Int = -1
    @JvmField var headModelIds: IntArray? = null
    @JvmField var shadowColour2: Short = 0.toShort()
    @JvmField var recolourSource: ShortArray? = null
    @JvmField var contrast: Int = 0
    @JvmField var resizeVertical: Int = 128
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var anInt901: Int = -1
    @JvmField var standAnimation: Int = -1
    @JvmField var walkAnimation: Int = -1
    @JvmField var renderAnimationId: Int = 0
    var combatDistance: Int = 0
    var combatGraphics: Array<Graphics?> = arrayOfNulls<Graphics>(3)
    var turnAnimation: Int = 0
        private set
    var turn180Animation: Int = 0
        private set
    var turnCWAnimation: Int = 0
        private set
    var turnCCWAnimation: Int = 0
        private set
    override val examine: String?
        get() {
            val text = getConfiguration<String?>(NPCConfigParser.EXAMINE, examineText)
            if (text != null || name.length <= 1) return text
            return "It\'s a" + (if (StringUtils.isPlusN(name)) "n " else " ") + name + "."
        }
    val varbitOffset: Int
        get() {
            if (configFileId != -1) {
                return VarbitType.forNPCId(configFileId).startBit
            }
            return -1
        }
    val varbitSize: Int
        get() {
            if (configFileId != -1) {
                return VarbitType.forNPCId(configFileId).endBit - VarbitType.forNPCId(configFileId).startBit
            }
            return -1
        }

    init {
        this.id = id
        name = "null"
        options = arrayOfNulls<String>(5)
    }

    fun getChildNPC(player: Player?): NpcType {
        if (childNPCIds == null || childNPCIds!!.size < 1) {
            return this
        }
        var configValue = -1
        if (player != null) {
            if (configFileId != -1) {
                configValue = VarbitType.forNPCId(configFileId).getValue(player)
            } else if (configId != -1) {
                configValue = getVarp(player, configId)
            }
        } else {
            configValue = 0
        }
        if (configValue < 0 || configValue >= childNPCIds!!.size - 1 || childNPCIds!![configValue] == -1) {
            var objectId = childNPCIds!![childNPCIds!!.size - 1]
            if (objectId != -1) {
                return forId(objectId)
            }
            return this
        }
        return forId(childNPCIds!![configValue])
    }
    internal fun parse(buffer: ByteBuffer) {
        while (true) {
            var opcode = buffer.get().toInt() and 0xFF
            if (opcode == 0) {
                break
            }
            parseOpcode(buffer, opcode)
        }
    }
    private fun parseOpcode(buffer: ByteBuffer, opcode: Int) {
        when (opcode) {
            1 -> {
                val length = buffer.get().toInt() and 0xFF
                modelIds = IntArray(length)
                for (i_66_ in 0 until length) {
                    modelIds!![i_66_] = buffer.getShort().toInt() and 0xFFFF
                    if (modelIds!![i_66_] == 65535) modelIds!![i_66_] = -1
                }
            }
            2 -> name = ByteBufferUtils.getString(buffer)
            12 -> size = buffer.get().toInt() and 0xFF
            13 -> standAnimation = buffer.getShort().toInt()
            14 -> walkAnimation = buffer.getShort().toInt()
            15 -> turnAnimation = buffer.getShort().toInt()
            16 -> buffer.getShort()
            17 -> {
                walkAnimation = buffer.getShort().toInt()
                turn180Animation = buffer.getShort().toInt()
                turnCWAnimation = buffer.getShort().toInt()
                turnCCWAnimation = buffer.getShort().toInt()
            }
            30, 31, 32, 33, 34 -> options[opcode - 30] = ByteBufferUtils.getString(buffer)
            40 -> {
                val length = buffer.get().toInt() and 0xFF
                recolourDestination = ShortArray(length)
                recolourSource = ShortArray(length)
                var i_65_ = 0
                while (i_65_ < length) {
                    recolourSource!![i_65_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    recolourDestination!![i_65_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    i_65_++
                }
            }
            41 -> {
                val length = buffer.get().toInt() and 0xFF
                retextureSource = ShortArray(length)
                retextureDestination = ShortArray(length)
                var i_54_ = 0
                while (i_54_ < length) {
                    retextureSource!![i_54_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    retextureDestination!![i_54_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    i_54_++
                }
            }
            42 -> {
                val length = buffer.get().toInt() and 0xFF
                recolourPaletteIndices = ByteArray(length)
                var i_55_ = 0
                while (length > i_55_) {
                    recolourPaletteIndices!![i_55_] = buffer.get().toByte()
                    i_55_++
                }
            }
            60 -> {
                val length = buffer.get().toInt() and 0xFF
                headModelIds = IntArray(length)
                var i_64_ = 0
                while (i_64_ < length) {
                    headModelIds!![i_64_] = buffer.getShort().toInt() and 0xFFFF
                    i_64_++
                }
            }
            93 -> isVisibleOnMap = false
            95 -> combatLevel = buffer.getShort().toInt() and 0xFFFF
            97 -> resizeHorizontal = buffer.getShort().toInt() and 0xFFFF
            98 -> resizeVertical = buffer.getShort().toInt() and 0xFFFF
            99 -> alwaysOnTop = true
            100 -> ambient = buffer.get().toInt()
            101 -> contrast = buffer.get() * 5
            102 -> headIcons = buffer.getShort().toInt() and 0xFFFF
            103 -> turnSpeed = buffer.getShort().toInt() and 0xFFFF
            106, 118 -> {
                configFileId = buffer.getShort().toInt() and 0xFFFF
                if (configFileId == 65535) {
                    configFileId = -1
                }
                configId = buffer.getShort().toInt() and 0xFFFF
                if (configId == 65535) {
                    configId = -1
                }
                var defaultValue = -1
                if (opcode == 118) {
                    defaultValue = buffer.getShort().toInt() and 0xFFFF
                    if (defaultValue == 65535) {
                        defaultValue = -1
                    }
                }
                val length = buffer.get().toInt() and 0xFF
                childNPCIds = IntArray(2 + length)
                run({ var i = 0
                        while (length >= i) {
                            childNPCIds!![i] = buffer.getShort().toInt() and 0xFFFF
                            if (childNPCIds!![i] == 65535) {
                                childNPCIds!![i] = -1
                            }
                            i++
                        } })
                childNPCIds!![length + 1] = defaultValue
            }
            107 -> opcode107Enabled = false
            109 -> opcode109Enabled = false
            111 -> characterShadowEnabled = false
            113 -> {
                shadowColour1 = (buffer.getShort().toInt() and 0xFFFF).toShort()
                shadowColour2 = (buffer.getShort().toInt() and 0xFFFF).toShort()
            }
            114 -> {
                shadowTransparency1 = (buffer.get()).toByte()
                shadowTransparency2 = (buffer.get()).toByte()
            }
            115 -> {
                buffer.get()
                buffer.get()
            }
            119 -> buffer.get()
            121 -> {
                val length = buffer.get().toInt() and 0xFF
                for (i in 0 until length) {
                    buffer.get()
                    buffer.get()
                    buffer.get()
                    buffer.get()
                }
            }
            122 -> hitBarSpriteId = buffer.short.toInt() and 0xffff
            123 -> buffer.getShort()
            125 -> buffer.get()
            126 -> {
                buffer.getShort()
                renderAnimationId = buffer.getShort().toInt()
            }
            127 -> renderAnimationId = buffer.getShort().toInt()
            128 -> buffer.get()
            134 -> {
                buffer.getShort()
                buffer.getShort()
                buffer.getShort()
                buffer.getShort()
                buffer.get()
            }
            135, 136 -> {
                val option = buffer.get().toInt() and 0xff
                optionCursors[option] = buffer.short.toInt() and 0xffff
            }
            137 -> attackCursorId = buffer.short.toInt() and 0xffff
            138 -> buffer.short
            139 -> timerBarSpriteId = buffer.short.toInt() and 0xffff
            142 -> mapElementId = buffer.short.toInt() and 0xffff
            140 -> buffer.get()
            141, 143 -> Unit
            in 150..154 -> options[opcode - 150] = ByteBufferUtils.getString(buffer)
            160 -> questIds = IntArray(buffer.get().toInt() and 0xff) { buffer.short.toInt() and 0xffff }
            249 -> {
                cacheParams = DefinitionParams.read(buffer, cacheParams)
            }
            else -> error("NPC $id: unknown opcode $opcode at ${buffer.position() - 1}")
        }
    }

    fun hasAttackOption(): Boolean {
        for (option in options) {
            if (option != null && option.equals("attack", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun hasAction(optionName: String?): Boolean = options.any { it?.equals(optionName, ignoreCase = true) == true }

    fun initCombatGraphics(config: MutableMap<String, Any?>?) {
        if (config!!.containsKey(NPCConfigParser.START_GRAPHIC)) {
            combatGraphics[0] = Graphics((config.get(NPCConfigParser.START_GRAPHIC) as Int?)!!, getConfiguration<Int>(NPCConfigParser.START_HEIGHT, 0))
        }
        if (config.containsKey(NPCConfigParser.PROJECTILE)) {
            combatGraphics[1] = Graphics((config.get(NPCConfigParser.PROJECTILE) as Int?)!!, getConfiguration<Int>(NPCConfigParser.PROJECTILE_HEIGHT, 42))
        }
        if (config.containsKey(NPCConfigParser.END_GRAPHIC)) {
            combatGraphics[2] = Graphics((config.get(NPCConfigParser.END_GRAPHIC) as Int?)!!, getConfiguration<Int>(NPCConfigParser.END_HEIGHT, 96))
        }
    }

    fun getCombatAnimation(index: Int): Animation? {
        var name: String? = ""
        when (index) {
            0 -> name = NPCConfigParser.MELEE_ANIMATION
            1 -> name = NPCConfigParser.MAGIC_ANIMATION
            2 -> name = NPCConfigParser.RANGE_ANIMATION
            3 -> name = NPCConfigParser.DEFENCE_ANIMATION
            4 -> name = NPCConfigParser.DEATH_ANIMATION
            else -> {}
        }
        return getConfiguration<Animation?>(name ?: return null, null)
    }

    fun isOpcode107Enabled(): Boolean = opcode107Enabled

    fun getShadowTransparency1(): Byte = shadowTransparency1

    fun isOpcode109Enabled(): Boolean = opcode109Enabled

    fun getShadowTransparency2(): Byte = shadowTransparency2

    fun isaBoolean856(): Boolean = aBoolean856

    fun isCharacterShadowEnabled(): Boolean = characterShadowEnabled

    fun getRecolourDestination(): ShortArray? = recolourDestination

    fun getRecolourPaletteIndices(): ByteArray? = recolourPaletteIndices

    fun getShadowColour1(): Short = shadowColour1

    fun isAlwaysOnTop(): Boolean = alwaysOnTop

    fun getRetextureDestination(): ShortArray? = retextureDestination

    fun getRetextureSource(): ShortArray? = retextureSource

    fun getConfigId(): Int {
        if (configFileId != -1) {
            return VarbitType.forNPCId(configFileId).varpId
        } else
        return configFileId
    }

    fun getShadowColour2(): Short = shadowColour2

    fun getRecolourSource(): ShortArray? = recolourSource

    fun getSize(): Int = size

    fun getRenderAnimationId(): Int = renderAnimationId

    fun getAnInt833(): Int = anInt833

    fun getAnInt837(): Int = anInt837

    fun getChildNPCIds(): IntArray? = childNPCIds

    fun getHeadIcons(): Int = headIcons

    fun getAnInt842(): Int = anInt842

    fun getAnInt836(): Int = anInt836

    fun getConfigFileId(): Int = configFileId

    fun isVisibleOnMap(): Boolean = isVisibleOnMap

    fun getAnInt884(): Int = anInt884

    fun getAnInt875(): Int = anInt875

    fun getTurnSpeed(): Int = turnSpeed

    fun getResizeVertical(): Int = resizeVertical

    fun getAnInt901(): Int = anInt901

    fun getAnInt872(): Int = anInt872

    fun getModelIds(): IntArray? = modelIds

    fun getAnInt876(): Int = anInt876

    fun getAnInt850(): Int = anInt850

    fun getContrast(): Int = contrast

    fun getHeadModelIds(): IntArray? = headModelIds

    fun getStandAnimation(): Int = standAnimation

    fun getAnInt846(): Int = anInt846

    fun getResizeHorizontal(): Int = resizeHorizontal

    fun getAmbient(): Int = ambient

    fun getAnInt874(): Int = anInt874

    fun getAnInt889(): Int = anInt889

    fun getAnInt871(): Int = anInt871

    fun getAnInt870(): Int = anInt870

    fun getAnInt879(): Int = anInt879

    fun getWalkAnimation(): Int = walkAnimation

    fun setRenderAnimationId(value: Int) { renderAnimationId = value }

    companion object {
        private val DEFINITIONS: MutableMap<Int, NpcType> = HashMap<Int, NpcType>()
        private val OPTION_HANDLERS: MutableMap<String, OptionHandler?> = HashMap<String, OptionHandler?>()

        @JvmStatic
        fun forId(id: Int): NpcType {
            var def = DEFINITIONS.get(id)
            if (def == null) {
                def = NpcType(id)
                val data = Cache.getIndexes()[18]!!.getFileData(id.ushr(7), id and 0x7f, null)
                if (data != null) def.parse(ByteBuffer.wrap(data))
                DEFINITIONS.put(id, def)
            }
            return def
        }
        @JvmStatic
        fun getOptionHandler(nodeId: Int, name: String?): OptionHandler? =
            forId(nodeId).getConfiguration<OptionHandler>("option:$name") ?: OPTION_HANDLERS[name]


        @JvmStatic
        fun setOptionHandler(name: String, handler: OptionHandler?): Boolean = OPTION_HANDLERS.put(name, handler) != null
        @JvmStatic
        fun getOptionHandlers(): MutableMap<String, OptionHandler?> = OPTION_HANDLERS
        @JvmStatic
        fun getDefinitions(): MutableMap<Int, NpcType> = DEFINITIONS
    }
}
