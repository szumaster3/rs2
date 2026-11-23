package core.cache.def.impl

import core.cache.Cache
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.HashMap

class IfaceDefinition {
    @JvmField var id: Int = 0
    @JvmField var type: ComponentType? = null
    @JvmField var version: Int = 0
    @JvmField var clientCode: Int = 0
    @JvmField var baseX: Int = 0
    @JvmField var baseY: Int = 0
    @JvmField var baseWidth: Int = 0
    @JvmField var baseHeight: Int = 0
    @JvmField var dynWidth: Int = 0
    @JvmField var dynHeight: Int = 0
    @JvmField var xMode: Int = 0
    @JvmField var yMode: Int = 0
    @JvmField var overlayer: Int = 0
    @JvmField var hidden: Boolean = false
    @JvmField var scrollMaxH: Int = 0
    @JvmField var scrollMaxV: Int = 0
    @JvmField var noClickThrough: Boolean = false
    @JvmField var spriteId: Int = 0
    @JvmField var activeSpriteId: Int = 0
    @JvmField var angle2d: Int = 0
    @JvmField var hasAlpha: Boolean = false
    @JvmField var spriteTiling: Boolean = false
    @JvmField var alpha: Int = 0
    @JvmField var outlineThickness: Int = 0
    @JvmField var shadowColor: Int = 0
    @JvmField var hFlip: Boolean = false
    @JvmField var vFlip: Boolean = false
    @JvmField var modelType: Int = 0
    @JvmField var activeModelType: Int = 0
    @JvmField var modelId: Int = 0
    @JvmField var activeModelId: Int = 0
    @JvmField var unknownModelProp_1: Int = 0
    @JvmField var unknownModelProp_2: Int = 0
    @JvmField var modelXAngle: Int = 0
    @JvmField var modelYAngle: Int = 0
    @JvmField var modelYOffset: Int = 0
    @JvmField var modelZoom: Int = 0
    @JvmField var modelAnimId: Int = 0
    @JvmField var activeModelAnimId: Int = 0
    @JvmField var modelOrtho: Boolean = false
    @JvmField var unknownModelProp_3: Int = 0
    @JvmField var unknownModelProp_4: Int = 0
    @JvmField var unknownModelProp_5: Boolean = false
    @JvmField var unknownModelProp_6: Int = 0
    @JvmField var unknownModelProp_7: Int = 0
    @JvmField var font: Int = 0
    @JvmField var text: String? = null
    @JvmField var activeText: String? = null
    @JvmField var vPadding: Int = 0
    @JvmField var halign: Int = 0
    @JvmField var valign: Int = 0
    @JvmField var shadowed: Boolean = false
    @JvmField var color: Int = 0
    @JvmField var activeColor: Int = 0
    @JvmField var overColor: Int = 0
    @JvmField var unknownColor: Int = 0
    @JvmField var filled: Boolean = false
    @JvmField var lineWidth: Int = 0
    @JvmField var unknownProp_8: Boolean = false
    @JvmField var unknownIntArray_1: IntArray? = null
    @JvmField var unknownIntArray_2: IntArray? = null
    @JvmField var unknownByteArray_1: ByteArray? = null
    @JvmField var unknownByteArray_2: ByteArray? = null
    @JvmField var optionBase: String? = null
    @JvmField var ops: Array<String?>? = null
    @JvmField var dragDeadzone: Int = 0
    @JvmField var dragDeadtime: Int = 0
    @JvmField var dragRenderBehavior: Boolean = false
    @JvmField var opCircumfix: String? = null
    @JvmField var opSuffix: String? = null
    @JvmField var option: String? = null
    @JvmField var unknownProp_9: Int = 0
    @JvmField var unknownProp_10: Int = 0
    @JvmField var unknownProp_11: Int = 0
    @JvmField var cs1ComparisonOperands: IntArray? = null
    @JvmField var cs1ComparisonOpcodes: IntArray? = null
    @JvmField var cs1Scripts: Array<IntArray?>? = null
    @JvmField var objCounts: IntArray? = null
    @JvmField var objTypes: IntArray? = null
    @JvmField var invMarginX: Int = 0
    @JvmField var invMarginY: Int = 0
    @JvmField var invOffsetX: IntArray? = null
    @JvmField var invOffsetY: IntArray? = null
    @JvmField var invSprite: IntArray? = null
    @JvmField var buttonType: Int = 0
    @JvmField var invOptions: Array<String?>? = null
    @JvmField var scripts: LinkedScripts? = null
    @JvmField var triggers: ScriptTriggers? = null
    @JvmField var children: Array<IfaceDefinition?>? = null
    @JvmField var parent: Int = 0
    override fun toString(): String {
        if (parent == id) {
            return "[IF " + id + " (P)]"
        } else {
            return "[IF " + (id shr 16) + ", CH " + (id and 0xFF) + " (" + type!!.name + ")]"
        }
    }

    companion object {
        private val defCache: HashMap<Int, IfaceDefinition> = HashMap<Int, IfaceDefinition>()

        @JvmStatic
        fun forId(id: Int): IfaceDefinition? {
            if (defCache.containsKey(id)) {
                return defCache.get(id)
            }
            var def = loadAndParse(id)
            defCache.put(id, def!!)
            return def
        }

        @JvmStatic
        fun forId(id: Int, child: Int): IfaceDefinition? {
            if (defCache.containsKey(child + (id shl 16)))
                return defCache.get(child + (id shl 16))
            var def = forId(id)
            return def!!.children!![child]
        }
        private fun loadAndParse(id: Int): IfaceDefinition? {
            var def = IfaceDefinition()
            var childCount = Cache.getIndexes()[3]!!.getFilesSize(id)
            def.children = arrayOfNulls<IfaceDefinition>(childCount)
            def.id = id
            def.parent = id
            for (i in 0 until childCount) {
                def.children!![i] = parseChild(id, i)
            }
            return def
        }
        private fun parseChild(id: Int, childIndex: Int): IfaceDefinition? {
            var def = IfaceDefinition()
            def.id = childIndex + (id shl 16)
            def.parent = id
            defCache.put(def.id, def)
            var dataRaw = Cache.getIndexes()[3]!!.getFileData(id, childIndex)
            if (dataRaw == null) return def
            val data = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.wrap(dataRaw))
            if (dataRaw[0].toInt() == -1)
                decodeIf3(def, data)
            else
                decodeIf1(def, data)
            return def
        }
        private fun decodeIf1(def: IfaceDefinition?, data: IoBuffer?) {
            data!!.g1()
            def!!.version = 1
            def.type = ComponentType.values()[data.g1()]
            def.buttonType = data.g1()
            def.clientCode = data.g2()
            def.baseX = data.g2b()
            def.baseY = data.g2b()
            def.baseWidth = data.g2()
            def.baseHeight = data.g2()
            def.dynHeight = 0
            def.dynWidth = def.dynHeight
            def.yMode = 0
            def.xMode = def.yMode
            def.alpha = data.g1()
            def.overlayer = data.g2()
            if (def.overlayer == 65535)
                def.overlayer = -1
            else
                def.overlayer += def.id and -0x10000
            def.unknownProp_11 = data.g2()
            var unknownLocal_1 = data.g1()
            if (unknownLocal_1 > 0) {
                def.cs1ComparisonOpcodes = IntArray(unknownLocal_1)
                def.cs1ComparisonOperands = IntArray(unknownLocal_1)
                for (i in 0 until unknownLocal_1) {
                    def.cs1ComparisonOpcodes!![i] = data.g1()
                    def.cs1ComparisonOperands!![i] = data.g2()
                }
            }
            var unknownLocal_2 = data.g1()
            var unknownLocal_3: Int
            if (unknownLocal_2 > 0) {
                def.cs1Scripts = arrayOfNulls<IntArray>(unknownLocal_2)
                for (i in 0 until unknownLocal_2) {
                    unknownLocal_3 = data.g2()
                    def.cs1Scripts!![i] = IntArray(unknownLocal_3)
                    for (j in 0 until unknownLocal_3) {
                        def.cs1Scripts!![i]!![j] = data.g2()
                        if (def.cs1Scripts!![i]!![j] == 65535)
                            def.cs1Scripts!![i]!![j] = -1
                    }
                }
            }
            var flags = 0
            if (def.type == ComponentType.SCROLLABLE) {
                def.scrollMaxV = data.g2()
                def.hidden = data.g1() == 1
            }
            if (def.type == ComponentType.UNKNOWN_1) {
                data.g2()
                data.g1()
            }
            if (def.type == ComponentType.UNKNOWN_2) {
                def.dynHeight = 3
                def.objCounts = IntArray(def.baseWidth * def.baseHeight)
                def.objTypes = IntArray(def.baseWidth * def.baseHeight)
                def.dynWidth = 3
                var unknownLocal_4 = data.g1()
                var unknownLocal_5 = data.g1()
                if (unknownLocal_4 == 1)
                    flags = 268435456
                var unknownLocal_6 = data.g1()
                if (unknownLocal_5 == 1)
                    flags = flags or 0x40000000
                if (unknownLocal_6 == 1)
                    flags = flags or Integer.MAX_VALUE
                var unknownLocal_7 = data.g1()
                if (unknownLocal_7 == 1)
                    flags = flags or 0x20000000
                def.invMarginX = data.g1()
                def.invMarginY = data.g1()
                def.invOffsetX = IntArray(20)
                def.invOffsetY = IntArray(20)
                def.invSprite = IntArray(20)
                for (i in 0..19) {
                    var hasSprite = data.g1()
                    if (hasSprite == 1) {
                        def.invOffsetX!![i] = data.g2b()
                        def.invOffsetY!![i] = data.g2b()
                        def.invSprite!![i] = data.g4()
                    } else {
                        def.invSprite!![i] = -1
                    }
                }
                def.invOptions = arrayOfNulls<String>(5)
                for (i in 0..4) {
                    var option = data.getJagString()
                    if (option!!.length > 0) {
                        def.invOptions!![i] = option
                        flags = flags or (0x1 shl i + 23)
                    }
                }
            }
            if (def.type == ComponentType.UNKNOWN_3) {
                def.filled = data.g1() == 1
            }
            if (def.type == ComponentType.TEXT || def.type == ComponentType.UNKNOWN_1) {
                def.halign = data.g1()
                def.valign = data.g1()
                def.vPadding = data.g1()
                def.font = data.g2()
                if (def.font == 65535)
                    def.font = -1
                def.shadowed = data.g1() == 1
            }
            if (def.type == ComponentType.TEXT) {
                def.text = data.getJagString()
                def.activeText = data.getJagString()
            }
            if (def.type == ComponentType.UNKNOWN_1 || def.type == ComponentType.UNKNOWN_3 || def.type == ComponentType.TEXT) {
                def.color = data.g4()
            }
            if (def.type == ComponentType.UNKNOWN_3 || def.type == ComponentType.TEXT) {
                def.activeColor = data.g4()
                def.overColor = data.g4()
                def.unknownColor = data.g4()
            }
            if (def.type == ComponentType.SPRITE) {
                def.spriteId = data.g4()
                def.activeSpriteId = data.g4()
            }
            if (def.type == ComponentType.MODEL) {
                def.modelType = 1
                def.modelId = data.g2()
                if (def.modelId == 65535)
                    def.modelId = -1
                def.activeModelType = 1
                def.activeModelId = data.g2()
                if (def.activeModelId == 65535)
                    def.activeModelId = -1
                def.modelAnimId = data.g2()
                if (def.modelAnimId == 65535)
                    def.modelAnimId = -1
                def.activeModelAnimId = data.g2()
                if (def.activeModelAnimId == 65535)
                    def.activeModelAnimId = -1
                def.modelZoom = data.g2()
                def.modelXAngle = data.g2()
                def.modelYAngle = data.g2()
            }
            if (def.type == ComponentType.UNKNOWN_7) {
                def.dynWidth = 3
                def.dynHeight = def.dynWidth
                def.objCounts = IntArray(def.baseHeight * def.baseWidth)
                def.objTypes = IntArray(def.baseHeight * def.baseWidth)
                def.halign = data.g1()
                def.font = data.g2()
                if (def.font == 65535)
                    def.font = -1
                def.shadowed = data.g1() == 1
                def.color = data.g4()
                def.invMarginX = data.g2b()
                def.invMarginY = data.g2b()
                var invHasOptions = data.g1()
                if (invHasOptions == 1) {
                    flags = flags or 0x40000000
                }
                def.invOptions = arrayOfNulls<String>(5)
                for (i in 0..4) {
                    var option = data.getJagString()
                    if (option!!.length > 0) {
                        def.invOptions!![i] = option
                        flags = flags or (0x1 shl i + 23)
                    }
                }
            }
            if (def.type == ComponentType.UNKNOWN_8)
                def.text = data.getJagString()
            if (def.buttonType == 2 || def.type == ComponentType.UNKNOWN_2) {
                def.opCircumfix = data.getJagString()
                def.opSuffix = data.getJagString()
                var unknownLocal_4 = data.g2() and 0x3F
                flags = flags or (unknownLocal_4 shl 11)
            }
            if (def.buttonType == 1 || def.buttonType == 4 || def.buttonType == 5 || def.buttonType == 6) {
                def.option = data.getJagString()
                if (def.option!!.length == 0) {
                    when (def.buttonType) {
                        1 -> def.option = "Ok"
                        4, 5 -> def.option = "Select"
                        6 -> def.option = "Continue"
                    }
                }
            }
            if (def.buttonType == 1 || def.buttonType == 4 || def.buttonType == 5) {
                flags = flags or 0x400000
            }
            if (def.buttonType == 6) {
                flags = flags or 0x1
            }
        }
        private fun decodeIf3(def: IfaceDefinition?, data: IoBuffer?) {
            data!!.g1()
            def!!.version = 3
            var type = data.g1()
            if ((type and 0x80) != 0) {
                type = type and 0x7F
                data.getJagString()
            }
            def.type = ComponentType.values()[type]
            def.clientCode = data.g2()
            def.baseX = data.g2b()
            def.baseY = data.g2b()
            def.baseWidth = data.g2()
            def.baseHeight = data.g2()
            def.dynWidth = data.g1b()
            def.dynHeight = data.g1b()
            def.yMode = data.g1b()
            def.xMode = data.g1b()
            def.overlayer = data.g2()
            if (def.overlayer == 65535) {
                def.overlayer = -1
            } else {
                def.overlayer = (def.id and -0x10000) + def.overlayer
            }
            def.hidden = data.g1() == 1
            parseIf3Type(def, data)
            var unknownLocal_1 = data.g3()
            var unknownLocal_2 = data.g1()
            var unknownLocal_3: Int
            if (unknownLocal_2 != 0) {
                def.unknownIntArray_1 = IntArray(10)
                def.unknownByteArray_1 = ByteArray(10)
                def.unknownByteArray_2 = ByteArray(10)
                while (unknownLocal_2 != 0) {
                    unknownLocal_3 = (unknownLocal_2 shr 4) - 1
                    unknownLocal_2 = data.g1() or (unknownLocal_2 shl 8)
                    unknownLocal_2 = unknownLocal_2 and 0xFFF
                    if (unknownLocal_2 == 4095) {
                        def.unknownIntArray_1!![unknownLocal_3] = -1
                    } else {
                        def.unknownIntArray_1!![unknownLocal_3] = unknownLocal_2
                    }
                    def.unknownByteArray_2!![unknownLocal_3] = data.g1b().toByte()
                    def.unknownByteArray_1!![unknownLocal_3] = data.g1b().toByte()
                    unknownLocal_2 = data.g1()
                }
            }
            def.optionBase = data.getJagString()
            unknownLocal_2 = data.g1()
            var opCount = unknownLocal_2 and 0xF
            if (opCount > 0) {
                def.ops = arrayOfNulls<String>(opCount)
                for (i in 0 until opCount) {
                    def.ops!![i] = data.getJagString()
                }
            }
            var unknownLocal_5: Int
            var unknownLocal_6 = unknownLocal_2 shr 4
            if (unknownLocal_6 > 0) {
                unknownLocal_5 = data.g1()
                def.unknownIntArray_2 = IntArray(unknownLocal_5 + 1)
                for (i in def.unknownIntArray_2!!.indices)
                    def.unknownIntArray_2!![i] = -1
                def.unknownIntArray_2!![unknownLocal_5] = data.g2()
            }
            if (unknownLocal_6 > 1) {
                unknownLocal_5 = data.g1()
                def.unknownIntArray_2!![unknownLocal_5] = data.g2()
            }
            def.dragDeadzone = data.g1()
            def.dragDeadtime = data.g1()
            def.dragRenderBehavior = data.g1() == 1
            unknownLocal_5 = -1
            def.opCircumfix = data.getJagString()
            if (((unknownLocal_1 shr 11) and 0x7F) != 0) {
                unknownLocal_5 = data.g2()
                def.unknownProp_9 = data.g2()
                if (unknownLocal_5 == 65535)
                    unknownLocal_5 = -1
                if (def.unknownProp_9 == 65535)
                    def.unknownProp_9 = -1
                def.unknownProp_10 = data.g2()
                if (def.unknownProp_10 == 65535)
                    def.unknownProp_10 = -1
            }
            def.scripts = LinkedScripts()
            def.scripts!!.unknown = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseOver = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseLeave = parseIf3ScriptArgs(data)
            def.scripts!!.onUseWith = parseIf3ScriptArgs(data)
            def.scripts!!.onUse = parseIf3ScriptArgs(data)
            def.scripts!!.onVarpTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onInvTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onStatTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onTimer = parseIf3ScriptArgs(data)
            def.scripts!!.onOptionClick = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseRepeat = parseIf3ScriptArgs(data)
            def.scripts!!.onClickRepeat = parseIf3ScriptArgs(data)
            def.scripts!!.onDrag = parseIf3ScriptArgs(data)
            def.scripts!!.onRelease = parseIf3ScriptArgs(data)
            def.scripts!!.onHold = parseIf3ScriptArgs(data)
            def.scripts!!.onDragStart = parseIf3ScriptArgs(data)
            def.scripts!!.onDragRelease = parseIf3ScriptArgs(data)
            def.scripts!!.onScroll = parseIf3ScriptArgs(data)
            def.scripts!!.onVarcTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onVarcstrTransmit = parseIf3ScriptArgs(data)
            def.triggers = ScriptTriggers()
            def.triggers!!.varpTriggers = parseIf3Triggers(data)
            def.triggers!!.inventoryTriggers = parseIf3Triggers(data)
            def.triggers!!.statTriggers = parseIf3Triggers(data)
            def.triggers!!.varcTriggers = parseIf3Triggers(data)
            def.triggers!!.varcstrTriggers = parseIf3Triggers(data)
        }
        private fun parseIf3ScriptArgs(data: IoBuffer?): ScriptArgs? {
            var argCount = data!!.g1()
            if (argCount == 0)
                return null
            var argArray = arrayOfNulls<Any>(argCount)
            for (i in 0 until argCount) {
                var type = data.g1()
                if (type == 0) {
                    var datum = Integer.valueOf(data.g4())!!
                    argArray[i] = datum
                } else if (type == 1) {
                    var datum = data.getJagString()
                    argArray[i] = datum
                }
            }
            return ScriptArgs(argArray[0] as Int, Arrays.copyOfRange<Any?>(argArray, 1, argArray.size))
        }
        private fun parseIf3Triggers(data: IoBuffer?): IntArray? {
            var triggerCount = data!!.g1()
            if (triggerCount == 0)
                return null
            var triggers = IntArray(triggerCount)
            for (i in 0 until triggerCount) {
                triggers[i] = data.g4()
            }
            return triggers
        }
        private fun parseIf3Type(def: IfaceDefinition?, data: IoBuffer?) {
            when (def!!.type) {
                ComponentType.SCROLLABLE -> {
                    def.scrollMaxH = data!!.g2()
                    def.scrollMaxV = data.g2()
                    def.noClickThrough = data.g1() == 1
                }
                ComponentType.SPRITE -> {
                    def.spriteId = data!!.g4()
                    def.angle2d = data.g2()
                    var spriteFlags = data.g1()
                    def.hasAlpha = (spriteFlags and 0x2) != 0
                    def.spriteTiling = (spriteFlags and 0x1) != 0
                    def.alpha = data.g1()
                    def.outlineThickness = data.g1()
                    def.shadowColor = data.g4()
                    def.vFlip = data.g1() == 1
                    def.hFlip = data.g1() == 1
                }
                ComponentType.MODEL -> {
                    def.modelType = 1
                    def.modelId = data!!.g2()
                    if (def.modelId == 65535)
                        def.modelId = -1
                    def.unknownModelProp_1 = data.g2b()
                    def.unknownModelProp_2 = data.g2b()
                    def.modelXAngle = data.g2()
                    def.modelYAngle = data.g2()
                    def.modelYOffset = data.g2()
                    def.modelZoom = data.g2()
                    def.modelAnimId = data.g2()
                    if (def.modelAnimId == 65535)
                        def.modelAnimId = -1
                    def.modelOrtho = data.g1() == 1
                    def.unknownModelProp_3 = data.g2()
                    def.unknownModelProp_4 = data.g2()
                    def.unknownModelProp_5 = data.g1() == 1
                    if (def.dynWidth != 0)
                        def.unknownModelProp_6 = data.g2()
                    if (def.dynHeight != 0)
                        def.unknownModelProp_7 = data.g2()
                }
                ComponentType.TEXT -> {
                    def.font = data!!.g2()
                    if (def.font == 65535)
                        def.font = -1
                    def.text = data.getJagString()
                    def.vPadding = data.g1()
                    def.halign = data.g1()
                    def.valign = data.g1()
                    def.shadowed = data.g1() == 1
                    def.color = data.g4()
                }
                ComponentType.UNKNOWN_3 -> {
                    def.color = data!!.g4()
                    def.filled = data.g1() == 1
                    def.alpha = data.g1()
                }
                ComponentType.UNKNOWN_9 -> {
                    def.lineWidth = data!!.g1()
                    def.color = data.g4()
                    def.unknownProp_8 = data.g1() == 1
                } else -> Unit
            }
        }
    }
}
