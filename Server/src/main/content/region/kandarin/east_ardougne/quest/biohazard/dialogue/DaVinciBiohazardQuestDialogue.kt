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
 * Represents the Da Vinci dialogue.
 *
 * # Relations
 * - [Biohazard][content.region.kandarin.east_ardougne.quest.biohazard]
 */
@Initializable
class DaVinciBiohazardQuestDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        val delivered = getAttribute(player, GameAttributes.SECOND_VIAL_CORRECT, false) ||
            getAttribute(player, GameAttributes.SECOND_VIAL_WRONG, false)
        val questStage = getQuestStage(player, Quests.BIOHAZARD)
        val inRimmington = npc.id == NPCs.DA_VINCI_336

        when {
            inRimmington && delivered -> {
                npcl(FaceAnim.NEUTRAL, "Oh, it's you again. Please don't distract me now, I'm contemplating the sublime.").also { stage = END_DIALOGUE }
            }

            !GameWorld.settings!!.isMembers -> {
                npcl(FaceAnim.ANNOYED, "Bah! A great artist such as myself should not have to suffer the HUMILIATION of spending time on these dreadful worlds where non-members wander everywhere!").also { stage = END_DIALOGUE }
            }

            !inRimmington && delivered -> {
                npc("Hello again. I hope your journey was as pleasant as", "mine.")
                stage = 8
            }

            inRimmington && questStage in 10..15 -> {
                player("Hello, I hear you're an errand boy for the chemist.")
                stage = 0
            }

            else -> {
                sendMessage(player, "Da Vinci does not feel sufficiently moved to talk.")
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc("Well that's my job yes. But I don't necessarily define", "my identity in such black and white terms.").also { stage++ }
            1 -> player("Good for you. Now can you take a vial to Varrock for", "me?").also { stage++ }
            2 -> npc("Go on then.").also { stage++ }
            3 -> options("You give him the vial of ethenea...", "You give him the vial of liquid honey...", "You give him the vial of sulphuric broline...").also { stage++ }
            4 -> {
                val (vial, name) = when (buttonId) {
                    1 -> Items.ETHENEA_415 to "ethenea"
                    2 -> Items.LIQUID_HONEY_416 to "liquid honey"
                    else -> Items.SULPHURIC_BROLINE_417 to "sulphuric broline"
                }
                if (!removeItem(player!!, vial)) {
                    sendMessage(player!!, "You can't give him what you don't have.")
                    end()
                } else {
                    sendMessage(player!!, "You give him the vial of $name.")
                    val key = if (vial == Items.ETHENEA_415) GameAttributes.SECOND_VIAL_CORRECT else GameAttributes.SECOND_VIAL_WRONG
                    setAttribute(player!!, key, true)
                    player("Right. I'll see you later in the Dancing Donkey Inn.")
                    stage = 5
                }
            }
            5 -> npc("That's right.").also { stage = 6 }
            6 -> end()

            8 -> {
                if (getAttribute(player, GameAttributes.SECOND_VIAL_CORRECT, false)) {
                    playerl(FaceAnim.FRIENDLY, "Well, as they say, it's always sunny in Gielinor.")
                    stage = 9
                } else {
                    playerl(FaceAnim.FRIENDLY, "Yep. Anyway, I'll take the package off you now.")
                    stage = 11
                }
            }
            9 -> npc("Ok, here it is.").also { stage++ }
            10 -> {
                sendMessage(player!!, "He gives you the vial of ethenea.")
                addItemOrDrop(player!!, Items.ETHENEA_415, 1)
                removeAttribute(player!!, GameAttributes.SECOND_VIAL_CORRECT)
                player("Thanks, you've been a big help.")
                stage = 21
            }
            11 -> npcl(FaceAnim.HALF_THINKING, "Package? That's a funny way to describe a liquid of such exquisite beauty!").also { stage++ }
            12 -> options("I'm getting a bad feeling about this.", "Just give me the stuff now please.").also { stage++ }
            13 -> when (buttonId) {
                1 -> player("I'm getting a bad feeling about this.").also { stage++ }
                2 -> player("Just give me the stuff now please.").also { stage++ }
            }
            14 -> player("You do still have it don't you?").also { stage++ }
            15 -> npcl(FaceAnim.FRIENDLY, "Absolutely. It's just not stored in a vial anymore.").also { stage++ }
            16 -> player(FaceAnim.ASKING, "What?").also { stage++ }
            17 -> npcl(FaceAnim.FRIENDLY, "Instead it has been liberated. It now gleams from the canvas of my latest epic: The Majesty of Varrock!").also { stage++ }
            18 -> playerl(FaceAnim.FRIENDLY, "That's great. Thanks to you I'll have to walk back to East Ardougne to get another vial.").also { stage++ }
            19 -> npcl(FaceAnim.FRIENDLY, "Well you can't put a price on art.").also { stage++ }
            20 -> {
                end()
                removeAttribute(player!!, GameAttributes.SECOND_VIAL_WRONG)
            }
            21 -> end()
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = DaVinciBiohazardQuestDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.DA_VINCI_336, NPCs.DA_VINCI_337)
}
