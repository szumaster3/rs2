package content.global.activity.champions.plugin

import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Vars

/**
 * The champion's challenge data.
 *
 * Champion     | Level | Hitpoints | Restrictions                         | XP
 * ------------ | ----- | --------- | ------------------------------------ | ---
 * Imp          | 14    | 40        | No special attacks                   | 160
 * Goblin       | 24    | 32        | Only Magic                           | 128
 * Skeleton     | 40    | 58        | Only Ranged                          | 232
 * Zombie       | 51    | 60        | No Magic Attacks                     | 240
 * Hobgoblin    | 56    | 58        | No Melee attacks                     | 232
 * Giant        | 56    | 70        | Only Melee                           | 280
 * Jogre        | 107   | 120       | No Ranged attacks                    | 480
 * Ghoul        | 85    | 100       | Weapons only, no inventory           | 400
 * Earth        | 102   | 108       | No prayers                           | 432
 * Lesser Demon | 162   | 148       | No armour or weapons; only inventory | 592
 * Leon d'Cour  | 141   | 123       | Weapons only, no inventory           | 492
 */
enum class ChampionDefinition(val scrollId: Int, val npcId: Int, val xp: Double, val varbitId: Int?) {
    EARTH_WARRIOR(Items.CHAMPIONS_SCROLL_6798, NPCs.CHAMPIONS_EARTHWARRIOR_3057, 432.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_EARTH_WARRIOR_1452),
    GHOUL(Items.CHAMPIONS_SCROLL_6799, NPCs.CHAMPIONS_GHOUL_3059, 400.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_GHOUL_1453),
    GIANT(Items.CHAMPIONS_SCROLL_6800, NPCs.CHAMPIONS_GIANT_3058, 280.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_GIANT_1454),
    GOBLIN(Items.CHAMPIONS_SCROLL_6801, NPCs.CHAMPIONS_GOBLIN_3060, 128.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_GOBLIN_1455),
    HOBGOBLIN(Items.CHAMPIONS_SCROLL_6802, NPCs.CHAMPIONS_HOBGOBLIN_3061, 232.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_HOBGOBLIN_1456),
    IMP(Items.CHAMPIONS_SCROLL_6803, NPCs.CHAMPIONS_IMP_3062, 160.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_IMP_1457),
    JOGRE(Items.CHAMPIONS_SCROLL_6804, NPCs.CHAMPIONS_JOGRE_3063, 480.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_JOGRE_1458),
    LESSER_DEMON(Items.CHAMPIONS_SCROLL_6805, NPCs.CHAMPIONS_LESSERDEMON_3064, 592.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_LESSER_DEMON_1459),
    SKELETON(Items.CHAMPIONS_SCROLL_6806, NPCs.CHAMPIONS_SKELETON_3065, 232.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_SKELETON_1460),
    ZOMBIE(Items.CHAMPIONS_SCROLL_6807, NPCs.CHAMPIONS_ZOMBIE_3066, 240.0, Vars.VARBIT_CHAMPIONS_CHALLENGE_ZOMBIE_1461),
    LEON(-1, NPCs.LEON_DCOUR_3067, 492.0, null);

    companion object {
        private val byScroll = values().associateBy { it.scrollId }
        fun fromScroll(scrollId: Int) = byScroll[scrollId]
    }
}
