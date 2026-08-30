package content.minigame.pest_control.reward

import content.global.skill.herblore.herb.HerbItem
import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.tools.RandomFunction
import shared.consts.Components
import shared.consts.Items

/**
 * Represents the pest control reward interface.
 * @author Vexia
 */
class PCRewardInterface : InterfaceListener {

    companion object {

        /**
         * Represents the red colour.
         */
        const val RED = "<col=FF0000>"

        /**
         * Represents the green colour.
         */
        const val GREEN = "<col=04B404>"

        /**
         * Represents the white colour.
         */
        const val WHITE = "<col=FFFFFF>"

        /**
         * Represents the skill headers ordered by skill index.
         */
        private val SKILL_HEADER = intArrayOf(10, 12, 11, 15, 13, 16, 14)

        /**
         * Represents the skill array of exp rewards.
         */
        private val SKILL_ARRAY = intArrayOf(
            Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGE,
            Skills.MAGIC, Skills.HITPOINTS, Skills.PRAYER
        )

        /**
         * Represents the skill options array.
         */
        val SKILL_POINTS = intArrayOf(1, 10, 100)

        /**
         * Represents the charm points.
         */
        val CHARM_POINTS = intArrayOf(2, 28, 56)

        /**
         * Represents the amount of charms to get from the points.
         */
        private val CHARM_AMOUNTS = intArrayOf(1, 14, 28)

        /**
         * Method used to open the pest control reward interface.
         */
        @JvmStatic
        fun open(player: Player) {
            removeAttribute(player,"pc-reward")
            sendString(player, "Points: " + player.savedData.activityData.pestControlPoints, 105)
            clear(player)
            openInterface(player, Components.PEST_REWARDS_267)
        }

        /**
         * Method used to send the skill headers.
         */
        private fun sendSkills(player: Player) {
            for (skill in SKILL_ARRAY) {
                sendString(player, getSkillCondition(player, skill), getSkillChild(skill))
            }
        }

        /**
         * Method used to send a string onto this interface.
         */
        private fun sendString(player: Player, string: String, child: Int) {
            sendString(player, string, 267, child)
        }

        /**
         * Method used to deselect the current reward.
         */
        private fun deselect(player: Player): Boolean {
            return deselect(player, getReward(player))
        }

        /**
         * Method used to deselect a given reward.
         */
        private fun deselect(player: Player, reward: Reward?): Boolean {
            if (reward == null) {
                return false
            }
            clear(player)
            reward.deselect(player, getCachedOption(player))
            return true
        }

        /**
         * Method used to cache the reward.
         */
        private fun cacheReward(player: Player, reward: Reward, option: Int) {
            deselect(player) // deselect any previous ones.
            reward.select(player, option)
            sendString(player, "<col=F7DF22>Confirm:", 106)
            sendString(player, reward.getName(), 104)
            setAttribute(player,"pc-reward", reward)
            setAttribute(player,"pc-reward:option", option)
        }

        /**
         * Method used to clear the interface with new data.
         */
        fun clear(player: Player) {
            sendSkills(player)
            for (reward in Reward.values()) {
                if (reward.isSkillReward()) {
                    continue
                }
                val points = player.savedData.activityData.pestControlPoints
                if (reward.charm) {
                    sendString(
                        player,
                        if (points < 2) RED + "You need 2 points." else GREEN + reward.getName(),
                        reward.header
                    )
                    continue
                }
                sendString(
                    player,
                    if (points < reward.points) {
                        if (points < 1) RED + "You need at least 1 point." else RED + "You need " + reward.points + " points."
                    } else {
                        GREEN + reward.getName()
                    },
                    reward.header
                )
            }
        }

        /**
         * Method used to calculate the experience the player can receive in this skill.
         */
        private fun calculateExperience(player: Player, skillId: Int, points: Int): Int {
            val level = getStatLevel(player, skillId)
            var n = 0
            when (skillId) {
                Skills.PRAYER -> n = 18
                Skills.MAGIC, Skills.RANGE -> n = 32
                Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.HITPOINTS -> n = 35
            }
            val xpPerPoint = ((level * level).toDouble() / 600).toInt() * n
            val bonus = when {
                points >= 100 -> 1.1
                points >= 10 -> 1.01
                else -> 1.0
            }
            return (points * xpPerPoint * bonus).toInt()
        }

        /**
         * Method used to get the skill condition string to send.
         */
        private fun getSkillCondition(player: Player, skillId: Int): String {
            if (getStatLevel(player,skillId) < 25) {
                return RED + "Must reach level 25 first."
            }
            return GREEN + getSkillXp(player, skillId)
        }

        /**
         * Method used to get the skill experience string.
         */
        fun getSkillXp(player: Player, skillId: Int): String {
            return Skills.SKILL_NAME[skillId] + " - " + calculateExperience(player, skillId, 1) + " xp"
        }

        /**
         * Method used to get the skill header by the index.
         */
        private fun getSkillChild(skill: Int): Int {
            return SKILL_HEADER[skill]
        }

        /**
         * Method used to get the current reward.
         */
        private fun getReward(player: Player): Reward? {
            return player.getAttribute<Reward>("pc-reward", null)
        }

        /**
         * Method used to check if the player has a reward set.
         */
        private fun hasReward(player: Player): Boolean {
            return getReward(player) != null
        }

        /**
         * Method used to get the pest control reward option index.
         */
        private fun getCachedOption(player: Player): Int {
            return player.getAttribute("pc-reward:option", 0)
        }

        /**
         * Method used to select a reward.
         */
        fun select(player: Player, button: Int) {
            val reward = Reward.forButton(button) ?: return
            val option = reward.getOption(button)
            if (!reward.checkRequirements(player, option)) {
                return
            }
            cacheReward(player, reward, option)
        }

        /**
         * Method used to confirm the reward.
         */
        fun confirm(player: Player) {
            if (!hasReward(player)) {
                sendMessage(player, "Please choose a reward.")
                return
            }
            val reward = getReward(player)!!
            if ((reward.charm || !reward.isSkillReward()) && freeSlots(player) == 0) {
                sendMessage(player, "You don't have enough inventory space.")
                return
            }
            val option = getCachedOption(player)
            val points = reward.getPoints(option)
            val message: String

            closeInterface(player)
            if (player.savedData.activityData.pestControlPoints >= points) {
                player.savedData.activityData.decreasePestControlPoints(points)
                if (reward.isSkillReward()) {
                    val experience = calculateExperience(player, reward.skill, points)
                    rewardXP(player, reward.skill, experience.toDouble())
                    message = "The Void Knight has granted you $experience " + reward.getName() + "."
                } else {
                    if (!reward.checkItemRequirement(player, option)) {
                        return
                    }
                    if (!reward.charm) {
                        val rewardItems = reward.reward!!
                        if (rewardItems.size > 1) {
                            val pack = reward.constructPack()
                            for (i in pack) {
                                if (!player.inventory.add(i)) {
                                    GroundItemManager.create(i, player)
                                }
                            }
                        } else {
                            if (!player.inventory.add(rewardItems[0])) {
                                GroundItemManager.create(rewardItems[0], player)
                            }
                        }
                    } else {
                        val charm = Item(reward.reward!![0].id)
                        val amt = CHARM_AMOUNTS[option - 1]
                        for (i in 0 until amt) {
                            if (!player.inventory.add(charm)) {
                                GroundItemManager.create(charm, player)
                            }
                        }
                    }
                    message = "The Void Knight has given you a " + reward.getName() + "."
                }
                sendDialogueLines(player,
                    message,
                    "<col=571D07>Remaining Void Knight Commendation Points: " + player.savedData.activityData.pestControlPoints
                )
            }
        }
    }

    override fun defineInterfaceListeners() {

        on(Components.PEST_REWARDS_267) { player, _, _, button, _, _ ->
            when (button) {
                96 -> confirm(player)
                else -> {
                    if (button in 34..86) {
                        if (player.savedData.activityData.pestControlPoints == 0) {
                            sendMessage(player, "You don't have enough points.")
                            return@on true
                        }
                        select(player, button)
                    }
                }
            }
            return@on true
        }
    }

    /**
     * Represents the rewards that are obtainable with this interface.
     * @author Vexia
     */
    enum class Reward(
        val skill: Int = -1,
        private val nameOverride: String? = null,
        val points: Int = 0,
        val reward: Array<Item>? = null,
        val childs: IntArray = IntArray(0),
        val charm: Boolean = false
    ) {
        ATTACK(skill = Skills.ATTACK, childs = intArrayOf(10, 34, 49, 56)),
        STRENGTH(skill = Skills.STRENGTH, childs = intArrayOf(11, 35, 50, 57)),
        DEFENCE(skill = Skills.DEFENCE, childs = intArrayOf(12, 36, 51, 58)),
        RANGE(skill = Skills.RANGE, childs = intArrayOf(13, 37, 52, 59)),
        MAGIC(skill = Skills.MAGIC, childs = intArrayOf(14, 38, 53, 60)),
        HITPOINTS(skill = Skills.HITPOINTS, childs = intArrayOf(15, 39, 54, 61)),
        PRAYER(skill = Skills.PRAYER, childs = intArrayOf(16, 40, 55, 62)),
        HERB_PACK(
            nameOverride = "Herb Pack",
            points = 30,
            reward = arrayOf(
                HerbItem.HARRALANDER.herb,
                HerbItem.RANARR.herb,
                HerbItem.TOADFLAX.herb,
                HerbItem.IRIT.herb,
                HerbItem.AVANTOE.herb,
                HerbItem.KWUARM.herb,
                HerbItem.GUAM.herb,
                HerbItem.MARRENTILL.herb
            ),
            childs = intArrayOf(32, 45)
        ) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean {
                if (player.skills.getLevel(Skills.HERBLORE) < 25) {
                    player.packetDispatch.sendMessage("You need level 25 herblore to purchase this pack.")
                    return false
                }
                return true
            }
        },
        MINERAL_PACK(
            nameOverride = "Mineral Pack",
            points = 15,
            reward = arrayOf(Item(453), Item(440)),
            childs = intArrayOf(47, 46)
        ) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean {
                if (getStatLevel(player, Skills.MINING) < 25) {
                    sendMessage(player, "You need level 25 mining to purchase this pack.")
                    return false
                }
                return true
            }
        },
        SEED_PACK(
            nameOverride = "Seed Pack",
            points = 15,
            reward = arrayOf(Item(5320), Item(5322), Item(5100)),
            childs = intArrayOf(33, 48)
        ) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean {
                if (getStatLevel(player, Skills.FARMING) < 25) {
                    sendMessage(player, "You need level 25 farming to purchase this pack.")
                    return false
                }
                return true
            }
        },
        VOID_MACE(nameOverride = "Void Knight Mace", points = 250, reward = arrayOf(Item(Items.VOID_KNIGHT_MACE_8841)), childs = intArrayOf(28, 41)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_TOP(nameOverride = "Void Knight Top", points = 250, reward = arrayOf(Item(Items.VOID_KNIGHT_TOP_8839)), childs = intArrayOf(29, 42)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_ROBES(nameOverride = "Void Knight Robes", points = 250, reward = arrayOf(Item(Items.VOID_KNIGHT_ROBE_8840)), childs = intArrayOf(30, 43)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_GLOVES(nameOverride = "Void Knight Gloves", points = 150, reward = arrayOf(Item(Items.VOID_KNIGHT_GLOVES_8842)), childs = intArrayOf(31, 44)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_MAGE_HELM(nameOverride = "Void Knight Mage Helm", points = 200, reward = arrayOf(Item(Items.VOID_MAGE_HELM_11663)), childs = intArrayOf(63, 67)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_RANGER_HELM(nameOverride = "Void Knight Ranger Helm", points = 200, reward = arrayOf(Item(Items.VOID_RANGER_HELM_11664)), childs = intArrayOf(64, 68)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_MELEE_HELM(nameOverride = "Void Knight Melee Helm", points = 200, reward = arrayOf(Item(Items.VOID_MELEE_HELM_11665)), childs = intArrayOf(65, 69)) {
            override fun checkItemRequirement(player: Player, option: Int): Boolean = hasVoidSkills(player)
        },
        VOID_KNIGHT_SEAL(nameOverride = "Void Knight Seal", points = 10, reward = arrayOf(Item(Items.VOID_SEAL8_11666)), childs = intArrayOf(66, 70)),
        SPINNER_CHARM(nameOverride = "Spinner Charm", reward = arrayOf(Item(Items.SPINNER_CHARM_12166)), childs = intArrayOf(71, 75, 76, 77), charm = true),
        RAVAGER_CHARM(nameOverride = "Ravager Charm", reward = arrayOf(Item(Items.RAVAGER_CHARM_12164)), childs = intArrayOf(72, 81, 82, 83), charm = true),
        TORCHER_CHARM(nameOverride = "Torcher Charm", reward = arrayOf(Item(Items.TORCHER_CHARM_12167)), childs = intArrayOf(74, 78, 79, 80), charm = true),
        SHIFTER_CHAR(nameOverride = "Shifter Charm", reward = arrayOf(Item(Items.SHIFTER_CHARM_12165)), childs = intArrayOf(73, 84, 85, 86), charm = true);

        companion object {
            /**
             * Represents the void required skills.
             */
            private val VOID_SKILLS = intArrayOf(
                Skills.HITPOINTS, Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH,
                Skills.RANGE, Skills.MAGIC, Skills.PRAYER
            )

            /**
             * Represents the maximum build of an item array pack.
             */
            private const val MAX_BUILD = 18

            /**
             * Represents the minimum build of an item array pack.
             */
            private const val MIN_BUILD = 13

            /**
             * Method used to get the reward type based off the button.
             */
            fun forButton(button: Int): Reward? {
                for (reward in values()) {
                    for (i in reward.childs) {
                        if (i == button) {
                            return reward
                        }
                    }
                }
                return null
            }
        }

        /**
         * Represents the header (first child) of this reward.
         */
        val header: Int get() = childs[0]

        /**
         * Method used to check the requirements of a reward.
         */
        fun checkRequirements(player: Player, option: Int): Boolean {
            if (player.savedData.activityData.pestControlPoints < getPoints(option)) {
                sendMessage(player, "You don't have enough points.")
                return false
            }
            return if (isSkillReward()) checkSkillRequirement(player, option) else checkItemRequirement(player, option)
        }

        /**
         * Method used to select a reward.
         */
        fun select(player: Player, option: Int) {
            if (isSkillReward()) {
                skillSelect(player, option)
            } else {
                itemSelect(player, option)
            }
        }

        /**
         * Method used to deselect a reward.
         */
        fun deselect(player: Player, option: Int) {
            if (isSkillReward()) {
                skillDeselect(player, option)
            } else {
                itemDeselect(player, option)
            }
        }

        /**
         * Method used to select a skill.
         */
        private fun skillSelect(player: Player, option: Int) {
            sendString(player, WHITE + getSkillXp(player, skill), header)
            sendString(player, WHITE + getOptionString(option), childs[option])
        }

        /**
         * Method used to handle the item select.
         */
        private fun itemSelect(player: Player, option: Int) {
            sendString(player, WHITE + getName(), header)
            if (charm) {
                sendString(player, WHITE + getOptionString(option), childs[option])
            }
        }

        /**
         * Method used to deselect a skill.
         */
        private fun skillDeselect(player: Player, option: Int) {
            sendString(player, "<col=784F1C>" + getOptionString(option), childs[option])
        }

        /**
         * Method used to handle the item deselect.
         */
        private fun itemDeselect(player: Player, option: Int) {
            if (charm) {
                sendString(player, "<col=784F1C>" + getOptionString(option), childs[option])
            }
        }

        /**
         * Method used to get the option string.
         */
        private fun getOptionString(option: Int): String {
            return if (charm) {
                when (option) {
                    1 -> "(2 Pts)"
                    2 -> "(28 Pts)"
                    else -> "(56 Pts)"
                }
            } else {
                when (option) {
                    1 -> "(1 Pt)"
                    2 -> "(10 Pts)"
                    else -> "(100 Pts)"
                }
            }
        }

        /**
         * Method used to get the option chosen.
         */
        fun getOption(button: Int): Int {
            for ((index, i) in childs.withIndex()) {
                if (i == button) {
                    return index
                }
            }
            return -1
        }

        /**
         * Gets the amount of required points.
         */
        fun getPoints(option: Int): Int {
            if (charm) {
                return CHARM_POINTS[option - 1]
            }
            return if (isSkillReward()) SKILL_POINTS[option - 1] else points
        }

        /**
         * Method used to check if the player has the skills to buy void.
         */
        fun hasVoidSkills(player: Player): Boolean {
            for (s in VOID_SKILLS) {
                if (player.skills.getLevel(s) < (if (s != Skills.PRAYER) 42 else 22)) {
                    sendMessage(player,"You need level 42 in hitpoints, attack, defence, strength, ranged, magic, and")
                    sendMessage(player,
                        "22 prayer to purchase the " +
                                (nameOverride ?: "").lowercase().replace("_", " ").replace("void knight", "").trim() + "."
                    )
                    return false
                }
            }
            return true
        }

        /**
         * Gets the name of this reward.
         */
        fun getName(): String {
            return if (isSkillReward()) Skills.SKILL_NAME[skill] + " xp" else (nameOverride ?: "")
        }

        /**
         * Method used to check a skill requirement.
         */
        private fun checkSkillRequirement(player: Player, option: Int): Boolean {
            if (getStatLevel(player, skill) < 25) {
                sendMessage(player,"The Void Knights will not offer training in skills which you have a level of")
                sendMessage(player,"less than 25.")
                return false
            }
            return true
        }

        /**
         * Method used to check the item requirement reward.
         */
        open fun checkItemRequirement(player: Player, option: Int): Boolean {
            return true
        }

        /**
         * Method used to generate an item pack.
         */
        fun constructPack(): Array<Item> {
            val build = if (this == SEED_PACK || this == HERB_PACK) {
                RandomFunction.random(MIN_BUILD, MAX_BUILD)
            } else {
                RandomFunction.random(38, 43)
            }
            var left = build
            val pack = ArrayList<Item>(20)
            for (i in reward ?: emptyArray()) {
                var amt = if (this == SEED_PACK || this == HERB_PACK) {
                    RandomFunction.random(1, 5)
                } else {
                    RandomFunction.random(16, 25)
                }
                if (amt > left) {
                    amt = left
                }
                if (amt < 1) {
                    continue
                }
                val id = if (this != SEED_PACK) itemDefinition(i.id).noteId else i.id
                pack.add(Item(id, amt))
                left -= amt
            }
            return pack.toTypedArray()
        }

        /**
         * Checks if this reward is a skill reward.
         */
        fun isSkillReward(): Boolean = childs.size > 2 && !charm
    }
}