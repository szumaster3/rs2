package content.global.skill.smithing.item

import core.api.*
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Animations
import shared.consts.Items

/**
 * Handles crafting of dragon shields.
 */
@Initializable
class DragonShieldDialogue : Dialogue {

    constructor() : super()
    constructor(player: Player?) : super(player)

    override fun newInstance(player: Player): Dialogue = DragonShieldDialogue(player)

    override fun open(vararg args: Any): Boolean {
        val player = player ?: return false
        val type = args.getOrNull(0) as? Int ?: return false

        if (!inInventory(player, Items.HAMMER_2347)) {
            sendDialogue(player, "You need a hammer to work the metal with.")
            return false
        }

        when (type) {
            1 -> {
                if (!inInventory(player, Items.SHIELD_RIGHT_HALF_2368) || !inInventory(player, Items.SHIELD_LEFT_HALF_2366)) {
                    sendDialogue(player, "You need the other half of the shield.")
                    return false
                }

                sendDialogueLines(
                    player,
                    "You set to work trying to fix the ancient shield. It's seen some",
                    "heavy action and needs some serious work doing to it."
                )
                stage = 0
            }

            2 -> {
                if (!inInventory(player, Items.ANTI_DRAGON_SHIELD_1540)) {
                    sendDialogue(player, "You need an anti-dragon shield to attach the visage to.")
                    return false
                }
                if (!inInventory(player, Items.DRACONIC_VISAGE_11286)) {
                    sendDialogue(player, "You don't have anything you could attach to the shield.")
                    return false
                }

                sendDialogueLines(
                    player,
                    "You set to work, trying to attach the ancient draconic",
                    "visage to your anti-dragonbreath shield. It's not easy to",
                    "work with the ancient artifact and it takes all of your",
                    "skills as a master smith."
                )
                stage = 10
            }

            else -> return false
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val player = player ?: return false

        when (stage) {
            0 -> {
                lock(player, 5)
                animate(player, Animations.HUMAN_SMITHING_898)
                if (removeItem(player, Items.SHIELD_RIGHT_HALF_2368) && removeItem(player, Items.SHIELD_LEFT_HALF_2366)) {
                    sendDialogueLines(
                        player,
                        "Even for an experienced armourer it is not an easy task, but",
                        "eventually it is ready. You have restored the dragon square shield to",
                        "its former glory."
                    )
                    addItem(player, Items.DRAGON_SQ_SHIELD_1187)
                    rewardXP(player, Skills.SMITHING, 75.0)
                }
                stage = 1
            }

            10 -> {
                lock(player, 5)
                animate(player, Animations.HUMAN_SMITHING_898)
                if (removeItem(player, Items.ANTI_DRAGON_SHIELD_1540) && removeItem(player, Items.DRACONIC_VISAGE_11286)) {
                    sendDialogueLines(
                        player,
                        "Even for an experienced armourer it is not an easy task, but",
                        "eventually it is ready. You have crafted the",
                        "draconic visage and anti-dragonbreath shield into a",
                        "dragonfire shield."
                    )
                    addItem(player, Items.DRAGONFIRE_SHIELD_11284)
                    rewardXP(player, Skills.SMITHING, 2000.0)
                }
                stage = 1
            }

            1 -> {
                end()
            }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(82127843)
}