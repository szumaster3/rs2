package content.region.fremennik.miscellania.plugin

import content.data.Quests

import content.region.fremennik.miscellania.dialogue.FerdDialogue
import content.region.fremennik.miscellania.dialogue.FullangrDialogue
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.NPCs

class MiscellaniaPlugin : InteractionListener {

    companion object {
        private val MISC_CASTLE_NPC = intArrayOf(
            NPCs.ALRIK_1381,
            NPCs.BRODDI_1390,
            NPCs.EINAR_1380,
            NPCs.RAGNAR_1379,
            NPCs.RAGNVALD_1392,
            NPCs.RANNVEIG_1386,
            NPCs.THORA_1387,
            NPCs.THORHILD_1382,
            NPCs.VALGERD_1388
        )
        private val MISC_DUNG_NPC = intArrayOf(
            NPCs.INGRID_3926,
            NPCs.THORA_3927,
            NPCs.TJORVI_3925,
            NPCs.FERD_3937,
            NPCs.FULLANGR_3934,
            NPCs.RUNOLF_3924
        )
    }

    override fun defineListeners() {
        on(MISC_CASTLE_NPC, IntType.NPC, "talk-to") { player, node ->
            sendPlayerDialogue(player, "Hello.", FaceAnim.FRIENDLY)
            addDialogueAction(player) { _, button ->
                if(button > 0)
                    sendNPCDialogue(player, node.id, "Good day, Your Royal Highness.")
            }
            return@on true
        }

        on(MISC_DUNG_NPC, IntType.NPC, "talk-to") { player, node ->
            when(node.id) {
                NPCs.FERD_3937 -> openDialogue(player, FerdDialogue(), node.id)
                NPCs.FULLANGR_3934 -> openDialogue(player, FullangrDialogue(), node.id)
                else -> {
                    sendPlayerDialogue(player, "Hello.", FaceAnim.FRIENDLY)
                    addDialogueAction(player) { _, button ->
                        if(button > 0)
                            sendNPCDialogue(player, node.id, "Good day, Your Royal Highness.")
                    }
                }
            }
            return@on true
        }

        on(NPCs.FISHERMAN_FRODI_1397, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "Hello!")
            return@on true
        }

        on(NPCs.FARMER_FROMUND_3917, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "Hey! This is the Queen's farm. You'll need her approval to make use of it.")
            return@on true
        }

        on(NPCs.GREENGROCER_1394, IntType.NPC, "talk-to") { player, node ->
            if (!hasRequirement(player, Quests.THRONE_OF_MISCELLANIA.questName, false)) {
                sendNPCDialogueLines(player, node.id, FaceAnim.NEUTRAL, false, "Welcome, Sir.", "I sell only the finest and freshest vegetables!")
            } else {
                sendNPCDialogueLines(player, node.id, FaceAnim.NEUTRAL, false, "Welcome, Your Royal Highness.", "I sell only the finest and freshest vegetables.")
            }
            addDialogueAction(player) { _, button ->
                if(button > 0)
                    openNpcShop(player, node.id)
            }
            return@on true
        }
    }
}
