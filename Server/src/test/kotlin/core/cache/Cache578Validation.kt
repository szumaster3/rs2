package core.cache

import core.cache.def.impl.*
import core.cache.misc.buffer.ByteBufferUtils
import core.game.system.config.XteaParser
import core.tools.StringUtils
import java.io.File
import java.nio.ByteBuffer

/**
 * Run from Server with the test and application classes on the classpath.
 */
object Cache578Validation {
    @JvmStatic
    fun main(args: Array<String>) {
        val data = File(args.firstOrNull() ?: "data")
        Cache.openIndexes(File(data, "cache").path)
        var failures = 0
        val configs = requireNotNull(Cache.getIndexes()[2])
        fun verifyQuests(ids: IntArray?) {
            ids?.forEach { check(configs.validFile(35, it)) { "missing quest definition $it" } }
        }

        fun scan(name: String, index: Int, onlyGroup: Int? = null, decode: (Int, Int, ByteBuffer) -> Unit) {
            val manager = requireNotNull(Cache.getIndexes()[index])
            val groups = onlyGroup?.let { intArrayOf(it) } ?: manager.information!!.containersIndexes
            var expected = 0
            var loaded = 0
            for (group in groups) {
                for (file in requireNotNull(manager.getFileIds(group))) {
                    expected++
                    var buffer: ByteBuffer? = null
                    try {
                        buffer = ByteBuffer.wrap(requireNotNull(manager.getFileData(group, file)))
                        decode(group, file, buffer)
                        check(!buffer.hasRemaining()) { "${buffer.remaining()} trailing bytes" }
                        loaded++
                    } catch (e: Exception) {
                        System.err.println("$name $index/$group/$file offset=${buffer?.position()}: $e")
                    }
                }
            }
            failures += expected - loaded
            println("$name $loaded/$expected")
        }

        scan("Item", 19) { g, f, b -> verifyQuests(ItemDefinition.parseDefinition(g shl 8 or f, b).questIds) }
        scan("NPC", 18) { g, f, b -> verifyQuests(NPCDefinition(g shl 7 or f).apply { parse(b) }.questIds) }
        scan("Loc", 16) { g, f, b -> verifyQuests(SceneryDefinition.parseDefinition(g shl 8 or f, b).questIds) }
        scan("Seq", 20) { _, _, b -> AnimationDefinition().readValueLoop(b) }
        scan("SpotAnim", 21) { _, _, b -> GraphicDefinition().readValueLoop(b) }
        scan("BAS", 2, 32) { _, _, b -> RenderAnimationDefinition().parse(b) }
        scan("IDK", 2, 3) { _, _, b -> ClothDefinition().load(b) }
        scan("Varbit", 22) { g, f, b ->
            val varbit = VarbitDefinition(g shl 10 or f).apply { decode(b) }
            check(configs.validFile(16, varbit.varpId)) { "missing varp ${varbit.varpId}" }
            check(varbit.startBit in 0..31 && varbit.endBit in varbit.startBit..31)
        }
        scan("Varp", 2, 16) { _, _, b -> VarpDefinition().decode(b) }
        scan("Struct", 2, 26) { _, f, b -> Struct.decode(f, b) }
        scan("Enum", 17) { g, f, b ->
            val id = g shl 8 or f
            val copy = b.duplicate()
            CS2Mapping(id).load(copy)
            check(!copy.hasRemaining())
            DataMap.decode(id, b)
        }
        scan("Interface", 3) { g, f, b ->
            IfaceDefinition.decodeComponent(IfaceDefinition().apply { id = g shl 16 or f }, b)
            val child = requireNotNull(IfaceDefinition.forId(g, f))
            check(child.id == (g shl 16 or f) && child.parent == g && child.type != null)
        }

        XteaParser().load(File(data, "configs/xteas.json").path)
        val maps = requireNotNull(Cache.getIndexes()[5])
        val info = maps.information!!
        val byHash = info.containersIndexes.associateBy { info.containers[it]!!.nameHash }
        var landscapes = 0
        var decoded = 0
        var terrainGroups = 0
        var decodedTerrain = 0
        var withoutKey = 0
        var unusedKeys = 0
        for (region in 0..0xffff) {
            val terrain = byHash[StringUtils.getNameHash("m${region ushr 8}_${region and 255}")]
            if (terrain != null) {
                terrainGroups++
                if (maps.getFileData(terrain, 0)?.isNotEmpty() == true) decodedTerrain++
                else System.err.println("terrain region=$region group=$terrain: cannot unpack")
            }
            val name = "l${region ushr 8}_${region and 255}"
            val group = byHash[StringUtils.getNameHash(name)]
            val key = XteaParser.REGION_XTEA[region]
            if (group == null) {
                if (key != null) unusedKeys++
                continue
            }
            landscapes++
            if (key == null) withoutKey++
            try {
                val content = requireNotNull(maps.getFileData(group, 0, key))
                val buffer = ByteBuffer.wrap(content)
                while (ByteBufferUtils.getBigSmart(buffer) != 0) {
                    while (ByteBufferUtils.getSmart(buffer) != 0) buffer.get()
                }
                check(!buffer.hasRemaining()) { "trailing landscape bytes: ${buffer.remaining()}" }
                decoded++
            } catch (e: Exception) {
                System.err.println("$name group=$group suppliedKey=${key != null}: $e")
            }
        }
        failures += landscapes - decoded + terrainGroups - decodedTerrain
        println("Terrain $decodedTerrain/$terrainGroups")
        println("Landscape $decoded/$landscapes (without key=$withoutKey, keys without group=$unusedKeys)")
        check(failures == 0) { "$failures records failed validation" }
    }
}
