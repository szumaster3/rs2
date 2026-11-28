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

class NPCDefinition(id: Int): Definition<NPC>() {
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
    @JvmField var anInt833: Int = -1
    @JvmField var anInt836: Int = -1
    @JvmField var anInt837: Int = -1
    @JvmField var aBoolean841: Boolean = true
    @JvmField var anInt842: Int = -1
    @JvmField var configFileId: Int = -1
    @JvmField var childNPCIds: IntArray? = null
    @JvmField var anInt846: Int = -1
    @JvmField var anInt850: Int = 255
    @JvmField var aByte851: Byte = (-96).toByte()
    @JvmField var aBoolean852: Boolean = true
    @JvmField var anInt853: Int = 32
    @JvmField var aByte854: Byte = (-16).toByte()
    @JvmField var aBoolean856: Boolean = false
    @JvmField var aBoolean857: Boolean = true
    @JvmField var aShortArray859: ShortArray? = null
    @JvmField var aByteArray861: ByteArray? = null
    @JvmField var aShort862: Short = 0.toShort()
    @JvmField var aBoolean863: Boolean = false
    @JvmField var anInt864: Int = 128
    @JvmField var aShortArray866: ShortArray? = null
    @JvmField var anIntArray868: IntArray? = IntArray(0)
    @JvmField var anInt869: Int = 0
    @JvmField var anInt870: Int = -1
    @JvmField var anInt871: Int = -1
    @JvmField var anInt872: Int = -1
    @JvmField var anInt874: Int = -1
    @JvmField var anInt875: Int = 0
    @JvmField var anInt876: Int = -1
    @JvmField var anInt879: Int = -1
    @JvmField var aShortArray880: ShortArray? = null
    @JvmField var anInt884: Int = -1
    @JvmField var configId: Int = -1
    @JvmField var anInt889: Int = -1
    @JvmField var anIntArray892: IntArray? = null
    @JvmField var aShort894: Short = 0.toShort()
    @JvmField var aShortArray896: ShortArray? = null
    @JvmField var anInt897: Int = 0
    @JvmField var anInt899: Int = 128
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
                return VarbitDefinition.forNPCId(configFileId).startBit
            }
            return -1
        }
    val varbitSize: Int
        get() {
            if (configFileId != -1) {
                return VarbitDefinition.forNPCId(configFileId).endBit - VarbitDefinition.forNPCId(configFileId).startBit
            }
            return -1
        }

    init {
        this.id = id
        name = "null"
        options = arrayOfNulls<String>(5)
    }

    fun getChildNPC(player: Player?): NPCDefinition {
        if (childNPCIds == null || childNPCIds!!.size < 1) {
            return this
        }
        var configValue = -1
        if (player != null) {
            if (configFileId != -1) {
                configValue = VarbitDefinition.forNPCId(configFileId).getValue(player)
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
                anIntArray868 = IntArray(length)
                for (i_66_ in 0 until length) {
                    anIntArray868!![i_66_] = buffer.getShort().toInt() and 0xFFFF
                    if (anIntArray868!![i_66_] == 65535) anIntArray868!![i_66_] = -1
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
                aShortArray859 = ShortArray(length)
                aShortArray896 = ShortArray(length)
                var i_65_ = 0
                while (i_65_ < length) {
                    aShortArray896!![i_65_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    aShortArray859!![i_65_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    i_65_++
                }
            }
            41 -> {
                val length = buffer.get().toInt() and 0xFF
                aShortArray880 = ShortArray(length)
                aShortArray866 = ShortArray(length)
                var i_54_ = 0
                while (i_54_ < length) {
                    aShortArray880!![i_54_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    aShortArray866!![i_54_] = (buffer.getShort().toInt() and 0xFFFF).toShort()
                    i_54_++
                }
            }
            42 -> {
                val length = buffer.get().toInt() and 0xFF
                aByteArray861 = ByteArray(length)
                var i_55_ = 0
                while (length > i_55_) {
                    aByteArray861!![i_55_] = buffer.get().toByte()
                    i_55_++
                }
            }
            60 -> {
                val length = buffer.get().toInt() and 0xFF
                anIntArray892 = IntArray(length)
                var i_64_ = 0
                while (i_64_ < length) {
                    anIntArray892!![i_64_] = buffer.getShort().toInt() and 0xFFFF
                    i_64_++
                }
            }
            93 -> isVisibleOnMap = false
            95 -> combatLevel = buffer.getShort().toInt() and 0xFFFF
            97 -> anInt864 = buffer.getShort().toInt() and 0xFFFF
            98 -> anInt899 = buffer.getShort().toInt() and 0xFFFF
            99 -> aBoolean863 = true
            100 -> anInt869 = buffer.get().toInt()
            101 -> anInt897 = buffer.get() * 5
            102 -> headIcons = buffer.getShort().toInt() and 0xFFFF
            103 -> anInt853 = buffer.getShort().toInt() and 0xFFFF
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
            107 -> aBoolean841 = false
            109 -> aBoolean852 = false
            111 -> aBoolean857 = false
            113 -> {
                aShort862 = (buffer.getShort().toInt() and 0xFFFF).toShort()
                aShort894 = (buffer.getShort().toInt() and 0xFFFF).toShort()
            }
            114 -> {
                aByte851 = (buffer.get()).toByte()
                aByte854 = (buffer.get()).toByte()
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

    fun isaBoolean841(): Boolean = aBoolean841

    fun getaByte851(): Byte = aByte851

    fun isaBoolean852(): Boolean = aBoolean852

    fun getaByte854(): Byte = aByte854

    fun isaBoolean856(): Boolean = aBoolean856

    fun isaBoolean857(): Boolean = aBoolean857

    fun getaShortArray859(): ShortArray? = aShortArray859

    fun getaByteArray861(): ByteArray? = aByteArray861

    fun getaShort862(): Short = aShort862

    fun isaBoolean863(): Boolean = aBoolean863

    fun getaShortArray866(): ShortArray? = aShortArray866

    fun getaShortArray880(): ShortArray? = aShortArray880

    fun getConfigId(): Int {
        if (configFileId != -1) {
            return VarbitDefinition.forNPCId(configFileId).varpId
        } else
        return configFileId
    }

    fun getaShort894(): Short = aShort894

    fun getaShortArray896(): ShortArray? = aShortArray896

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

    fun getAnInt853(): Int = anInt853

    fun getAnInt899(): Int = anInt899

    fun getAnInt901(): Int = anInt901

    fun getAnInt872(): Int = anInt872

    fun getAnIntArray868(): IntArray? = anIntArray868

    fun getAnInt876(): Int = anInt876

    fun getAnInt850(): Int = anInt850

    fun getAnInt897(): Int = anInt897

    fun getAnIntArray892(): IntArray? = anIntArray892

    fun getStandAnimation(): Int = standAnimation

    fun getAnInt846(): Int = anInt846

    fun getAnInt864(): Int = anInt864

    fun getAnInt869(): Int = anInt869

    fun getAnInt874(): Int = anInt874

    fun getAnInt889(): Int = anInt889

    fun getAnInt871(): Int = anInt871

    fun getAnInt870(): Int = anInt870

    fun getAnInt879(): Int = anInt879

    fun getWalkAnimation(): Int = walkAnimation

    fun setRenderAnimationId(value: Int) { renderAnimationId = value }

    companion object {
        private val DEFINITIONS: MutableMap<Int, NPCDefinition> = HashMap<Int, NPCDefinition>()
        private val OPTION_HANDLERS: MutableMap<String, OptionHandler?> = HashMap<String, OptionHandler?>()

        @JvmStatic
        fun forId(id: Int): NPCDefinition {
            var def = DEFINITIONS.get(id)
            if (def == null) {
                def = NPCDefinition(id)
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
        fun getDefinitions(): MutableMap<Int, NPCDefinition> = DEFINITIONS
    }
}
