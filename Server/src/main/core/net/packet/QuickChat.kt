package core.net.packet

import core.api.getQuestPoints
import core.api.getVarbit
import core.api.getAttribute
import content.data.GameAttributes
import content.global.activity.champions.plugin.ChampionDefinition
import core.cache.def.impl.VarbitType
import content.global.activity.penguinhns.PenguinManager
import content.global.skill.slayer.SlayerManager
import content.global.skill.slayer.Tasks
import core.cache.Cache
import core.game.node.entity.player.Player
import java.nio.ByteBuffer

/**
 * Phrase definitions and client/server parameter widths.
 */
object QuickChat {
    private val inputWidths = intArrayOf(2,2,4,0,1,8,0,0,0,0,2,0,0,0,0,0)
    private val outputWidths = intArrayOf(2,2,4,2,1,8,4,1,4,4,2,1,1,1,4,1)
    private val parameterCounts = intArrayOf(1,0,0,0,1,0,2,1,1,1,0,2,0,0,1,0)

    // Explicit enum_1563 keys; never use persisted Tasks.ordinal on the wire.
    private val slayerKeys = mapOf(
        Tasks.ABERRANT_SPECTRES to 41,
        Tasks.ABYSSAL_DEMONS to 42,
        Tasks.ANKOU to 79,
        Tasks.BANSHEE to 38,
        Tasks.BASILISKS to 43,
        Tasks.BATS to 8,
        Tasks.BEARS to 13,
        Tasks.BIRDS to 5,
        Tasks.BLACK_DEMONS to 30,
        Tasks.BLACK_DRAGONS to 27,
        Tasks.BLOODVELDS to 48,
        Tasks.BLUE_DRAGONS to 25,
        Tasks.BRINE_RATS to 84,
        Tasks.BRONZE_DRAGONS to 58,
        Tasks.CATABLEPONS to 78,
        Tasks.CAVE_BUG to 63,
        Tasks.CAVE_CRAWLERS to 37,
        Tasks.CAVE_HORRORS to 80,
        Tasks.CAVE_SLIMES to 62,
        Tasks.COCKATRICES to 44,
        Tasks.COWS to 6,
        Tasks.CRAWLING_HAND to 39,
        Tasks.CROCODILES to 65,
        Tasks.DAGANNOTHS to 35,
        Tasks.DARK_BEASTS to 66,
        Tasks.DESERT_LIZARDS to 68,
        Tasks.DOG to 22,
        Tasks.DUST_DEVILS to 49,
        Tasks.DWARF to 57,
        Tasks.EARTH_WARRIORS to 54,
        Tasks.ELVES to 56,
        Tasks.FIRE_GIANTS to 16,
        Tasks.FLESH_CRAWLERS to 77,
        Tasks.GARGOYLES to 46,
        Tasks.GHOSTS to 12,
        Tasks.GHOULS to 23,
        Tasks.GOBLINS to 2,
        Tasks.GORAKS to 82,
        Tasks.GREATER_DEMONS to 29,
        Tasks.GREEN_DRAGONS to 24,
        Tasks.HARPIE_BUG_SWARMS to 70,
        Tasks.HELLHOUNDS to 31,
        Tasks.HILL_GIANTS to 14,
        Tasks.HOBGOBLINS to 21,
        Tasks.ICE_FIENDS to 75,
        Tasks.ICE_GIANTS to 15,
        Tasks.ICE_WARRIOR to 19,
        Tasks.INFERNAL_MAGES to 40,
        Tasks.IRON_DRAGONS to 59,
        Tasks.JELLIES to 50,
        Tasks.JUNGLE_HORRORS to 81,
        Tasks.KALPHITES to 53,
        Tasks.KURASKS to 45,
        Tasks.LESSER_DEMONS to 28,
        Tasks.MITHRIL_DRAGONS to 94,
        Tasks.MINOTAURS to 76,
        Tasks.MONKEYS to 1,
        Tasks.MOSS_GIANTS to 17,
        Tasks.NECHRYAELS to 52,
        Tasks.OGRES to 20,
        Tasks.OTHERWORDLY_BEING to 55,
        Tasks.PYREFIENDS to 47,
        Tasks.RATS to 3,
        Tasks.ROCK_SLUGS to 51,
        Tasks.SCORPIONS to 7,
        Tasks.SHADE to 64,
        Tasks.SKELETONS to 11,
        Tasks.SPIDERS to 4,
        Tasks.SPIRTUAL_MAGES to 91,
        Tasks.SPIRTUAL_RANGERS to 90,
        Tasks.SPIRTUAL_WARRIORS to 89,
        Tasks.STEEL_DRAGONS to 60,
        Tasks.TROLLS to 18,
        Tasks.TUROTHS to 36,
        Tasks.TZHAAR to 101,
        Tasks.ZYGOMITES to 74,
        Tasks.SHADES to 64,
        Tasks.MOGRES to 67,
        Tasks.SUQAHS to 83,
        Tasks.VAMPIRES to 34,
        Tasks.WATERFIENDS to 88,
        Tasks.WEREWOLFS to 33,
        Tasks.WOLVES to 9,
        Tasks.ZOMBIES to 10,
        Tasks.SKELETAL_WYVERN to 72,
    )

    internal fun slayerKey(task: Tasks?): Int? = if (task == null) 0 else slayerKeys[task]

    fun encode(player: Player, id: Int, input: ByteArray): ByteArray? {
        if (id !in 0..65535) return null
        val data = Cache.getIndexes().getOrNull(if (id < 32768) 24 else 25)
            ?.getFileData(1, id and 32767) ?: return null
        return encodeDefinition(id, data, input) { command, params ->
            when (command) {
                // stat_base and enum_string_statbase use the actual base level.
                4, 11 -> params.last().takeIf { it in 0..23 }?.let { player.skills.getStaticLevel(it).toLong() }
                6 -> if (params.contentEquals(intArrayOf(1563, 395)))
                    slayerKey(SlayerManager.getInstance(player).activeTask)?.toLong() else null
                7 -> if (params[0] == 1564) {
                    val clan = player.communication.clan
                    // The client casts the unsigned byte to int, so negative enum keys
                    // cannot be represented by this command. Do not send 254/255.
                    clan?.getRank(player)?.value?.takeIf { it >= 0 }?.toLong()
                } else null
                9 -> when (params[0]) {
                    5276 -> PenguinManager.tagMapping.values.count { tags ->
                        tags.any { it.asString == player.username.lowercase() }
                    }.toLong()
                    6764 -> if (ChampionDefinition.values().all { it.varbitId == null || VarbitType.exists(it.varbitId) }) {
                        val defeated = ChampionDefinition.values().count { champion ->
                            champion.varbitId?.let { getVarbit(player, it) == 1 }
                                ?: getAttribute(player, GameAttributes.ACTIVITY_CHAMPIONS_COMPLETE, false)
                        }
                        defeated.toLong()
                    } else null
                    else -> null
                }
                // Quest points are authoritative in the quest repository, not a client varp.
                8 -> when (params[0]) {
                    101 -> getQuestPoints(player).toLong()
                    1405 -> player.savedData.activityData.fogRating.toLong()
                    else -> null
                }
                15 -> player.properties.currentCombatLevel.toLong()
                else -> null
            }
        }
    }

    private data class Command(val id: Int, val params: IntArray)

    internal fun encodeDefinition(id: Int, data: ByteArray, input: ByteArray,
                                  resolve: (Int, IntArray) -> Long? = { _, _ -> null }): ByteArray? {
        if (id !in 0..65535) return null
        return try {
            val definition = ByteBuffer.wrap(data)
            var textSlots = -1
            var commands = emptyList<Command>()
            while (true) {
                when (definition.get().toInt() and 255) {
                    0 -> break
                    1 -> {
                        textSlots = 0
                        while (true) {
                            val c = definition.get().toInt() and 255
                            if (c == 0) break
                            if (c == 60) textSlots++
                        }
                    }
                    2 -> repeat(definition.get().toInt() and 255) { definition.short }
                    3 -> commands = List(definition.get().toInt() and 255) {
                        val command = definition.short.toInt() and 65535
                        if (command !in parameterCounts.indices) return null
                        Command(command, IntArray(parameterCounts[command]) { definition.short.toInt() and 65535 })
                    }
                    4 -> Unit
                    else -> return null
                }
            }
            if (definition.hasRemaining() || textSlots != commands.size) return null
            if (commands.sumOf { inputWidths[it.id] } != input.size) return null
            val result = ByteBuffer.allocate(2 + commands.sumOf { outputWidths[it.id] })
            result.putShort(id.toShort())
            var offset = 0
            for ((command, params) in commands) {
                val width = inputWidths[command]
                val outputWidth = outputWidths[command]
                if (width == 0 || command == 4) {
                    val value = resolve(command, params) ?: return null
                    if (value < 0 || (outputWidth < 8 && value ushr (outputWidth * 8) != 0L)) return null
                    for (shift in (outputWidth - 1) downTo 0) result.put((value ushr (shift * 8)).toByte())
                } else {
                    repeat(outputWidth - width) { result.put(0.toByte()) }
                    result.put(input, offset, width)
                }
                offset += width
            }
            result.array()
        } catch (_: java.nio.BufferUnderflowException) { null }
    }
}
