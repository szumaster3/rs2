package content.global.skill.smithing.item

import core.api.*
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import shared.consts.Animations
import shared.consts.Items

@Initializable
class GodswordDialogue(player: Player? = null) : Dialogue(player) {

    private var used: Int = -1

    override fun newInstance(player: Player) = GodswordDialogue(player)

    override fun open(vararg args: Any): Boolean {
        used = args[0] as Int
        sendDialogue("You set to work, trying to fix the ancient sword.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val player = player ?: return false
        var matched = false

        for ((pair, result) in COMBINATIONS) {
            if (used in pair) {
                val other = pair.first { it != used }
                if (inInventory(player, other, 1)) {
                    removeItem(player, Item(used))
                    removeItem(player, Item(other))
                    addItem(player, result)
                    player.lock(5)
                    player.animate(ANIMATION)
                    rewardXP(player, Skills.SMITHING, 100.0)
                    sendDialogue("Even for an experienced smith it is not an easy task, but eventually", "it is done.")
                    matched = true
                    break
                }
            }
        }

        if (!matched) {
            end()
            sendMessage(player, "You didn't have all the required items.")
        } else {
            end()
        }
        return true
    }

    override fun getIds() = intArrayOf(62362)

    companion object {
        private val ANIMATION = Animation(Animations.HUMAN_SMITHING_898)
        private val COMBINATIONS = mapOf(
            // Blades.
            setOf(Items.GODSWORD_SHARDS_11692, Items.GODSWORD_SHARD_1_11710) to Items.GODSWORD_BLADE_11690,
            setOf(Items.GODSWORD_SHARDS_11688, Items.GODSWORD_SHARD_3_11714) to Items.GODSWORD_BLADE_11690,
            setOf(Items.GODSWORD_SHARDS_11686, Items.GODSWORD_SHARD_2_11712) to Items.GODSWORD_BLADE_11690,
            // Shards.
            setOf(Items.GODSWORD_SHARD_1_11710, Items.GODSWORD_SHARD_2_11712) to Items.GODSWORD_SHARDS_11686,
            setOf(Items.GODSWORD_SHARD_1_11710, Items.GODSWORD_SHARD_3_11714) to Items.GODSWORD_SHARDS_11688,
            setOf(Items.GODSWORD_SHARD_2_11712, Items.GODSWORD_SHARD_3_11714) to Items.GODSWORD_SHARDS_11692
        )
    }
}
