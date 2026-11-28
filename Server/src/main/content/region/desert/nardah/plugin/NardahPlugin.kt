package content.region.desert.nardah.plugin

import content.region.desert.nardah.dialogue.*
import core.api.openDialogue
import core.api.sendDialogue
import core.api.sendNPCDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.NPCs

class NardahPlugin: InteractionListener {

    override fun defineListeners() {

        on(NPCs.ELID_MAYOR_3040, IntType.NPC, "talk-to") { player, _ ->
            sendDialogue(player, "The mayor doesn't seem interested in talking to you right now.")
            return@on true
        }

        on(NPCs.ELID_GHASLOR_3029, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "Good day young ${if (player.isMale) "gentleman" else "lady"}.")
            return@on true
        }

        on(NPCs.NKUKU_3032, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, NkukuDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.KAZEMDE_3039, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, KazemdeDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.ROKUH_3045, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, RokuhDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.SEDDU_3038, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, SedduDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.ELID_SHIRATTI_3044, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, ShirattiTheCustodianDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.ZAHRA_3036, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, ZahraDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.ZAHUR_3037, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, ZahurDialogue(), node.asNpc())
            return@on true
        }
    }
}