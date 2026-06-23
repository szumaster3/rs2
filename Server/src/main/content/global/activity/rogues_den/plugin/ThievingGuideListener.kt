package content.global.activity.rogues_den.plugin

import core.Util.random
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.ChanceItem
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs

class ThievingGuideListener : InteractionListener {

    companion object {

        private val COINS = arrayOf(
            ChanceItem(Items.COINS_995, 20, 20, 90.0),
            ChanceItem(Items.COINS_995, 40, 40, 80.0)
        )

        private val GEMS = arrayOf(
            ChanceItem(Items.UNCUT_SAPPHIRE_1623,1, 1, 80.0),
            ChanceItem(Items.UNCUT_EMERALD_1621, 1, 1, 60.0),
            ChanceItem(Items.UNCUT_RUBY_1619,    1, 1, 8.0),
            ChanceItem(Items.UNCUT_DIAMOND_1617, 1, 1, 7.0)
        )

        private val STETHOSCOPE = Item(Items.STETHOSCOPE_5560)

        private const val REQUIRED_LEVEL = 50
        private const val EXPERIENCE = 70.0
        private const val CRACKED_SAFE = 7238

        private val ANIMATIONS = arrayOf(
            Animation(Animations.SAFE_CRACK_2247),
            Animation(Animations.SAFE_CRACK_2248),
            Animation(Animations.ANIMATION_1113),
            Animation(Animations.DISARM_TRAP_2244)
        )
    }

    override fun defineListeners() {

        on(shared.consts.Scenery.DOORWAY_7256, IntType.SCENERY, "open") { player, _ ->

            sendNPCDialogueLines(
                player,
                NPCs.BRIAN_ORICHARD_2266,
                FaceAnim.THINKING,
                false,
                "And where do you think you're going? A little too eager I think.",
                "Come and talk to me before you go wandering around in there."
            )

            return@on true
        }

        on(shared.consts.Scenery.WALL_SAFE_7236, IntType.SCENERY, "crack") { player, node ->

            if (!finishedMoving(player)) {
                return@on true
            }

            if (getStatLevel(player, Skills.THIEVING) < REQUIRED_LEVEL) {
                sendMessage(
                    player, "You need to be level $REQUIRED_LEVEL thief to crack this safe."
                )
                return@on true
            }

            if (freeSlots(player) == 0) {
                sendMessage(player, "Not enough inventory space.")
                return@on true
            }

            val success = success(player)

            lock(player, 4)
            sendMessage(player, "You start cracking the safe.")
            animate(player, ANIMATIONS[if (success) 1 else 0])

            queueScript(player, 3, QueueStrength.SOFT) { stage ->
                when (stage) {
                    0 -> {
                        if (success) {
                            handleSuccess(player, node as Scenery)
                            return@queueScript stopExecuting(player)
                        }

                        if (random(3) == 1) {
                            sendMessage(player, "You slip and trigger a trap!")
                            animate(player, ANIMATIONS[2])
                            impact(player, random(2, 6))
                            return@queueScript keepRunning(player)
                        }

                        return@queueScript stopExecuting(player)
                    }

                    1 -> {
                        resetAnimator(player)
                        return@queueScript stopExecuting(player)
                    }
                }

                return@queueScript stopExecuting(player)
            }

            return@on true
        }

        on(shared.consts.Scenery.FLOOR_7227, IntType.SCENERY, "disarm") { player, _ ->
            animate(player, ANIMATIONS[3])
            sendMessage(player, "You temporarily disarm the trap!")
            return@on true
        }
    }

    private fun handleSuccess(player: Player, scenery: Scenery) {
        replaceScenery(scenery, CRACKED_SAFE, 1)
        sendMessage(player, "You get some loot.")
        rewardXP(player, Skills.THIEVING, EXPERIENCE)
        addLoot(player)
    }

    private fun addLoot(player: Player) {

        val pool = if (random(2) == 1) GEMS else COINS

        val chances = pool.toMutableList()
        chances.shuffle()

        val rand = random(100)

        var item: Item? = null
        var tries = 0

        while (item == null) {

            val chance = chances[0]

            if (rand <= chance.chanceRate) {
                item = chance
                break
            }

            if (tries > chances.size) {
                item = if (chance.id == Items.UNCUT_DIAMOND_1617) {
                    COINS[0]
                } else {
                    chance
                }
                break
            }

            tries++
        }

        player.inventory.add(item)
    }

    private fun success(player: Player): Boolean {

        val level = player.skills.getLevel(Skills.THIEVING).toDouble()
        val req = 50.0

        val mod = if (player.inventory.containsItem(STETHOSCOPE)) {
            8
        } else {
            17
        }

        val successChance = kotlin.math.ceil((level * 50 - req * mod) / req / 3 * 4)

        return successChance >= random(99)
    }
}