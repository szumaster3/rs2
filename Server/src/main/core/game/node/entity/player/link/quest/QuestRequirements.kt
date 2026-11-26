package core.game.node.entity.player.link.quest

import content.data.Quests

import core.api.getStatLevel
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import kotlin.math.min

interface Requirements {
    fun evaluate(player: Player): Pair<Boolean, List<Requirements>>
}

open class SkillReq(
    val skillId: Int,
    val level: Int,
    val soft: Boolean = false,
) : Requirements {
    override fun evaluate(player: Player): Pair<Boolean, List<Requirements>> {
        val hasLevelRequirement = getStatLevel(entity = player, skill = skillId) >= level
        if (hasLevelRequirement) {
            return Pair(true, listOf())
        } else {
            return Pair(false, listOf(this))
        }
    }
}

open class QuestReq(
    val questReq: QuestRequirements,
    val stageRequired: Int = 100,
) : Requirements {
    override fun evaluate(player: Player): Pair<Boolean, List<Requirements>> {
        val quest = player.questRepository.getQuest(questReq.questName)
        val unmetRequirements = ArrayList<Requirements>()
        var isMet = true
        if (quest == null) {
            for (req in questReq.requirements) {
                val (met, reqs) = req.evaluate(player)
                isMet = isMet && met
                unmetRequirements.addAll(reqs)
            }
            unmetRequirements.add(QPCumulative(5))
        } else {
            isMet = quest.getStage(player) >= stageRequired
            if (!isMet) unmetRequirements.add(this)
            unmetRequirements.add(QPCumulative(quest.questPoints))
        }

        return Pair(isMet, unmetRequirements)
    }
}

open class QPReq(
    val amount: Int,
) : Requirements {
    override fun evaluate(player: Player): Pair<Boolean, List<Requirements>> {
        val needed = min(amount, player.questRepository.getAvailablePoints())
        val hasNeeded = player.questRepository.points >= needed

        return Pair(hasNeeded, if (hasNeeded) listOf() else listOf(this))
    }
}

open class QPCumulative(
    val amount: Int,
) : Requirements {
    override fun evaluate(player: Player): Pair<Boolean, List<Requirements>> = Pair(false, listOf(this))
}

enum class QuestRequirements(
    val questName: String,
    vararg val requirements: Requirements,
) {
    COOK_ASSIST(Quests.COOKS_ASSISTANT.questName),
    DEMON_SLAYER(Quests.DEMON_SLAYER.questName),
    DORIC_QUEST(Quests.DORICS_QUEST.questName),
    DRAGON_SLAYER(Quests.DRAGON_SLAYER.questName, QPReq(32)),
    ERNEST(Quests.ERNEST_THE_CHICKEN.questName),
    GOBLIN_DIP(Quests.GOBLIN_DIPLOMACY.questName),
    IMP_CATCHER(Quests.IMP_CATCHER.questName),
    KNIGHT_SWORD(Quests.THE_KNIGHTS_SWORD.questName, SkillReq(Skills.MINING, 10, true),),
    PIRATE_T(Quests.PIRATES_TREASURE.questName),
    ALI_RESCUE(Quests.PRINCE_ALI_RESCUE.questName),
    RESTLESS_GHOST(Quests.THE_RESTLESS_GHOST.questName),
    ROMEO(Quests.ROMEO_JULIET.questName),
    SHEEP(Quests.SHEEP_SHEARER.questName),
    ARRAV(Quests.SHIELD_OF_ARRAV.questName),
    VAMPIRE(Quests.VAMPIRE_SLAYER.questName),
    DORIC(Quests.DORICS_QUEST.questName),
    RUNE_MYSTERIES(Quests.RUNE_MYSTERIES.questName),
    BLACK_KNIGHT(Quests.BLACK_KNIGHTS_FORTRESS.questName, QPReq(12),),
    WITCH_POTION(Quests.WITCHS_POTION.questName),
    DRUIDIC_RITUAL(Quests.DRUIDIC_RITUAL.questName),
    LOST_CITY(Quests.LOST_CITY.questName, SkillReq(Skills.CRAFTING, 31, true), SkillReq(Skills.WOODCUTTING, 36, true)),
    WITCH_HOUSE(Quests.WITCHS_HOUSE.questName),
    MERLIN(Quests.MERLINS_CRYSTAL.questName),
    HERO(Quests.HEROES_QUEST.questName, QPReq(55), SkillReq(Skills.COOKING, 53, true), SkillReq(Skills.FISHING, 53, true), SkillReq(Skills.HERBLORE, 25, true), SkillReq(Skills.MINING, 50, true), QuestReq(ARRAV), QuestReq(LOST_CITY), QuestReq(MERLIN), QuestReq(DRAGON_SLAYER)),
    SCORP_CATCHER(Quests.SCORPION_CATCHER.questName, SkillReq(Skills.PRAYER, 31)),
    FAMILY_CREST(Quests.FAMILY_CREST.questName, SkillReq(Skills.MINING, 40, true), SkillReq(Skills.SMITHING, 40, true), SkillReq(Skills.MAGIC, 59, true), SkillReq(Skills.CRAFTING, 40, true)),
    FISHING_CONTEST(Quests.FISHING_CONTEST.questName, SkillReq(Skills.FISHING, 10)),
    TOTEM(Quests.TRIBAL_TOTEM.questName, SkillReq(Skills.THIEVING, 21)),
    MONK(Quests.MONKS_FRIEND.questName),
    IKOV(Quests.TEMPLE_OF_IKOV.questName, SkillReq(Skills.THIEVING, 42, true), SkillReq(Skills.RANGE, 40)),
    CLOCK_TOWER(Quests.CLOCK_TOWER.questName),
    GRAIL(Quests.HOLY_GRAIL.questName, QuestReq(MERLIN), SkillReq(Skills.ATTACK, 20)),
    GNOME_VILLAGE(Quests.TREE_GNOME_VILLAGE.questName),
    FIGHT_ARENA(Quests.FIGHT_ARENA.questName),
    HAZEEL(Quests.HAZEEL_CULT.questName),
    SHEEP_HERDER(Quests.SHEEP_HERDER.questName),
    PLAGUE_CITY(Quests.PLAGUE_CITY.questName),
    SEA_SLUG(Quests.SEA_SLUG.questName, SkillReq(Skills.FIREMAKING, 30, true)),
    WATERFALL(Quests.WATERFALL_QUEST.questName),
    POTION(Quests.JUNGLE_POTION.questName, SkillReq(Skills.HERBLORE, 3, true), QuestReq(DRUIDIC_RITUAL)),
    GRAND_TREE(Quests.THE_GRAND_TREE.questName, SkillReq(Skills.AGILITY, 25, true)),
    BIOHAZARD(Quests.BIOHAZARD.questName, QuestReq(PLAGUE_CITY)),
    UNDERGROUND_PASS(Quests.UNDERGROUND_PASS.questName, SkillReq(Skills.RANGE, 25), QuestReq(BIOHAZARD), QuestReq(PLAGUE_CITY)),
    OBSERVATORY(Quests.OBSERVATORY_QUEST.questName),
    TOURIST(Quests.THE_TOURIST_TRAP.questName, SkillReq(Skills.FLETCHING, 10, true), SkillReq(Skills.SMITHING, 20, true)),
    WATCHTOWER(Quests.WATCHTOWER.questName, SkillReq(Skills.MAGIC, 14, true), SkillReq(Skills.THIEVING, 15, true), SkillReq(Skills.AGILITY, 25, true), SkillReq(Skills.HERBLORE, 14, true), SkillReq(Skills.MINING, 40, true)),
    DWARF_CANNON(Quests.DWARF_CANNON.questName),
    MURDER_MYS(Quests.MURDER_MYSTERY.questName),
    DIG_SITE(Quests.THE_DIG_SITE.questName, SkillReq(Skills.AGILITY, 10, true), SkillReq(Skills.HERBLORE, 10, true), SkillReq(Skills.THIEVING, 25, true)),
    GERTRUDE(Quests.GERTRUDES_CAT.questName),
    SHILO(Quests.SHILO_VILLAGE.questName, QuestReq(POTION), SkillReq(Skills.CRAFTING, 20, true), SkillReq(Skills.AGILITY, 32, true)),
    LEGEND(Quests.LEGENDS_QUEST.questName, QPReq(107), SkillReq(Skills.AGILITY, 50, true), SkillReq(Skills.CRAFTING, 50, true), SkillReq(Skills.HERBLORE, 45, true), SkillReq(Skills.MAGIC, 56, true), SkillReq(Skills.MINING, 52, true), SkillReq(Skills.PRAYER, 42, true), SkillReq(Skills.SMITHING, 50, true), SkillReq(Skills.STRENGTH, 50, true), SkillReq(Skills.THIEVING, 50, true), SkillReq(Skills.WOODCUTTING, 50, true), QuestReq(FAMILY_CREST), QuestReq(HERO), QuestReq(SHILO), QuestReq(UNDERGROUND_PASS), QuestReq(WATERFALL)),
    DEATH_PLATEAU(Quests.DEATH_PLATEAU.questName),
    TROLL_STRONGHOLD(Quests.DEATH_PLATEAU.questName, QuestReq(DEATH_PLATEAU), SkillReq(Skills.AGILITY, 15, true)),
    EADGAR(Quests.EADGARS_RUSE.questName, QuestReq(DRUIDIC_RITUAL), QuestReq(TROLL_STRONGHOLD), SkillReq(Skills.HERBLORE, 31, true)),
    CHOMPY(Quests.BIG_CHOMPY_BIRD_HUNTING.questName, SkillReq(Skills.FLETCHING, 5, true), SkillReq(Skills.COOKING, 30, true), SkillReq(Skills.RANGE, 30, false)),
    ELEMENTAL_W1(Quests.ELEMENTAL_WORKSHOP_I.questName, SkillReq(Skills.MINING, 20, true), SkillReq(Skills.SMITHING, 20, true), SkillReq(Skills.CRAFTING, 20, true)),
    PRIEST(Quests.PRIEST_IN_PERIL.questName),
    NATURE_SPIRIT(Quests.NATURE_SPIRIT.questName, QuestReq(PRIEST), QuestReq(RESTLESS_GHOST)),
    REGICIDE(Quests.REGICIDE.questName, QuestReq(UNDERGROUND_PASS), SkillReq(Skills.CRAFTING, 10), SkillReq(Skills.AGILITY, 56, true)),
    TAI_BWO(Quests.TAI_BWO_WANNAI_TRIO.questName, SkillReq(Skills.AGILITY, 15, true), SkillReq(Skills.COOKING, 30), SkillReq(Skills.FISHING, 65, true), QuestReq(POTION)),
    SHADES(Quests.SHADES_OF_MORTTON.questName, QuestReq(PRIEST), SkillReq(Skills.CRAFTING, 20, true), SkillReq(Skills.HERBLORE, 15, true), SkillReq(Skills.FIREMAKING, 5, true)),
    FREM_TRIALS(Quests.THE_FREMENNIK_TRIALS.questName, SkillReq(Skills.FLETCHING, 25, true), SkillReq(Skills.WOODCUTTING, 40, true), SkillReq(Skills.CRAFTING, 40, true)),
    HORROR_DEEP(Quests.HORROR_FROM_THE_DEEP.questName, SkillReq(Skills.AGILITY, 35, true)),
    THRONE(Quests.THRONE_OF_MISCELLANIA.questName, QuestReq(HERO), QuestReq(FREM_TRIALS)),
    MONKEY(Quests.MONKEY_MADNESS.questName, QuestReq(GRAND_TREE), QuestReq(GNOME_VILLAGE)),
    MINE(Quests.HAUNTED_MINE.questName, QuestReq(PRIEST), SkillReq(Skills.CRAFTING, 35, true)),
    TROLL_ROMANCE(Quests.TROLL_ROMANCE.questName, QuestReq(TROLL_STRONGHOLD), SkillReq(Skills.AGILITY, 28, true)),
    SEARCH_MYREQUE(Quests.IN_SEARCH_OF_THE_MYREQUE.questName, QuestReq(NATURE_SPIRIT), SkillReq(Skills.AGILITY, 25, true)),
    FENKENSTRAIN(Quests.CREATURE_OF_FENKENSTRAIN.questName, QuestReq(PRIEST), QuestReq(RESTLESS_GHOST), SkillReq(Skills.THIEVING, 25, true), SkillReq(Skills.CRAFTING, 20, true)),
    ROVING_ELVES(Quests.ROVING_ELVES.questName, QuestReq(REGICIDE), QuestReq(WATERFALL), SkillReq(Skills.AGILITY, 56, true)),
    GHOSTS_AHOY(Quests.GHOSTS_AHOY.questName, QuestReq(PRIEST), QuestReq(RESTLESS_GHOST), SkillReq(Skills.AGILITY, 25, true), SkillReq(Skills.COOKING, 20, true)),
    FAVOR(Quests.ONE_SMALL_FAVOUR.questName, QuestReq(RUNE_MYSTERIES), QuestReq(SHILO), SkillReq(Skills.AGILITY, 36, true), SkillReq(Skills.CRAFTING, 25, true), SkillReq(Skills.HERBLORE, 18, true), SkillReq(Skills.SMITHING, 30, true)),
    MOUNTAIN_DAUGHTER(Quests.MOUNTAIN_DAUGHTER.questName, SkillReq(Skills.AGILITY, 20, true)),
    BETWEEN_ROCK(Quests.BETWEEN_A_ROCK.questName, QuestReq(DWARF_CANNON), QuestReq(FISHING_CONTEST), SkillReq(Skills.DEFENCE, 30, true), SkillReq(Skills.MINING, 40, true), SkillReq(Skills.SMITHING, 50, true)),
    FEUD(Quests.THE_FEUD.questName, SkillReq(Skills.THIEVING, 30)),
    GOLEM(Quests.THE_GOLEM.questName, SkillReq(Skills.CRAFTING, 20, true), SkillReq(Skills.THIEVING, 25, true)),
    DESERT(Quests.DESERT_TREASURE.questName, QuestReq(DIG_SITE), QuestReq(IKOV), QuestReq(TOURIST), QuestReq(TROLL_STRONGHOLD), QuestReq(PRIEST), QuestReq(WATERFALL), SkillReq(Skills.THIEVING, 53), SkillReq(Skills.MAGIC, 50), SkillReq(Skills.FIREMAKING, 50, true), SkillReq(Skills.SLAYER, 10)),
    ICTHLARIN(Quests.ICTHLARINS_LITTLE_HELPER.questName, QuestReq(GERTRUDE)),
    TEARS_OF_GUTHIX(Quests.TEARS_OF_GUTHIX.questName, QPReq(43), SkillReq(Skills.FIREMAKING, 49, true), SkillReq(Skills.CRAFTING, 20, true), SkillReq(Skills.MINING, 20, true)),
    LOST_TRIBE(Quests.THE_LOST_TRIBE.questName, QuestReq(GOBLIN_DIP), QuestReq(RUNE_MYSTERIES), SkillReq(Skills.AGILITY, 13, true), SkillReq(Skills.THIEVING, 13, true), SkillReq(Skills.MINING, 17, true)),
    GIANT_DWARF(Quests.THE_GIANT_DWARF.questName, SkillReq(Skills.CRAFTING, 12, true), SkillReq(Skills.FIREMAKING, 16, true), SkillReq(Skills.MAGIC, 33, true), SkillReq(Skills.THIEVING, 14, true)),
    RECRUITMENT_DRIVE(Quests.RECRUITMENT_DRIVE.questName, QuestReq(BLACK_KNIGHT), QuestReq(DRUIDIC_RITUAL)),
    MEP_1(Quests.MOURNINGS_END_PART_I.questName, SkillReq(Skills.RANGE, 60), SkillReq(Skills.THIEVING, 50), QuestReq(ROVING_ELVES), QuestReq(CHOMPY), QuestReq(SHEEP_HERDER)),
    FORGETTABLE(Quests.FORGETTABLE_TALE.questName, SkillReq(Skills.COOKING, 22, true), SkillReq(Skills.FARMING, 17, true), QuestReq(GIANT_DWARF), QuestReq(FISHING_CONTEST)),
    GARDEN(Quests.GARDEN_OF_TRANQUILLITY.questName, QuestReq(FENKENSTRAIN), SkillReq(Skills.FARMING, 25)),
    TWO_CATS(Quests.A_TAIL_OF_TWO_CATS.questName, QuestReq(ICTHLARIN)),
    WANTED(Quests.WANTED.questName, QPReq(32), QuestReq(RECRUITMENT_DRIVE), QuestReq(LOST_TRIBE), QuestReq(PRIEST)),
    MEP_2(Quests.MOURNINGS_END_PART_II.questName, QuestReq(MEP_1)),
    ZOGRE(Quests.ZOGRE_FLESH_EATERS.questName, QuestReq(CHOMPY), QuestReq(POTION), SkillReq(Skills.SMITHING, 4, true), SkillReq(Skills.HERBLORE, 8, true), SkillReq(Skills.RANGE, 30)),
    RUM_DEAL(Quests.RUM_DEAL.questName, QuestReq(ZOGRE), QuestReq(PRIEST), SkillReq(Skills.CRAFTING, 42, true), SkillReq(Skills.FISHING, 50, true), SkillReq(Skills.FARMING, 40, true), SkillReq(Skills.PRAYER, 47, true), SkillReq(Skills.SLAYER, 42)),
    SHADOW(Quests.SHADOW_OF_THE_STORM.questName, SkillReq(Skills.CRAFTING, 30, true), QuestReq(GOLEM), QuestReq(DEMON_SLAYER)),
    HISTORY(Quests.MAKING_HISTORY.questName, QuestReq(PRIEST), QuestReq(RESTLESS_GHOST)),
    MEETING_HISTORY(Quests.MEETING_HISTORY.questName, QuestReq(HISTORY), QuestReq(RESTLESS_GHOST), QuestReq(DRUIDIC_RITUAL), QuestReq(PRIEST)),
    RATCATCHERS(Quests.RATCATCHERS.questName, QuestReq(ICTHLARIN), QuestReq(GIANT_DWARF)),
    SPIRITS_ELID(Quests.SPIRITS_OF_THE_ELID.questName, SkillReq(Skills.MAGIC, 33, true), SkillReq(Skills.RANGE, 37, true), SkillReq(Skills.MINING, 37, true), SkillReq(Skills.THIEVING, 37, true)),
    DEVIOUS(Quests.DEVIOUS_MINDS.questName, SkillReq(Skills.SMITHING, 65, true), SkillReq(Skills.RUNECRAFTING, 50, true), SkillReq(Skills.FLETCHING, 50, true), QuestReq(WANTED), QuestReq(TROLL_STRONGHOLD), QuestReq(DORIC)),
    SAND(Quests.THE_HAND_IN_THE_SAND.questName, SkillReq(Skills.THIEVING, 17, true), SkillReq(Skills.CRAFTING, 49, true)),
    ENAKHRA(Quests.ENAKHRAS_LAMENT.questName, SkillReq(Skills.CRAFTING, 50, true), SkillReq(Skills.FIREMAKING, 45, true), SkillReq(Skills.PRAYER, 43, true), SkillReq(Skills.MAGIC, 39, true)),
    CABIN_FEVER(Quests.CABIN_FEVER.questName, QuestReq(PIRATE_T), QuestReq(RUM_DEAL), SkillReq(Skills.AGILITY, 42), SkillReq(Skills.CRAFTING, 45), SkillReq(Skills.SMITHING, 50), SkillReq(Skills.RANGE, 40)),
    FAIRYTALE_1(Quests.FAIRYTALE_I_GROWING_PAINS.questName, QuestReq(LOST_CITY), QuestReq(NATURE_SPIRIT)),
    RFD(Quests.RECIPE_FOR_DISASTER.questName, QPReq(175), QuestReq(COOK_ASSIST), SkillReq(Skills.COOKING, 70, true), SkillReq(Skills.AGILITY, 48, true), SkillReq(Skills.MINING, 50, true), SkillReq(Skills.FISHING, 53, true), SkillReq(Skills.THIEVING, 53, true), SkillReq(Skills.HERBLORE, 25, true), SkillReq(Skills.MAGIC, 59, true), SkillReq(Skills.SMITHING, 40, true), SkillReq(Skills.FIREMAKING, 50, true), SkillReq(Skills.RANGE, 40), SkillReq(Skills.CRAFTING, 40, true), SkillReq(Skills.FLETCHING, 10, true), SkillReq(Skills.WOODCUTTING, 36, true), QuestReq(FISHING_CONTEST), QuestReq(GOBLIN_DIP), QuestReq(CHOMPY), QuestReq(MURDER_MYS), QuestReq(NATURE_SPIRIT), QuestReq(WITCH_HOUSE), QuestReq(GERTRUDE), QuestReq(SHADOW), QuestReq(LEGEND), QuestReq(MONKEY), QuestReq(DESERT), QuestReq(HORROR_DEEP)),
    AID_MYREQUE(Quests.IN_AID_OF_THE_MYREQUE.questName, QuestReq(SEARCH_MYREQUE), SkillReq(Skills.AGILITY, 25, true), SkillReq(Skills.CRAFTING, 25), SkillReq(Skills.MINING, 15), SkillReq(Skills.MAGIC, 7, true)),
    SOUL_BANE(Quests.A_SOULS_BANE.questName), BONE_MAN_1(Quests.RAG_AND_BONE_MAN.questName), SWAN(Quests.SWAN_SONG.questName, QPReq(100), SkillReq(Skills.MAGIC, 66, true), SkillReq(Skills.COOKING, 62, true), SkillReq(Skills.FISHING, 62, true), SkillReq(Skills.SMITHING, 45, true), SkillReq(Skills.FIREMAKING, 42, true), SkillReq(Skills.CRAFTING, 40, true), QuestReq(FAVOR), QuestReq(GARDEN)),
    ROYAL_TROUBLE(Quests.ROYAL_TROUBLE.questName, SkillReq(Skills.AGILITY, 40, true), SkillReq(Skills.SLAYER, 40, true), QuestReq(THRONE)),
    DEATH_DORGESHUUN(Quests.DEATH_TO_THE_DORGESHUUN.questName, QuestReq(LOST_TRIBE), SkillReq(Skills.AGILITY, 23, true), SkillReq(Skills.THIEVING, 23, true)),
    FAIRYTALE_2(Quests.FAIRYTALE_II_CURE_A_QUEEN.questName, QuestReq(FAIRYTALE_1), SkillReq(Skills.THIEVING, 40), SkillReq(Skills.FARMING, 49, true), SkillReq(Skills.HERBLORE, 57, true)),
    LUNAR_DIPLOMACY(Quests.LUNAR_DIPLOMACY.questName, QuestReq(FREM_TRIALS), QuestReq(LOST_CITY), QuestReq(RUNE_MYSTERIES), QuestReq(SHILO), SkillReq(Skills.HERBLORE, 5), SkillReq(Skills.CRAFTING, 61), SkillReq(Skills.DEFENCE, 40), SkillReq(Skills.FIREMAKING, 49), SkillReq(Skills.MAGIC, 65), SkillReq(Skills.MINING, 60), SkillReq(Skills.WOODCUTTING, 55)),
    GLOUPHRIE(Quests.THE_EYES_OF_GLOUPHRIE.questName, QuestReq(GRAND_TREE), SkillReq(Skills.CONSTRUCTION, 5), SkillReq(Skills.MAGIC, 46)),
    HALLOWVALE(Quests.DARKNESS_OF_HALLOWVALE.questName, QuestReq(AID_MYREQUE), SkillReq(Skills.CONSTRUCTION, 5, true), SkillReq(Skills.MINING, 20), SkillReq(Skills.THIEVING, 22), SkillReq(Skills.AGILITY, 26, true), SkillReq(Skills.CRAFTING, 32), SkillReq(Skills.MAGIC, 33, true), SkillReq(Skills.STRENGTH, 40)),
    SLUG_MENACE(Quests.THE_SLUG_MENACE.questName, QuestReq(WANTED), QuestReq(SEA_SLUG), SkillReq(Skills.CRAFTING, 30), SkillReq(Skills.RUNECRAFTING, 30), SkillReq(Skills.SLAYER, 30), SkillReq(Skills.THIEVING, 30)),
    ELEMENTAL_W2(Quests.ELEMENTAL_WORKSHOP_II.questName, QuestReq(ELEMENTAL_W1), SkillReq(Skills.MAGIC, 20, true), SkillReq(Skills.SMITHING, 30, true)),
    ARM_ADVENTURE(Quests.MY_ARMS_BIG_ADVENTURE.questName, SkillReq(Skills.FARMING, 29, true), SkillReq(Skills.WOODCUTTING, 10), QuestReq(EADGAR), QuestReq(FEUD), QuestReq(POTION)),
    ENL_JOURNEY(Quests.ENLIGHTENED_JOURNEY.questName, QPReq(20), SkillReq(Skills.FIREMAKING, 20, true), SkillReq(Skills.FARMING, 30, true), SkillReq(Skills.CRAFTING, 36, true)),
    EAGLE(Quests.EAGLES_PEAK.questName, SkillReq(Skills.HUNTER, 27, true)),
    ANMA(Quests.ANIMAL_MAGNETISM.questName, QuestReq(RESTLESS_GHOST), QuestReq(ERNEST), QuestReq(PRIEST), SkillReq(Skills.SLAYER, 18), SkillReq(Skills.CRAFTING, 19), SkillReq(Skills.RANGE, 30), SkillReq(Skills.WOODCUTTING, 35)),
    CONTACT(Quests.CONTACT.questName, QuestReq(ALI_RESCUE), QuestReq(ICTHLARIN)),
    COLD_WAR(Quests.COLD_WAR.questName, SkillReq(Skills.HUNTER, 10), SkillReq(Skills.AGILITY, 30, true), SkillReq(Skills.CRAFTING, 30), SkillReq(Skills.CONSTRUCTION, 34), SkillReq(Skills.THIEVING, 15)),
    FREM_ISLES(Quests.THE_FREMENNIK_ISLES.questName, QuestReq(FREM_TRIALS), SkillReq(Skills.CONSTRUCTION, 20, true)),
    BRAIN_ROBBERY(Quests.THE_GREAT_BRAIN_ROBBERY.questName, SkillReq(Skills.CRAFTING, 16), SkillReq(Skills.CONSTRUCTION, 30), SkillReq(Skills.PRAYER, 50), QuestReq(FENKENSTRAIN), QuestReq(CABIN_FEVER), QuestReq(RFD)),
    WHAT_LIES_BELOW(Quests.WHAT_LIES_BELOW.questName, QuestReq(RUNE_MYSTERIES), SkillReq(Skills.RUNECRAFTING, 35)),
    OLAF(Quests.OLAFS_QUEST.questName, QuestReq(FREM_TRIALS), SkillReq(Skills.FIREMAKING, 40, true), SkillReq(Skills.WOODCUTTING, 50, true)),
    ANOTHER_SLICE(Quests.ANOTHER_SLICE_OF_HAM.questName, SkillReq(Skills.ATTACK, 15), SkillReq(Skills.PRAYER, 25), QuestReq(DEATH_DORGESHUUN), QuestReq(GIANT_DWARF), QuestReq(DIG_SITE)),
    DREAM_MENTOR(Quests.DREAM_MENTOR.questName, QuestReq(LUNAR_DIPLOMACY), QuestReq(EADGAR)),
    GRIM_TALES(Quests.GRIM_TALES.questName, QuestReq(WITCH_HOUSE), SkillReq(Skills.FARMING, 45, true), SkillReq(Skills.HERBLORE, 52, true), SkillReq(Skills.THIEVING, 58, true), SkillReq(Skills.AGILITY, 59, true), SkillReq(Skills.WOODCUTTING, 71, true)),
    KINGS_RANSOM(Quests.KINGS_RANSOM.questName, SkillReq(Skills.MAGIC, 45), SkillReq(Skills.MINING, 45, true), SkillReq(Skills.DEFENCE, 65), QuestReq(BLACK_KNIGHT), QuestReq(GRAIL), QuestReq(MURDER_MYS), QuestReq(FAVOR)),
    TOWER_OF_LIFE(Quests.TOWER_OF_LIFE.questName, SkillReq(Skills.CONSTRUCTION, 10)),
    BONE_MAN_2(Quests.RAG_AND_BONE_MAN.questName, SkillReq(Skills.SLAYER, 40, true), SkillReq(Skills.DEFENCE, 20), QuestReq(BONE_MAN_1), QuestReq(FREM_TRIALS), QuestReq(FENKENSTRAIN), QuestReq(ZOGRE), QuestReq(WATERFALL)),
    BACK_TO_MY_ROOTS(Quests.BACK_TO_MY_ROOTS.questName, QuestReq(FAVOR), QuestReq(TOTEM), QuestReq(SAND), QuestReq(FAIRYTALE_1), SkillReq(Skills.WOODCUTTING, 72, true), SkillReq(Skills.FIREMAKING, 53), SkillReq(Skills.SLAYER, 59), SkillReq(Skills.AGILITY, 55)),
    LAND_GOBLINS(Quests.LAND_OF_THE_GOBLINS.questName, QuestReq(ANOTHER_SLICE), QuestReq(FISHING_CONTEST), SkillReq(Skills.AGILITY, 38), SkillReq(Skills.FISHING, 40), SkillReq(Skills.THIEVING, 45), SkillReq(Skills.HERBLORE, 48)),
    PATH_GLOUPHRIE(Quests.THE_PATH_OF_GLOUPHRIE.questName, QuestReq(GLOUPHRIE), QuestReq(GNOME_VILLAGE), QuestReq(WATERFALL), SkillReq(Skills.AGILITY, 45), SkillReq(Skills.RANGE, 47), SkillReq(Skills.SLAYER, 56), SkillReq(Skills.STRENGTH, 60), SkillReq(Skills.THIEVING, 56)),
    DEFENDER_VARROCK(Quests.DEFENDER_OF_VARROCK.questName, QuestReq(ARRAV), QuestReq(KNIGHT_SWORD), QuestReq(DEMON_SLAYER), QuestReq(IKOV), QuestReq(FAMILY_CREST), QuestReq(WHAT_LIES_BELOW), QuestReq(GARDEN), SkillReq(Skills.AGILITY, 51), SkillReq(Skills.HUNTER, 51), SkillReq(Skills.MINING, 59), SkillReq(Skills.SMITHING, 54)),
    SPIRIT_OF_SUMMER(Quests.SPIRIT_OF_SUMMER.questName, QuestReq(RESTLESS_GHOST), SkillReq(Skills.CONSTRUCTION, 40), SkillReq(Skills.FARMING, 26), SkillReq(Skills.PRAYER, 35), SkillReq(Skills.SUMMONING, 19)),
    RESORT(Quests.AS_A_FIRST_RESORT.questName, QuestReq(ZOGRE), SkillReq(Skills.HUNTER, 48, true), SkillReq(Skills.FIREMAKING, 51), SkillReq(Skills.WOODCUTTING, 58)),
    SUMMERS_END(Quests.SUMMERS_END.questName, QuestReq(SPIRIT_OF_SUMMER), SkillReq(Skills.FIREMAKING, 47), SkillReq(Skills.HUNTER, 35), SkillReq(Skills.MINING, 45), SkillReq(Skills.PRAYER, 55), SkillReq(Skills.SUMMONING, 23), SkillReq(Skills.WOODCUTTING, 37)),
    SEERGAZE(Quests.LEGACY_OF_SEERGAZE.questName, QuestReq(HALLOWVALE), SkillReq(Skills.AGILITY, 29), SkillReq(Skills.CONSTRUCTION, 20), SkillReq(Skills.CRAFTING, 47), SkillReq(Skills.FIREMAKING, 40), SkillReq(Skills.MAGIC, 49), SkillReq(Skills.MINING, 35), SkillReq(Skills.SLAYER, 31)),
    SMOKING_KILLS(Quests.SMOKING_KILLS.questName, QuestReq(RESTLESS_GHOST), QuestReq(ICTHLARIN), SkillReq(Skills.CRAFTING, 25), SkillReq(Skills.SLAYER, 35)),
    WHILE_GUTHIX_SLEEPS(Quests.WHILE_GUTHIX_SLEEPS.questName, SkillReq(Skills.SUMMONING, 23), SkillReq(Skills.HUNTER, 55), SkillReq(Skills.THIEVING, 60), SkillReq(Skills.DEFENCE, 65), SkillReq(Skills.FARMING, 65), SkillReq(Skills.HERBLORE, 65), SkillReq(Skills.MAGIC, 75), QuestReq(DEFENDER_VARROCK), QuestReq(DREAM_MENTOR), QuestReq(SAND), QuestReq(KINGS_RANSOM), QuestReq(LEGEND), QuestReq(MEP_2), QuestReq(PATH_GLOUPHRIE), QuestReq(RFD), QuestReq(SUMMERS_END), QuestReq(SWAN), QuestReq(TEARS_OF_GUTHIX), QuestReq(ZOGRE)),
    ROCKING_OUT(Quests.ROCKING_OUT.questName, SkillReq(Skills.AGILITY, 60), SkillReq(Skills.CRAFTING, 66), SkillReq(Skills.SMITHING, 69), SkillReq(Skills.THIEVING, 63), QuestReq(BRAIN_ROBBERY)),
}
