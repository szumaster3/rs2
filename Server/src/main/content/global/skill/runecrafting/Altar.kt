package content.global.skill.runecrafting

import content.data.Quests

import content.global.skill.runecrafting.item.Rune
import content.global.skill.runecrafting.item.Talisman
import content.global.skill.runecrafting.item.Tiara
import core.api.hasRequirement
import core.api.isQuestComplete
import core.api.sendMessage
import core.cache.def.impl.ObjType
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import shared.consts.Scenery as Objects

/**
 * Represents an altar used in the runecrafting skill.
 */
enum class Altar(
    val scenery: Int,
    val exit: Int,
    val rift: Int,
    val ruin: MysteriousRuins?,
    val rune: Rune?
) {
    AIR(Objects.ALTAR_2478, Objects.PORTAL_2465, Objects.AIR_RIFT_7139, MysteriousRuins.AIR, Rune.AIR),
    MIND(Objects.ALTAR_2479, Objects.PORTAL_2466, Objects.MIND_RIFT_7140, MysteriousRuins.MIND, Rune.MIND),
    WATER(Objects.ALTAR_2480, Objects.PORTAL_2467, Objects.WATER_RIFT_7137, MysteriousRuins.WATER, Rune.WATER),
    EARTH(Objects.ALTAR_2481, Objects.PORTAL_2468, Objects.EARTH_RIFT_7130, MysteriousRuins.EARTH, Rune.EARTH),
    FIRE(Objects.ALTAR_2482, Objects.PORTAL_2469, Objects.FIRE_RIFT_7129, MysteriousRuins.FIRE, Rune.FIRE),
    BODY(Objects.ALTAR_2483, Objects.PORTAL_2470, Objects.BODY_RIFT_7131, MysteriousRuins.BODY, Rune.BODY),
    COSMIC(Objects.ALTAR_2484, Objects.PORTAL_2471, Objects.COSMIC_RIFT_7132, MysteriousRuins.COSMIC, Rune.COSMIC),
    CHAOS(Objects.ALTAR_2487, Objects.PORTAL_2474, Objects.CHAOS_RIFT_7134, MysteriousRuins.CHAOS, Rune.CHAOS),
    ASTRAL(Objects.ALTAR_17010, 0, 0, null, Rune.ASTRAL),
    NATURE(Objects.ALTAR_2486, Objects.PORTAL_2473, Objects.NATURE_RIFT_7133, MysteriousRuins.NATURE, Rune.NATURE),
    LAW(Objects.ALTAR_2485, Objects.PORTAL_2472, Objects.LAW_RIFT_7135, MysteriousRuins.LAW, Rune.LAW),
    DEATH(Objects.ALTAR_2488, Objects.PORTAL_2475, Objects.DEATH_RIFT_7136, MysteriousRuins.DEATH, Rune.DEATH),
    BLOOD(Objects.ALTAR_30624, Objects.PORTAL_2477, Objects.BLOOD_RIFT_7141, MysteriousRuins.BLOOD, Rune.BLOOD),
    OURANIA(Objects.RUNECRAFTING_ALTAR_26847, 0, 0, null, null);

    companion object {
        private val altarByScenery = values().associateBy { it.scenery }
        private val altarByExit = values().associateBy { it.exit }
        private val altarByRiftId = values().associateBy { it.rift }

        /**
         * Retrieves the corresponding altar based on the given scenery object.
         */
        fun forScenery(scenery: Scenery): Altar? {
            return altarByScenery[scenery.id]
                ?: altarByExit[scenery.id]
                ?: altarByRiftId[scenery.id]
        }
    }

    /**
     * Makes the player enter the rift for the current altar.
     */
    fun enterRift(player: Player) {
        when (this) {
            ASTRAL -> if (!hasRequirement(player, Quests.LUNAR_DIPLOMACY.questName)) return
            DEATH -> if (!hasRequirement(player, Quests.MOURNINGS_END_PART_II.questName)) return
            BLOOD -> if (!hasRequirement(player, Quests.LEGACY_OF_SEERGAZE.questName)) return
            LAW -> if (!ObjType.canEnterEntrana(player)) {
                sendMessage(player, "You can't take weapons and armour into the law rift.", null)
                return
            }
            COSMIC -> if (!isQuestComplete(player, Quests.LOST_CITY.questName)) {
                sendMessage(player, "You need to have completed the Lost City quest in order to do that.", null)
                return
            }
            else -> {}
        }
        ruin?.let { player.properties.teleportLocation = it.end }
    }

    /**
     * Determines if the current altar is the Ourania altar.
     */
    fun isOurania(): Boolean = rune == null

    /**
     * Retrieves the talisman associated with the current altar.
     */
    fun getTalisman(): Talisman? = Talisman.values().find { it.name.equals(this.name, ignoreCase = true) }

    /**
     * Retrieves the tiara associated with the current altar.
     */
    fun getTiara(): Tiara? = Tiara.values().find { it.name.equals(this.name, ignoreCase = true) }
}
