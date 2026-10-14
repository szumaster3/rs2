package content.region.morytania.port_phasmatys.quest.ahoy.dialogue

import content.region.morytania.port_phasmatys.quest.ahoy.plugin.GhostsAhoyUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Akharanu (Ghosts Ahoy quest) dialogue.
 */
class AkharanuAhoyDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, Quests.GHOSTS_AHOY)
        npc = NPC(NPCs.AK_HARANU_1688)
        if (questStage < 4 || questStage > 99) {
            end()
            return
        }
        val asked = getAttribute(player!!, GhostsAhoyUtils.akharanuAsked, false)
        when (stage) {
            0 -> when {
                questStage >= 80 -> npc("Greetings, honoured friend.").also { stage = END_DIALOGUE }
                inInventory(player!!, Items.SIGNED_OAK_BOW_4236) -> player("I have your signed longbow for you.").also { stage = 12 }
                asked -> player("Have you got an oak longbow that", "I can get Robin to sign for you?").also { stage = 11 }
                else -> player("It's nice to see a human face around here.").also { stage = 1 }
            }

            1 -> npc("My name is Ak-Haranu. I am trader, come", "from many far across sea in east.").also { stage++ }
            2 -> player("You come from the lands of the East?", "Do you have anything that can help me translate a book that", "is scribed in your language?").also { stage++ }
            3 -> npc("Ak-Haranu may help you. A translation manual I have,", "much good for reading Eastern language.").also { stage++ }
            4 -> player("How much do you want for it?").also { stage++ }
            5 -> npc("Ak-Haranu not want money for this book,", "as is such small thing. But there may be something you", "could do for me. I am big admirer of Robin, Master Bowman.", "He staying in village inn.").also { stage++ }
            6 -> player("What would you like me to do?").also { stage++ }
            7 -> npc("Please get Master Bowman sign an oak longbow for me,", "so Ak-Haranu can show family and friends when returning", "home and become much admired. Then I give you", "book in exchange.").also { stage++ }
            8 -> options("Okay, wait here - I'll get you your bow.", "Sorry, I have too much to do at the moment.").also { stage++ }
            9 -> when (buttonID) {
                1 -> player("Okay, wait here - I'll get you your bow.").also { stage++ }
                2 -> player("Sorry, I have too much to do at the moment.").also { stage = END_DIALOGUE }
            }
            10 -> {
                end()
                setAttribute(player!!, GhostsAhoyUtils.akharanuAsked, true)
            }

            11 -> npc("No, Ak-Haranu afraid that no longbow in supply at", "moment. You must make one or buy one.").also { stage = END_DIALOGUE }

            12 -> npc("Ah can it be true? You have obtained bow from Master", "Bowman?").also { stage++ }
            13 -> player("He was more than happy to oblige (cough).", "Here you are.").also { stage++ }
            14 -> {
                if (removeItem(player!!, Items.SIGNED_OAK_BOW_4236)) {
                    addItemOrDrop(player!!, Items.TRANSLATION_MANUAL_4249)
                    setAttribute(player!!, GhostsAhoyUtils.manualGiven, true)
                    sendItemDialogue(player!!, Items.TRANSLATION_MANUAL_4249, "Ak-Haranu gives you a translation manual in return for the signed oak longbow.")
                    stage++
                } else {
                    stage = END_DIALOGUE
                    end()
                }
            }
            15 -> npc("May honour be bestowed upon you and your family!").also { stage = END_DIALOGUE }
        }
    }
}
