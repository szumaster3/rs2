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
 * Represents the Old Man (Ghosts Ahoy quest) dialogue.
 */
class OldManAhoyDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        val p = player!!
        val questStage = getQuestStage(p, Quests.GHOSTS_AHOY)
        npc = NPC(NPCs.OLD_MAN_1696)
        if (questStage < 4 || questStage > 99) {
            end()
            return
        }
        val hasKey = hasAnItem(p, Items.CHEST_KEY_2404).container != null
        val lostKey = !hasKey && questStage >= 5
        val keyOption = if (lostKey) "I've lost my key to the chest - do you have another one?" else "Can I have the key to the chest?"

        when (stage) {
            0 -> if (questStage >= 6) {
                options("What is your name?", keyOption, "Is this your toy boat?", "How is it going?").also { stage++ }
            } else {
                options("What is your name?", keyOption, "Is this your toy boat?").also { stage++ }
            }

            1 -> when (buttonID) {
                1 -> player("What is your name?").also { stage = 2 }
                2 -> if (lostKey) {
                    player("I've lost my key to the chest - do you have another one?").also { stage = 19 }
                } else {
                    player("Can I have the key to the chest?").also { stage = 5 }
                }
                3 -> player("Is this your toy boat?").also { stage = 8 }
                4 -> player("How is it going?").also { stage = 25 }
            }

            2 -> npc("I don't remember. Everyone around here just", "calls me 'boy'.").also { stage++ }
            3 -> player("You're the cabin boy?!?").also { stage++ }
            4 -> npc("Yes, and proud of it.").also { stage = END_DIALOGUE }
            5 -> npc("Hang on, let me ask the Captain ...").also { stage++ }
            6 -> sendDialogue(p, "The old man cocks an ear towards the Pirate Captain's skeleton.").also { stage++ }
            7 -> npc("The Captain says no.").also { stage = END_DIALOGUE }
            8 -> sendDialogue(p, "The old man inspects the toy boat.").also { stage++ }
            9 -> when {
                inInventory(p, Items.MODEL_SHIP_4253) ->
                    npc("No - I made a toy boat a long while ago, but", "that one had a flag.").also { stage = END_DIALOGUE }

                inInventory(p, Items.MODEL_SHIP_4254) && getAttribute(p, GhostsAhoyUtils.rightShip, false) ->
                    npc("My word - so it is!!! I never thought I would see it again!!", "Where did you get it from?").also { stage++ }

                inInventory(p, Items.MODEL_SHIP_4254) ->
                    npc("No - I made a toy boat a long while ago, but the colours", "on the flag were different.").also { stage = END_DIALOGUE }

                else ->
                    npc("Come back if we have anything to", "talk about.").also { stage = END_DIALOGUE }
            }

            10 -> player("Your mother gave it to me to pass on to you.").also { stage++ }
            11 -> npc("My mother? She still lives?").also { stage++ }
            12 -> player("Yes, in a shack to the west of here.").also { stage++ }
            13 -> npc("After all these years ...").also { stage++ }
            14 -> player("Can I have the key to the chest, then?").also { stage++ }
            15 -> npc("Hang on, let me ask the Captain ...").also { stage++ }
            16 -> sendDialogue(p, "The old man cocks an ear towards the skeleton of the Pirate Captain.").also { stage++ }
            17 -> npc("The Captain says yes.").also { stage++ }
            18 -> {
                // The boat is only taken together with the key, so closing the dialogue early can't lose it.
                if (removeItem(p, Items.MODEL_SHIP_4254)) {
                    addItemOrDrop(p, Items.CHEST_KEY_2404)
                    GhostsAhoyUtils.advanceQuest(p, 5)
                    sendItemDialogue(p, Items.CHEST_KEY_2404, "The old man gives you the chest key.")
                }
                stage = END_DIALOGUE
            }

            19 -> npc("What? But the chest is only just over there!", "How on Gielinor did you lose it in that short distance?").also { stage++ }
            20 -> player("Sorry, I won't do it again...").also { stage++ }
            21 -> {
                if (hasAnItem(p, Items.CHEST_KEY_2404).container == null) {
                    addItemOrDrop(p, Items.CHEST_KEY_2404)
                }
                sendItemDialogue(p, Items.CHEST_KEY_2404, "The old man grudgingly gives you another chest key.")
                stage = END_DIALOGUE
            }

            25 -> npc("Wonderful, wonderful! Mother's coming to get me!").also { stage = END_DIALOGUE }
        }
    }
}
