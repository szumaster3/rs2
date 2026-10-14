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
 * Represents the Gravingas (Ghosts Ahoy quest) dialogue.
 */
class GravingasAhoyDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, Quests.GHOSTS_AHOY)
        val started = getAttribute(player!!, GhostsAhoyUtils.petitionstart, false)
        val signs = getAttribute(player!!, GhostsAhoyUtils.petitionsigns, 0)
        npc = NPC(NPCs.GRAVINGAS_1685)
        if (questStage !in 1..99) {
            end()
            return
        }
        when (stage) {
            0 -> when {
                !started -> npc("Will you join with me and protect against the evil ban", "of Nercrovarus and his disciples?").also { stage = 1 }

                getAttribute(player!!, GhostsAhoyUtils.petitionPresented, false) ->
                    npc("Necrovarus has seen the petition, even if he did not", "like it. Keep fighting, friend.").also { stage = END_DIALOGUE }

                !inInventory(player!!, Items.PETITION_FORM_4283) ->
                    npc("Blown away in the sea breeze, hey?", "Oh well, can't be helped. Here's another one, but", "you'll have to start from scratch again.").also { stage = 10 }

                getAttribute(player!!, GhostsAhoyUtils.petitioncomplete, false) ->
                    npc("You've got them all! Now go and present it to", "Necrovarus!!").also { stage = END_DIALOGUE }

                signs == 0 ->
                    npc("Come on - you haven't even started yet! You need 10", "more signatures.").also { stage = END_DIALOGUE }

                else ->
                    npc("Not doing too badly I see! You need ${GhostsAhoyUtils.signaturesNeeded - signs} more", "signatures.").also { stage = END_DIALOGUE }
            }

            1 -> options("After hearing Velorina's story I will be happy to help out.", "I'm sorry, I don't really think I should get involved.").also { stage++ }
            2 -> when (buttonID) {
                1 -> player("After hearing Velorina's story I will be happy to help", "out.").also { stage = 4 }
                2 -> player("I'm sorry, I don't really think I should get involved.").also { stage = 3 }
            }

            3 -> npc("Ah, the youth of today - so apathetic to politics.").also { stage = END_DIALOGUE }
            4 -> {
                addItemOrDrop(player!!, Items.PETITION_FORM_4283, 1)
                setAttribute(player!!, GhostsAhoyUtils.petitionstart, true)
                setAttribute(player!!, GhostsAhoyUtils.petitionsigns, 0)
                npc("Excellent, excellent!! Here - take this petition form, and", "try and get 10 signatures from the townsfolk.").also { stage = END_DIALOGUE }
            }

            10 -> {
                if (inInventory(player!!, Items.PETITION_FORM_4283)) {
                    end()
                } else {
                    addItemOrDrop(player!!, Items.PETITION_FORM_4283, 1)
                    setAttribute(player!!, GhostsAhoyUtils.petitionsigns, 0)
                    removeAttribute(player!!, GhostsAhoyUtils.petitioncomplete)
                    sendItemDialogue(player!!, Items.PETITION_FORM_4283, "Gravingas gives you a new petition form.")
                    stage = END_DIALOGUE
                }
            }
        }
    }
}
