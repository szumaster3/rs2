package content.region.misthalin.edgeville.dialogue

import core.api.getStatLevel
import core.api.sendMessage
import core.api.visualize
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.skill.Skills
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Animations
import shared.consts.NPCs

/**
 * Represents the dialogue for the Edgeville Monk.
 * @author Vexia
 */
@Initializable
class EdgevilleMonkDialogue(player: Player? = null) : Dialogue(player) {

    companion object {
        private val ANIMATION = Animation(Animations.CAST_SPELL_WISE_OLD_710)
        private val GRAPHIC = Graphics(shared.consts.Graphics.MONK_CAST_HEAL_84, 46)
    }

    override fun newInstance(player: Player?): Dialogue = EdgevilleMonkDialogue(player)

    override fun open(vararg args: Any?): Boolean {
        if (args.isNotEmpty()) {
            if (args[0] is NPC) {
                npc = args[0] as NPC
            } else {
                interpreter.sendDialogues(
                    NPCs.MONK_7727,
                    null,
                    "Only members of our order can go up there. You'll",
                    "need to talk to Abbot Langley if you want to explore",
                    "the monastery further."
                )
                stage = 21
                return true
            }
        }

        interpreter.sendDialogues(npc, FaceAnim.HALF_GUILTY, "Greetings traveller.")
        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic("Can you heal me? I'm injured.", 10),
                Topic("Isn't this place built a bit out of the way?", 20),
                Topic("How do I get further into the monastery?", 30)
            )

            10 -> {
                interpreter.sendDialogues(npc, FaceAnim.HALF_GUILTY, "Ok.")
                stage = 11
            }

            11 -> {
                end()
                npc.walkingQueue.reset()
                npc.lock(ANIMATION.duration)
                visualize(npc,ANIMATION,GRAPHIC)
                sendMessage(player, "You feel a little better.")
                player.skills.heal(
                    (getStatLevel(player,Skills.HITPOINTS) * 0.20).toInt()
                )
            }

            20 -> {
                interpreter.sendDialogues(
                    npc,
                    FaceAnim.HALF_GUILTY,
                    "We like it that way actually! We get disturbed less. We still",
                    "get rather a large amount of travellers looking for",
                    "sanctuary and healing here as it is!"
                )
                stage = END_DIALOGUE
            }
            30 -> {
                interpreter.sendDialogues(
                    npc,
                    FaceAnim.HALF_GUILTY,
                    "You'll need to talk to Abbot Langley about that. He's",
                    "usually to be found walking the halls of the monastery."
                )
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MONK_7727)
}