package content.global.activity.champions.plugin

import content.data.GameAttributes
import core.api.*
import core.game.container.impl.EquipmentContainer
import core.game.dialogue.FaceAnim
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.system.config.ItemConfigParser
import core.game.world.GameWorld
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Vars

/**
 * Handles Champion challenge NPC.
 */
@Initializable
class ChampionChallengeNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    private var lastTick = 0

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC =
        ChampionChallengeNPC(id, location)

    override fun getIds(): IntArray = styleRules.keys.toIntArray()

    override fun checkImpact(state: BattleState) {
        super.checkImpact(state)
        val player = state.attacker as? Player ?: return
        val rule = styleRules[id] ?: return

        val now = GameWorld.ticks
        fun block(message: String) {
            state.neutralizeHits()
            if (now - lastTick > 10) {
                sendMessage(player, message, now)
                lastTick = now
            }
        }

        when (id) {
            NPCs.CHAMPIONS_IMP_3062 -> player.equipment[EquipmentContainer.SLOT_WEAPON]
                ?.takeIf { it.definition.getConfiguration(ItemConfigParser.HAS_SPECIAL, false) }
                ?.let { return block(rule.message) }

            NPCs.CHAMPIONS_GHOUL_3059, NPCs.LEON_DCOUR_3067 -> {
                val used = getUsedOption(player)
                if (freeSlots(player) != 28 || used in listOf("pick", "take")) return block(rule.message)
            }

            NPCs.CHAMPIONS_LESSERDEMON_3064 -> {
                if (getUsedOption(player) in listOf("equip", "wield", "wear", "hold")) return block(rule.message)
            }

            NPCs.CHAMPIONS_EARTHWARRIOR_3057 -> {
                player.prayer?.reset()
                return sendMessage(player, rule.message, now)
            }
        }

        val style = state.style
        if (rule.banned?.contains(style) == true || (rule.allowed != null && style !in rule.allowed))
            block(rule.message)
    }

    /**
     * Handles the post-victory Leon D'Cour scenario.
     */
    override fun isHidden(player: Player): Boolean =
        id == NPCs.LEON_DCOUR_3067 &&
                !getAttribute(player, GameAttributes.ACTIVITY_CHAMPIONS_COMPLETE, false)


    override fun finalizeDeath(killer: Entity?) {
        if (killer is Player) {
            removeAttributes(killer, ACTIVE_CHAMPION_KEY, GameAttributes.PRAYER_LOCK)
            lock(killer, 2)
            playJingle(killer, 85)
            openInterface(killer, Components.CHAMPIONS_SCROLL_63)

            val config = ChampionDefinition.values().firstOrNull { it.npcId == id } ?: return
            val defeatAll =
                getAttribute(killer, GameAttributes.ACTIVITY_CHAMPIONS_CHALLENGE_DEFEAT_ALL, false)

            // Removing the scroll after the fight.
            val scrollId = ChampionChallengePlugin.getActiveChampionScroll(killer)
            if (scrollId != null && scrollId == config.scrollId) {
                removeItem(killer, scrollId)
            }

            sendString(killer, "Well done, you defeated the ${getNPCName(id)}!", Components.CHAMPIONS_SCROLL_63, 2)
            sendItemZoomOnInterface(killer, Components.CHAMPIONS_SCROLL_63, 3, config.scrollId, 260)
            sendString(killer, "${config.xp.toInt()} Slayer Xp", Components.CHAMPIONS_SCROLL_63, 6)
            sendString(killer, "${config.xp.toInt()} Hitpoint Xp", Components.CHAMPIONS_SCROLL_63, 7)

            // Reward and placement of banners on the wall.
            config.varbitId?.let { setVarbit(killer, it, 1, true) }
            rewardXP(killer, Skills.HITPOINTS, config.xp)
            rewardXP(killer, Skills.SLAYER, config.xp)

            // Checking if the player has defeated all champions and can start last fight.
            if ((Vars.VARBIT_CHAMPIONS_CHALLENGE_EARTH_WARRIOR_1452..Vars.VARBIT_CHAMPIONS_CHALLENGE_ZOMBIE_1461)
                    .all { getVarbit(killer, it) == 1 && !defeatAll }
            ) {
                setAttribute(killer, GameAttributes.ACTIVITY_CHAMPIONS_CHALLENGE_DEFEAT_ALL, true)
                sendNPCDialogueLines(killer, NPCs.LEON_DCOUR_3067, FaceAnim.NEUTRAL, false, "You have done well brave adventurer, but I would test", "your mettle now. You may arrange the fight with", "Larxus at your leisure.")
            }

            // Complete activity.
            if (config.npcId == NPCs.LEON_DCOUR_3067) {
                removeAttribute(killer, GameAttributes.ACTIVITY_CHAMPIONS_CHALLENGE_DEFEAT_ALL)
                setAttribute(killer, GameAttributes.ACTIVITY_CHAMPIONS_COMPLETE, true)
            }
            clearHintIcon(killer)
        }
        clear()
        super.finalizeDeath(killer)
    }

    companion object {
        private const val ACTIVE_CHAMPION_KEY = "active_champion_npc"

        /**
         * The list of banned items for earth warrior champion.
         */
        private val prayerItems = intArrayOf(
            Items.PRAYER_POTION_1_143, Items.PRAYER_POTION_1_144, Items.PRAYER_POTION_2_141, Items.PRAYER_POTION_2_142,
            Items.PRAYER_POTION_3_139, Items.PRAYER_POTION_3_140, Items.PRAYER_POTION_4_2434, Items.PRAYER_POTION_4_2435,
            Items.SUPER_RESTORE_1_3030, Items.SUPER_RESTORE_1_3031, Items.SUPER_RESTORE_2_3028, Items.SUPER_RESTORE_2_3029,
            Items.SUPER_RESTORE_3_3026, Items.SUPER_RESTORE_3_3027, Items.SUPER_RESTORE_4_3024, Items.SUPER_RESTORE_4_3025,
            Items.PRAYER_CAPE_9759, Items.PRAYER_CAPE_T_9760, Items.PRAYER_HOOD_9761, Items.PRAYER_CAPE_10643,
            Items.PRAYER_POTION_4_14209, Items.PRAYER_POTION_4_14210, Items.PRAYER_POTION_3_14211, Items.PRAYER_POTION_3_14212,
            Items.PRAYER_POTION_2_14213, Items.PRAYER_POTION_2_14214, Items.PRAYER_POTION_1_14215, Items.PRAYER_POTION_1_14216,
            Items.FALADOR_SHIELD_1_14577, Items.FALADOR_SHIELD_2_14578, Items.FALADOR_SHIELD_3_14579,
            Items.PRAYER_MIX_1_11467, Items.PRAYER_MIX_1_11468, Items.PRAYER_MIX_2_11465, Items.PRAYER_MIX_2_11466,
            Items.SUP_RESTORE_MIX_1_11495, Items.SUP_RESTORE_MIX_1_11496, Items.SUP_RESTORE_MIX_2_11493, Items.SUP_RESTORE_MIX_2_11494
        )

        /**
         * Spawns a champion NPC for the player's current challenge.
         *
         * @param player The challenger.
         * @param entry The [ChampionDefinition] entry to spawn.
         */
        fun spawnChampion(player: Player, entry: ChampionDefinition) {
            val existing  = getAttribute(player, ACTIVE_CHAMPION_KEY, false) as? ChampionChallengeNPC
            if(existing != null && existing.isActive){
                return
            }

            if (entry.npcId == NPCs.CHAMPIONS_EARTHWARRIOR_3057 && player.hasPrayerItems()) {
                sendNPCDialogue(player, NPCs.LARXUS_3050, "For this fight you're not allowed to use prayers!")
                return
            }

            val champion = ChampionChallengeNPC(entry.npcId).apply {
                location = location(3170, 9758, 0)
                isWalks = true
                isNeverWalks = false
                isRespawn = false
                isAggressive = true
                isActive = false
            }

            champion.properties?.let {
                if (champion.asNpc() != null && champion.isActive)
                    it.teleportLocation = it.spawnLocation
            }

            setAttribute(player, ACTIVE_CHAMPION_KEY, champion)
            champion.isActive = true

            champion.init()
            registerHintIcon(player, champion)
            champion.attack(player)
        }

        /**
         * Checks whether the player carries any prayer-related items (in inventory or equipment).
         */
        private fun Player.hasPrayerItems(): Boolean =
            inventory.containsAtLeastOneItem(prayerItems) || equipment.containsAtLeastOneItem(prayerItems)

        /**
         * Configuration of combat rules for each Champion NPC.
         */
        private val styleRules = mapOf(
            NPCs.CHAMPIONS_GIANT_3058 to StyleRule(setOf(CombatStyle.MELEE), setOf(CombatStyle.MAGIC, CombatStyle.RANGE), "Larxus said you could use only Melee in this duel."),
            NPCs.CHAMPIONS_GOBLIN_3060 to StyleRule(setOf(CombatStyle.MAGIC), setOf(CombatStyle.MELEE, CombatStyle.RANGE), "Larxus said you could use only Spells in this duel."),
            NPCs.CHAMPIONS_HOBGOBLIN_3061 to StyleRule(setOf(CombatStyle.MAGIC, CombatStyle.RANGE), setOf(CombatStyle.MELEE), "Larxus said you couldn't use Melee in this duel."),
            NPCs.CHAMPIONS_IMP_3062 to StyleRule(null, null, "Larxus said you couldn't use Special Attacks in this duel."),
            NPCs.CHAMPIONS_JOGRE_3063 to StyleRule(setOf(CombatStyle.MAGIC, CombatStyle.MELEE), setOf(CombatStyle.RANGE), "Larxus said you couldn't use Ranged Weapons."),
            NPCs.CHAMPIONS_ZOMBIE_3066 to StyleRule(setOf(CombatStyle.MELEE, CombatStyle.RANGE), setOf(CombatStyle.MAGIC), "Larxus said you couldn't use Spells in this duel."),
            NPCs.CHAMPIONS_SKELETON_3065 to StyleRule(setOf(CombatStyle.RANGE), setOf(CombatStyle.MELEE, CombatStyle.MAGIC), "Larxus said you could use only Ranged Weapons in this duel."),
            NPCs.CHAMPIONS_GHOUL_3059 to StyleRule(null, null, "You can only fight with weapons, no inventory items."),
            NPCs.LEON_DCOUR_3067 to StyleRule(null, null, "You can only fight with weapons, no inventory items."),
            NPCs.CHAMPIONS_LESSERDEMON_3064 to StyleRule(null, null, "You cannot wear armour or weapons; only inventory items allowed."),
            NPCs.CHAMPIONS_EARTHWARRIOR_3057 to StyleRule(null, null, "For this fight you're not allowed to use prayers!")
        )
    }
}

/**
 * Defines a combat rule configuration for a Champion NPC.
 *
 * @param allowed Allowed [CombatStyle]s, or null if unrestricted.
 * @param banned Banned [CombatStyle]s, or null if unrestricted.
 * @param message Message displayed when a rule is broken.
 */
private data class StyleRule(
    val allowed: Set<CombatStyle>? = null,
    val banned: Set<CombatStyle>? = null,
    val message: String
)