package content.region.morytania.port_phasmatys.quest.ahoy.dialogue

import content.region.morytania.port_phasmatys.quest.ahoy.plugin.GhostsAhoyUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Robin (Ghosts Ahoy quest) dialogue.
 */
class RobinDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, Quests.GHOSTS_AHOY)
        npc = NPC(NPCs.ROBIN_1694)
        if (questStage !in 1..79) {
            end()
            return
        }
        val wonRunedraw = getAttribute(player!!, GhostsAhoyUtils.getSignedBow, false)
        when (stage) {
            0 -> when {
                !inInventory(player!!, Items.OAK_LONGBOW_845) -> end()
                wonRunedraw -> player("I've had enough of you not paying up - you owe me", "100 gold coins. I'm going to tell the ghosts what you're", "doing.").also { stage = 20 }
                else -> player("Would you sign this oak longbow for me?").also { stage++ }
            }

            1 -> npc("I'm sorry, I don't sign autographs.").also { stage++ }
            2 -> npc("While you're here, though, why don't you have a game", "of Runedraw with me? If you've got 25 gold pieces", "I've got a bag of runes we can use.").also { stage++ }
            3 -> options("Yes, I'll give you a game.", "How do you play Runedraw?", "No, I don't approve of gambling.").also { stage++ }
            4 -> when (buttonID) {
                1 -> player("Yes, I'll give you a game.").also { stage++ }
                2 -> player("How do you play Runedraw?").also { stage = 6 }
                3 -> player("No, I don't approve of gambling.").also { stage = END_DIALOGUE }
            }
            5 -> {
                end()
                openInterface(player!!, Components.AHOY_RUNEDRAW_9)
            }
            6 -> npc("Two players take turns to draw a rune from a bag, which", "contains ten runes in total. Each rune has a", "different value: an air rune is worth one point, up to a", "Nature rune which is worth nine points.").also { stage++ }
            7 -> npc("If a player draws the Death rune then the game is over,", "and they have lost. A player can choose to hold if", "they wish and not draw any more runes, but this runs the", "risk of the other player drawing more runes until", "they have a greater points total and win.").also { stage++ }
            8 -> options("Yes, I'll give you a game.", "No, I don't approve of gambling.").also { stage++ }
            9 -> when (buttonID) {
                1 -> player("Yes, I'll give you a game.").also { stage = 5 }
                2 -> player("No, I don't approve of gambling.").also { stage = END_DIALOGUE }
            }

            20 -> npc("Please don't do that!!! They will suck the life from my", "bones!!!").also { stage++ }
            21 -> player("How about you signing my longbow then?").also { stage++ }
            22 -> npc("Yes, anything!!!").also { stage++ }
            23 -> {
                if (!removeItem(player!!, Items.OAK_LONGBOW_845)) {
                    npc("You need to bring me an oak longbow first.").also { stage = END_DIALOGUE }
                } else {
                    addItemOrDrop(player!!, Items.SIGNED_OAK_BOW_4236)
                    sendItemDialogue(player!!, Items.SIGNED_OAK_BOW_4236, "Robin signs the oak longbow for you.")
                    stage = END_DIALOGUE
                }
            }
        }
    }
}
