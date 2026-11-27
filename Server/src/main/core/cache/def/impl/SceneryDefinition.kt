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

class SceneryDefinition : Definition<Scenery>() {
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
    var questIds: IntArray? = null
        private set
    var modelIds: IntArray? = null
        private set
    var modelConfiguration: IntArray? = null
    var anIntArray3833: IntArray? = null
        private set
    var anInt3834: Int = 0
    var anInt3835: Int = -1
        private set
    private var aByte3837: Byte = 0
    var anInt3838: Int = -1
        private set
    var aBoolean3839: Boolean = false
    var anInt3840: Int = 0
        private set
    var anInt3841: Int = 128
        private set
    var anInt3844: Int = -1
        private set
    var aBoolean3845: Boolean = false
    private var aByte3847: Byte = 0
    private var aByte3849: Byte = 0
    var anInt3850: Int = 0
        private set
    var anInt3851: Int = -1
        private set
    @JvmField var secondBool: Boolean = false
    @JvmField var aBoolean3853: Boolean = true
    var anInt3855: Int = -1
        private set
    @JvmField var notClipped: Boolean = false
    var anInt3857: Int = -1
        private set
    private var aByteArray3858: ByteArray? = null
    var anIntArray3859: IntArray? = null
        private set
    var anInt3860: Int = -1
        private set
    var varbitID: Int = -1
        private set
    var modifiedColors: ShortArray? = null
        private set
    var anInt3865: Int = 255
        private set
    var aBoolean3866: Boolean = false
    var aBoolean3867: Boolean = false
    @JvmField var projectileClipped: Boolean = true
    var anIntArray3869: IntArray? = null
    var aBoolean3870: Boolean = false
    @JvmField var sizeY: Int = 1
    var aBoolean3872: Boolean = true
    var membersOnly: Boolean = false
    @JvmField var boolean1: Boolean = false
    var anInt3875: Int = 0
    @JvmField var animationId: Int = -1
    var anInt3877: Int = 0
    var anInt3878: Int = 0
        private set
    @JvmField var clipType: Int = 2
    var anInt3881: Int = 0
    var anInt3882: Int = -1
        private set
    var anInt3883: Int = 0
        private set
    var loader: Any? = null
        private set
    var anInt3889: Int = 0
        private set
    @JvmField var sizeX: Int = 1
    @JvmField var aBoolean3891: Boolean = false
    var anInt3892: Int = 64
        private set
    @JvmField var interactable: Int = -1
    var aBoolean3894: Boolean = false
    var aBoolean3895: Boolean = true
    var anInt3896: Int = 0
        private set
    var configId: Int = -1
        private set
    private val aByteArray3899: ByteArray? = null
    var anInt3900: Int = 0
        private set
    var anInt3902: Int = 128
        private set
    var anInt3904: Int = 0
        private set
    var anInt3905: Int = 0
        private set
    var aBoolean3906: Boolean = false
    var anIntArray3908: IntArray? = null
        private set
    private var aByte3912: Byte = 0.toByte()
    var anInt3913: Int = -1
        private set
    private var aByte3914: Byte = 0.toByte()
    var anInt3915: Int = 0
        private set
    var anIntArrayArray3916: Array<IntArray?>? = null
    var anInt3917: Int = 128
        private set
    private var aShortArray3919: ShortArray? = null
    private var aShortArray3920: ShortArray? = null
    var anInt3921: Int = 0
        private set
    private val aClass194_3922: Any? = null
    var aBoolean3923: Boolean = false
    var aBoolean3924: Boolean = false
    var walkingFlag: Int = 0
        private set
    var isHasHiddenOptions: Boolean = false
    var mapIcon: Short = 0
        private set
    val configFile: VarbitDefinition?
        get() {
            if (varbitID != -1) {
                return VarbitDefinition.forSceneryId(varbitID)
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
        if (anInt3855 == -1) {
            anInt3855 = if (clipType == 0) 0 else 1
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

    fun getChildObject(player: Player?): SceneryDefinition {
        if (childrenIds == null || childrenIds!!.size < 1) {
            return this
        }
        var configValue = -1
        if (player != null) {
            if (varbitID != -1) {
                configValue = VarbitDefinition.forSceneryId(varbitID).getValue(player)
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

    fun getChildObjectAtIndex(index: Int): SceneryDefinition {
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

    fun isaBoolean3839(): Boolean = aBoolean3839

    fun setaBoolean3839(aBoolean3839: Boolean) {
        this.aBoolean3839 = aBoolean3839
    }

    fun getaByte3837(): Byte = aByte3837

    fun isaBoolean3845(): Boolean = aBoolean3845

    fun getaByte3847(): Byte = aByte3847

    fun getaByte3849(): Byte = aByte3849

    fun isaBoolean3853(): Boolean = aBoolean3853

    fun getaByteArray3858(): ByteArray? = aByteArray3858

    fun isaBoolean3866(): Boolean = aBoolean3866

    fun isaBoolean3867(): Boolean = aBoolean3867

    fun isaBoolean3870(): Boolean = aBoolean3870

    fun isaBoolean3872(): Boolean = aBoolean3872

    fun isaBoolean3873(): Boolean = membersOnly

    fun isaBoolean3891(): Boolean = aBoolean3891

    fun isaBoolean3894(): Boolean = aBoolean3894

    fun isaBoolean3895(): Boolean = aBoolean3895

    fun getaByteArray3899(): ByteArray? = aByteArray3899

    fun isaBoolean3906(): Boolean = aBoolean3906

    fun getaByte3912(): Byte = aByte3912

    fun getaByte3914(): Byte = aByte3914

    fun getaShortArray3919(): ShortArray? = aShortArray3919

    fun getaShortArray3920(): ShortArray? = aShortArray3920

    fun getaClass194_3922(): Any? = aClass194_3922

    fun isaBoolean3923(): Boolean = aBoolean3923

    fun isaBoolean3924(): Boolean = aBoolean3924

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
        private val definitions: MutableMap<Int, SceneryDefinition> = HashMap<Int, SceneryDefinition>()

        @JvmStatic
        fun getDefinitions(): MutableMap<Int, SceneryDefinition> = definitions
        private val OPTION_HANDLERS: MutableMap<String, OptionHandler?> = HashMap<String, OptionHandler?>()
        @Throws(Throwable::class)
        @JvmStatic
        fun parse() {
            for (objectId in 0 until Cache.getObjectDefinitionsSize()) {
                val data = Cache.getIndexes()[16]!!.getFileData(getContainerId(objectId), objectId and 0xff, null)
                if (data == null) {
                    SceneryDefinition.getDefinitions().put(objectId, SceneryDefinition())
                    continue
                }
                var def = SceneryDefinition.parseDefinition(objectId, ByteBuffer.wrap(data))

                SceneryDefinition.getDefinitions().put(objectId, def)
            }
        }

        @JvmStatic
        fun forId(objectId: Int): SceneryDefinition {
            var def = definitions.get(objectId)
            if (def != null) {
                return def
            }
            def = SceneryDefinition()
            definitions[objectId] = def
            def.id = objectId
            return def
        }

        @JvmStatic
        fun parseDefinition(objectId: Int, buffer: ByteBuffer): SceneryDefinition {
            var def = SceneryDefinition()
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
                    def.aByte3912 = 1.toByte()
                } else if (opcode == 22) {
                    def.aBoolean3867 = true
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
                    def.anInt3892 = ((buffer.get().toInt() and 0xFF) shl 2)
                } else if (opcode == 29) {
                    def.anInt3878 = buffer.get().toInt()
                } else if (opcode == 39) {
                    def.anInt3840 = buffer.get() * 5
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
                    def.aShortArray3920 = ShortArray(length)
                    def.aShortArray3919 = ShortArray(length)
                    for (i in 0 until length) {
                        def.aShortArray3920!![i] = buffer.getShort()
                        def.aShortArray3919!![i] = buffer.getShort()
                    }
                } else if (opcode == 42) {
                    var length = buffer.get().toInt() and 0xFF
                    def.aByteArray3858 = ByteArray(length)
                    for (i in 0 until length) {
                        def.aByteArray3858!![i] = buffer.get()
                    }
                } else if (opcode == 60) {
                    def.mapIcon = buffer.getShort()
                } else if (opcode == 62) {
                    def.aBoolean3839 = true
                } else if (opcode == 64) {
                    def.aBoolean3872 = false
                } else if (opcode == 65) {
                    def.anInt3902 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 66) {
                    def.anInt3841 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 67) {
                    def.anInt3917 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 68) {
                    buffer.getShort()
                } else if (opcode == 69) {
                    def.walkingFlag = buffer.get().toInt() and 0xFF
                } else if (opcode == 70) {
                    def.anInt3883 = buffer.getShort().toInt() shl 2
                } else if (opcode == 71) {
                    def.anInt3889 = buffer.getShort().toInt() shl 2
                } else if (opcode == 72) {
                    def.anInt3915 = buffer.getShort().toInt() shl 2
                } else if (opcode == 73) {
                    def.secondBool = true
                } else if (opcode == 74) {
                    def.notClipped = true
                } else if (opcode == 75) {
                    def.anInt3855 = buffer.get().toInt() and 0xFF
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
                    def.anInt3860 = buffer.getShort().toInt() and 0xFFFF
                    def.anInt3904 = buffer.get().toInt() and 0xFF
                } else if (opcode == 79) {
                    def.anInt3900 = buffer.getShort().toInt() and 0xFFFF
                    def.anInt3905 = buffer.getShort().toInt() and 0xFFFF
                    def.anInt3904 = buffer.get().toInt() and 0xFF
                    var length = buffer.get().toInt() and 0xFF
                    def.anIntArray3859 = IntArray(length)
                    for (i in 0 until length) {
                        def.anIntArray3859!![i] = buffer.getShort().toInt() and 0xFFFF
                    }
                } else if (opcode == 81) {
                    def.aByte3912 = 2.toByte()
                    def.anInt3882 = 256 * (buffer.get().toInt() and 0xFF)
                } else if (opcode == 82 || opcode == 88)
                {} else if (opcode == 89) {
                    def.aBoolean3895 = false
                } else if (opcode == 90) {
                    def.aBoolean3870 = true
                } else if (opcode == 91) {
                    def.membersOnly = true
                } else if (opcode == 93) {
                    def.aByte3912 = 3.toByte()
                    def.anInt3882 = buffer.getShort().toInt() and 0xFFFF
                } else if (opcode == 94) {
                    def.aByte3912 = 4.toByte()
                } else if (opcode == 95) {
                    def.aByte3912 = 5.toByte()
                } else if (opcode in 96..98 || opcode == 103 || opcode == 105)
                {} else if (opcode == 99 || opcode == 100) {
                    buffer.get()
                    buffer.getShort()
                } else if (opcode == 101 || opcode == 104) {
                    buffer.get()
                } else if (opcode == 102 || opcode == 107) {
                    buffer.getShort()
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
