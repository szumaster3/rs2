package core.cache.def.impl

import core.api.getVarp
import core.api.log
import core.cache.Cache
import core.cache.def.Definition
import core.cache.misc.buffer.ByteBufferUtils
import core.game.interaction.OptionHandler
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.tools.Log
import java.nio.ByteBuffer
import java.util.HashMap

class LocType : Definition<Scenery>() {
    private var cacheParams: MutableMap<Int, Any>? = null

    fun getParam(key: Int, defaultValue: Int): Int = cacheParams?.get(key) as? Int ?: defaultValue
    fun getParam(key: Int, defaultValue: String?): String? = cacheParams?.get(key) as? String ?: defaultValue
    fun getCacheParams(): Map<Int, Any> = cacheParams?.toMap() ?: emptyMap()

    override val examine: String get() = super.examine!!
    var originalColors: ShortArray? = null
        private set
    @JvmField var childrenIds: IntArray? = null
    var animationIds: IntArray? = null
        private set
    var animationWeights: IntArray? = null
        private set
    var optionCursors: MutableMap<Int, Int> = HashMap()
        private set
    var mapSceneId: Int = -1
        private set
    var mapElementId: Int = -1
        private set
    var questIds: IntArray? = null
        private set
    var modelIds: IntArray? = null
        private set
    var modelConfiguration: IntArray? = null
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anIntArray3833: IntArray? = null
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3834: Int = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3835: Int = -1
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private var aByte3837: Byte = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3838: Int = -1
        private set
    var mirrored: Boolean = false
    var contrast: Int = 0
        private set
    var resizeY: Int = 128
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3844: Int = -1
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3845: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private var aByte3847: Byte = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private var aByte3849: Byte = 0
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3850: Int = 0
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3851: Int = -1
        private set
    @JvmField var secondBool: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var aBoolean3853: Boolean = true
    var opcode75Value: Int = -1
        private set
    @JvmField var notClipped: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3857: Int = -1
        private set
    private var recolourPaletteIndices: ByteArray? = null
    var backgroundSoundIds: IntArray? = null
        private set
    var backgroundSoundId: Int = -1
        private set
    var varbitID: Int = -1
        private set
    var modifiedColors: ShortArray? = null
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3865: Int = 255
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3866: Boolean = false
    var shareLight: Boolean = false
    @JvmField var projectileClipped: Boolean = true
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anIntArray3869: IntArray? = null
    var opcode90Flag: Boolean = false
    @JvmField var sizeY: Int = 1
    var castsShadow: Boolean = true
    var membersOnly: Boolean = false
    @JvmField var boolean1: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3875: Int = 0
    @JvmField var animationId: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3877: Int = 0
    var ambient: Int = 0
        private set
    @JvmField var clipType: Int = 2
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3881: Int = 0
    var terrainContourValue: Int = -1
        private set
    var offsetX: Int = 0
        private set
    var loader: Any? = null
        private set
    var offsetY: Int = 0
        private set
    @JvmField var sizeX: Int = 1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    @JvmField var aBoolean3891: Boolean = false
    var wallWidth: Int = 64
        private set
    @JvmField var interactable: Int = -1
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3894: Boolean = false
    var randomAnimationFrame: Boolean = true
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3896: Int = 0
        private set
    var configId: Int = -1
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private val aByteArray3899: ByteArray? = null
    var backgroundSoundMinDelay: Int = 0
        private set
    var resizeX: Int = 128
        private set
    var backgroundSoundRange: Int = 0
        private set
    var backgroundSoundMaxDelay: Int = 0
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3906: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anIntArray3908: IntArray? = null
        private set
    private var terrainContourMode: Byte = 0.toByte()
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3913: Int = -1
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private var aByte3914: Byte = 0.toByte()
    var offsetZ: Int = 0
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anIntArrayArray3916: Array<IntArray?>? = null
    var resizeZ: Int = 128
        private set
    private var retextureDestination: ShortArray? = null
    private var retextureSource: ShortArray? = null
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var anInt3921: Int = 0
        private set
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    private val aClass194_3922: Any? = null
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3923: Boolean = false
    // Legacy unmapped field: no confirmed 578 cache opcode or semantic mapping.
    var aBoolean3924: Boolean = false
    var walkingFlag: Int = 0
        private set
    var isHasHiddenOptions: Boolean = false
    var mapIcon: Short = 0
        private set
    val configFile: VarbitType?
        get() {
            if (varbitID != -1) {
                return VarbitType.forSceneryId(varbitID)
            }
            return null
        }

    init {
        options = arrayOfNulls<String>(5)
        name = "null"
        walkingFlag = 0
        aBoolean3923 = false
        aBoolean3924 = false
        mapIcon = -1
    }
    internal fun configureObject() {
        if (interactable == -1) {
            interactable = 0
            if (modelIds != null && (modelConfiguration == null || modelConfiguration!![0] == 10)) {
                interactable = 1
            }
            for (i in 0..4) {
                if (options[i] != null) {
                    interactable = 1
                    break
                }
            }
        }
        if (childrenIds != null) {
            for (i in childrenIds!!.indices) {
                var def = forId(childrenIds!![i])
                def.varbitID = varbitID
            }
        }
        if (opcode75Value == -1) {
            opcode75Value = if (clipType == 0) 0 else 1
        }
        if (id == 31017) {
            sizeY = 2
            sizeX = sizeY
        }
        if (id == 29292) {
            projectileClipped = false
        }
        if (id == 28779) {
            clipType = 1
        }
    }

    fun hasActions(): Boolean {
        if (interactable > 0) {
            return true
        }
        if (childrenIds == null) {
            return hasOptions(false)
        }
        for (i in childrenIds!!.indices) {
            if (childrenIds!![i] != -1) {
                var def = forId(childrenIds!![i])
                if (def.hasOptions(false)) {
                    return true
                }
            }
        }
        return hasOptions(false)
    }

    fun getChildObject(player: Player?): LocType {
        if (childrenIds == null || childrenIds!!.size < 1) {
            return this
        }
        var configValue = -1
        if (player != null) {
            if (varbitID != -1) {
                configValue = VarbitType.forSceneryId(varbitID).getValue(player)
            } else if (configId != -1) {
                configValue = getVarp(player, configId)
            }
        } else {
            configValue = 0
        }
        var childDef = getChildObjectAtIndex(configValue)
        childDef.varbitID = this.varbitID
        return childDef
    }

    fun getChildObjectAtIndex(index: Int): LocType {
        if (childrenIds == null || childrenIds!!.size < 1) {
            return this
        }
        if (index < 0 || index >= childrenIds!!.size - 1 || childrenIds!![index] == -1) {
            var objectId = childrenIds!![childrenIds!!.size - 1]
            if (objectId != -1) {
                return forId(objectId)
            }
            return this
        }
        return forId(childrenIds!![index])
    }



    fun getaByte3837(): Byte = aByte3837

    fun isaBoolean3845(): Boolean = aBoolean3845

    fun getaByte3847(): Byte = aByte3847

    fun getaByte3849(): Byte = aByte3849

    fun isaBoolean3853(): Boolean = aBoolean3853


    fun isaBoolean3866(): Boolean = aBoolean3866




    fun isaBoolean3873(): Boolean = membersOnly

    fun isaBoolean3891(): Boolean = aBoolean3891

    fun isaBoolean3894(): Boolean = aBoolean3894


    fun getaByteArray3899(): ByteArray? = aByteArray3899

    fun isaBoolean3906(): Boolean = aBoolean3906


    fun getaByte3914(): Byte = aByte3914



    fun getaClass194_3922(): Any? = aClass194_3922

    fun isaBoolean3923(): Boolean = aBoolean3923

    fun isaBoolean3924(): Boolean = aBoolean3924

    fun isMirrored(): Boolean = mirrored
    fun isCastsShadow(): Boolean = castsShadow
    fun isShareLight(): Boolean = shareLight
    fun isRandomAnimationFrame(): Boolean = randomAnimationFrame
    fun isOpcode90Flag(): Boolean = opcode90Flag
    fun getTerrainContourMode(): Byte = terrainContourMode
    fun getRecolourPaletteIndices(): ByteArray? = recolourPaletteIndices
    fun getRetextureSource(): ShortArray? = retextureSource
    fun getRetextureDestination(): ShortArray? = retextureDestination

    fun hasAction(optionName: String?): Boolean = options.any { it?.equals(optionName, ignoreCase = true) == true }

    fun isSecondBool(): Boolean = secondBool

    fun isFirstBool(): Boolean = notClipped

    fun isProjectileClipped(): Boolean = projectileClipped

    fun getThirdBoolean(): Boolean = boolean1

    fun getAddObjectCheck(): Int = animationId

    fun getChildrenIds(): IntArray? = childrenIds

    fun getSizeY(): Int = sizeY

    fun getSizeX(): Int = sizeX

    fun getInteractable(): Int = interactable

    fun getClipType(): Int = clipType

    companion object {
        private val definitions: MutableMap<Int, LocType> = HashMap<Int, LocType>()

        @JvmStatic
        fun getDefinitions(): MutableMap<Int, LocType> = definitions
        private val OPTION_HANDLERS: MutableMap<String, OptionHandler?> = HashMap<String, OptionHandler?>()
        @Throws(Throwable::class)
        @JvmStatic
        fun parse() {
            for (objectId in 0 until Cache.getObjectDefinitionsSize()) {
                val data = Cache.getIndexes()[16]!!.getFileData(getContainerId(objectId), objectId and 0xff, null)
                if (data == null) {
                    LocType.getDefinitions().put(objectId, LocType())
                    continue
                }
                var def = LocType.parseDefinition(objectId, ByteBuffer.wrap(data))

                LocType.getDefinitions().put(objectId, def)
            }
        }

        @JvmStatic
        fun forId(objectId: Int): LocType {
            var def = definitions.get(objectId)
            if (def != null) {
                return def
            }
            def = LocType()
            definitions[objectId] = def
            def.id = objectId
            return def
        }

        @JvmStatic
        fun parseDefinition(objectId: Int, buffer: ByteBuffer): LocType {
            var def = LocType()
            def.id = objectId
            while (true) {
                if (!buffer.hasRemaining()) {
                    error("Missing terminator for object $objectId")
                }
                var opcode = buffer.get().toInt() and 0xFF
                if (opcode == 1 || opcode == 5) {
                    var length = buffer.get().toInt() and 0xff
                    if (def.modelIds == null) {
                        def.modelIds = IntArray(length)
                        if (opcode == 1) {
                            def.modelConfiguration = IntArray(length)
                        }
                        for (i in 0 until length) {
                            def.modelIds!![i] = buffer.getShort().toInt() and 0xFFFF
                            if (opcode == 1) {
                                def.modelConfiguration!![i] = buffer.get().toInt() and 0xFF
                            }
                        }
                    } else {
                        buffer.position(buffer.position() + (length * (if (opcode == 1) 3 else 2)))
                    }
                } else if (opcode == 2) {
                    def.name = ByteBufferUtils.getString(buffer)
                } else if (opcode == 14) {
                    def.sizeX = buffer.get().toInt() and 0xFF
                } else if (opcode == 15) {
                    def.sizeY = buffer.get().toInt() and 0xFF
                } else if (opcode == 17) {
                    def.projectileClipped = false
                    def.clipType = 0
                } else if (opcode == 18) {
                    def.projectileClipped = false
                } else if (opcode == 19) {
                    def.interactable = buffer.get().toInt() and 0xFF
                } else if (opcode == 21) {
                    def.terrainContourMode = 1.toByte()
                } else if (opcode == 22) {
                    def.shareLight = true
                } else if (opcode == 23) {
                    def.boolean1 = true
                } else if (opcode == 24) {
                    def.animationId = buffer.getShort().toInt() and 0xFFFF
                    if (def.animationId == 65535) {
                        def.animationId = -1
                    }
                } else if (opcode == 27) {
                    def.clipType = 1
                } else if (opcode == 28) {
                    def.wallWidth = ((buffer.get().toInt() and 0xFF) shl 2)
                } else if (opcode == 29) {
                    def.ambient = buffer.get().toInt()
                } else if (opcode == 39) {
                    def.contrast = buffer.get() * 5
                } else if (opcode in 30..34 || opcode in 150..154) {
                    val slot = if (opcode >= 150) opcode - 150 else opcode - 30
                    def.options[slot] = ByteBufferUtils.getString(buffer)
                    if (def.options[slot] == "Hidden") {
                        def.options[slot] = null
                        def.isHasHiddenOptions = true
                    }
                } else if (opcode == 40) {
                    var length = buffer.get().toInt() and 0xFF
                    def.originalColors = ShortArray(length)
                    def.modifiedColors = ShortArray(length)
                    for (i in 0 until length) {
                        def.originalColors!![i] = buffer.getShort()
                        def.modifiedColors!![i] = buffer.getShort()
                    }
                } else if (opcode == 41) {
                    var length = buffer.get().toInt() and 0xFF
                    def.retextureSource = ShortArray(length)
                    def.retextureDestination = ShortArray(length)
                    for (i in 0 until length) {
                        def.retextureSource!![i] = buffer.getShort()
                        def.retextureDestination!![i] = buffer.getShort()
                    }
                } else if (opcode == 42) {
                    var length = buffer.get().toInt() and 0xFF
                    def.recolourPaletteIndices = ByteArray(length)
                    for (i in 0 until length) {
                        def.recolourPaletteIndices!![i] = buffer.get()
                    }
                } else if (opcode == 60) {
                    def.mapIcon = buffer.getShort()
                } else if (opcode == 62) {
                    def.mirrored = true
                } else if (opcode == 64) {
                    def.castsShadow = false
                } else if (opcode == 65) {
                    def.resizeX = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 66) {
                    def.resizeY = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 67) {
                    def.resizeZ = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 68) {
                    buffer.getShort()
                } else if (opcode == 69) {
                    def.walkingFlag = buffer.get().toInt() and 0xFF
                } else if (opcode == 70) {
                    def.offsetX = buffer.getShort().toInt() shl 2
                } else if (opcode == 71) {
                    def.offsetY = buffer.getShort().toInt() shl 2
                } else if (opcode == 72) {
                    def.offsetZ = buffer.getShort().toInt() shl 2
                } else if (opcode == 73) {
                    def.secondBool = true
                } else if (opcode == 74) {
                    def.notClipped = true
                } else if (opcode == 75) {
                    def.opcode75Value = buffer.get().toInt() and 0xFF
                } else if (opcode == 77 || opcode == 92) {
                    def.varbitID = buffer.getShort().toInt() and 0xFFFF
                    if (def.varbitID == 65535) {
                        def.varbitID = -1
                    }
                    def.configId = buffer.getShort().toInt() and 0xFFFF
                    if (def.configId == 65535) {
                        def.configId = -1
                    }
                    var defaultId = -1
                    if (opcode == 92) {
                        defaultId = buffer.getShort().toInt() and 0xFFFF
                        if (defaultId == 65535) {
                            defaultId = -1
                        }
                    }
                    var childrenAmount = buffer.get().toInt() and 0xFF
                    def.childrenIds = IntArray(childrenAmount + 2)
                    var index = 0
                    while (childrenAmount >= index) {
                        def.childrenIds!![index] = buffer.getShort().toInt() and 0xFFFF
                        if (def.childrenIds!![index] == 65535) {
                            def.childrenIds!![index] = -1
                        }
                        index++
                    }
                    def.childrenIds!![childrenAmount + 1] = defaultId
                } else if (opcode == 78) {
                    def.backgroundSoundId = buffer.getShort().toInt() and 0xFFFF
                    def.backgroundSoundRange = buffer.get().toInt() and 0xFF
                } else if (opcode == 79) {
                    def.backgroundSoundMinDelay = buffer.getShort().toInt() and 0xFFFF
                    def.backgroundSoundMaxDelay = buffer.getShort().toInt() and 0xFFFF
                    def.backgroundSoundRange = buffer.get().toInt() and 0xFF
                    var length = buffer.get().toInt() and 0xFF
                    def.backgroundSoundIds = IntArray(length)
                    for (i in 0 until length) {
                        def.backgroundSoundIds!![i] = buffer.getShort().toInt() and 0xFFFF
                    }
                } else if (opcode == 81) {
                    def.terrainContourMode = 2.toByte()
                    def.terrainContourValue = 256 * (buffer.get().toInt() and 0xFF)
                } else if (opcode == 82 || opcode == 88)
                {} else if (opcode == 89) {
                    def.randomAnimationFrame = false
                } else if (opcode == 90) {
                    def.opcode90Flag = true
                } else if (opcode == 91) {
                    def.membersOnly = true
                } else if (opcode == 93) {
                    def.terrainContourMode = 3.toByte()
                    def.terrainContourValue = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 94) {
                    def.terrainContourMode = 4.toByte()
                } else if (opcode == 95) {
                    def.terrainContourMode = 5.toByte()
                } else if (opcode in 96..98 || opcode == 103 || opcode == 105)
                {} else if (opcode == 99 || opcode == 100) {
                    val option = buffer.get().toInt() and 0xff
                    def.optionCursors[option] = buffer.short.toInt() and 0xffff
                } else if (opcode == 101 || opcode == 104) {
                    buffer.get()
                } else if (opcode == 102) {
                    def.mapSceneId = buffer.short.toInt() and 0xffff
                } else if (opcode == 107) {
                    def.mapElementId = buffer.short.toInt() and 0xffff
                } else if (opcode == 106) {
                    val count = buffer.get().toInt() and 0xff
                    val ids = IntArray(count)
                    val weights = IntArray(count)
                    for (index in ids.indices) {
                        ids[index] = buffer.short.toInt() and 0xffff
                        weights[index] = buffer.get().toInt() and 0xff
                    }
                    def.animationIds = ids
                    def.animationWeights = weights
                } else if (opcode == 160) {
                    def.questIds = IntArray(buffer.get().toInt() and 0xff) { buffer.short.toInt() and 0xffff }
                } else if (opcode == 249) {
                    def.cacheParams = DefinitionParams.read(buffer, def.cacheParams)
                } else {
                    if (opcode != 0) {
                        error("Object $objectId: unknown opcode $opcode at ${buffer.position() - 1}")
                    }
                    break
                }
            }
            def.configureObject()
            if (def.notClipped) {
                def.clipType = 0
                def.projectileClipped = false
            }
            return def
        }

        @JvmStatic
        fun getOptionHandler(nodeId: Int, name: String?): OptionHandler? =
            forId(nodeId).getConfiguration<OptionHandler>("option:$name") ?: OPTION_HANDLERS[name]


        @JvmStatic
        fun setOptionHandler(name: String, handler: OptionHandler?): Boolean = OPTION_HANDLERS.put(name, handler) != null

        @JvmStatic
        fun getContainerId(id: Int): Int = id.ushr(8)
    }
}
