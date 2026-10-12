package content.region.kandarin.east_ardougne.quest.biohazard.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.GameWorld
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Hops dialogue.
 *
 * # Relations
 * - [Biohazard][content.region.kandarin.east_ardougne.quest.biohazard]
 */
@Initializable
class HopsDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        val delivered = getAttribute(player, GameAttributes.THIRD_VIAL_CORRECT, false) ||
            getAttribute(player, GameAttributes.THIRD_VIAL_WRONG, false)
        val questStage = getQuestStage(player, Quests.BIOHAZARD)
        val inRimmington = npc.id == NPCs.HOPS_340

        when {
            inRimmington && delivered -> {
                npcl(FaceAnim.NEUTRAL, "I suppose I'd better get going. I'll meet you at the Dancing Donkey Inn.")
                stage = END_DIALOGUE
            }

            !GameWorld.settings!!.isMembers && !inRimmington -> {
                sendDialogue(player, "Hops don't wanna talk now. Try different world, HIC!")
                stage = END_DIALOGUE
            }

            !GameWorld.settings!!.isMembers && inRimmington -> {
                sendMessage(player, "He isn't in a fit state to talk.")
                stage = END_DIALOGUE
            }

            !inRimmington && delivered -> {
                player("Hello, how was your journey?")
                stage = 8
            }

            inRimmington && questStage in 10..15 -> {
                player("Hi, I've got something for you to take to Varrock.")
                stage = 0
            }

            else -> {
                if (inRimmington) {
                    sendMessage(player, "He isn't in a fit state to talk.")
                } else {
                    sendMessage(player, "Hops doesn't feel like talking.")
                }
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        npc = NPC(NPCs.HOPS_340)
        when (stage) {
            0 -> npc("Sounds like pretty thirsty work.").also { stage++ }
            1 -> player("Well, there's an Inn in Varrock if you're desperate.").also { stage++ }
            2 -> npc("Don't worry, I'm a pretty resourceful fellow you know.").also { stage++ }
            3 -> options("You give him the vial of ethenea...", "You give him the vial of liquid honey...", "You give him the vial of sulphuric broline...").also { stage++ }
            4 -> {
                val (vial, name) = when (buttonID) {
                    1 -> Items.ETHENEA_415 to "ethenea"
                    2 -> Items.LIQUID_HONEY_416 to "liquid honey"
                    else -> Items.SULPHURIC_BROLINE_417 to "sulphuric broline"
                }
                if (!removeItem(player!!, vial)) {
                    sendMessage(player!!, "You can't give him what you don't have.")
                    end()
                } else {
                    sendMessage(player!!, "You give him the vial of $name.")
                    val key = if (vial == Items.SULPHURIC_BROLINE_417) GameAttributes.THIRD_VIAL_CORRECT else GameAttributes.THIRD_VIAL_WRONG
                    setAttribute(player!!, key, true)
                    player("Ok, I'll see you in Varrock.")
                    stage = 5
                }
            }
            5 -> npc(FaceAnim.HAPPY, "Sure, I'm a regular at the Dancing Donkey Inn as it", "happens.").also { stage = 6 }
            6 -> end()

            8 -> npc("Pretty thirst-inducing actually...").also { stage++ }
            9 -> player("Please tell me that you haven't drunk the contents...").also { stage++ }
            10 -> if (getAttribute(player, GameAttributes.THIRD_VIAL_CORRECT, false)) {
                npc(FaceAnim.SCARED, "Oh the gods no! What do you take me for?")
                stage = 11
            } else {
                npc(FaceAnim.NEUTRAL, "Of course I can tell you that I haven't drunk the contents...")
                stage = 13
            }
            11 -> npc("Here's your vial anyway.").also { stage++ }
            12 -> {
                sendMessage(player!!, "He gives you the vial of sulphuric broline.")
                addItemOrDrop(player!!, Items.SULPHURIC_BROLINE_417, 1)
                removeAttribute(player!!, GameAttributes.THIRD_VIAL_CORRECT)
                player("Thanks, I'll let you get your drink now.")
                stage = 16
            }
            13 -> npcl(FaceAnim.HALF_THINKING, "But I'd be lying. Sorry about that me old mucker, can I get you a drink?").also { stage++ }
            14 -> playerl(FaceAnim.NEUTRAL, "No, I think you've done enough for now.").also { stage++ }
            15 -> {
                end()
                removeAttribute(player, GameAttributes.THIRD_VIAL_WRONG)
            }
            16 -> end()
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = HopsDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.HOPS_340, NPCs.HOPS_341)
}
