package content.region.karamja.brimhaven.dialogue

import core.api.*
import core.game.dialogue.*
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Garv dialogue.
 */
@Initializable
class GarvDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, GarvDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("Can I go in there?", "I want for nothing!").also { stage++ }
            1 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_ASKING, "Can I go in there?").also { stage++ }
                2 -> player(FaceAnim.HALF_GUILTY, "I want for nothing!").also { stage = 3 }
            }

            2 -> npc(FaceAnim.HALF_GUILTY, "No. In there is private.").also { stage = END_DIALOGUE }
            3 -> npc(FaceAnim.HALF_GUILTY, "You're one of a very lucky few then.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = GarvDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.GARV_788)
}

class GarvDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when (getQuestStage(player!!, Quests.HEROES_QUEST)) {
                    in 0..2 -> 1
                    in 3..6, 100 -> 10
                    else -> 100
                }
            }

            // Before joining the Black Knights.
            1 -> {
                npcl("Hello. What do you want?")
                stage = 2
            }

            2 -> {
                options("Can I go in there?", "I want for nothing!")
                stage = 3
            }

            3 -> when (buttonID) {
                1 -> stage = 4
                2 -> stage = 5
            }

            4 -> {
                playerl("Can I go in there?")
                stage = 6
            }

            6 -> {
                npcl("No. In there is private.")
                endFile()
            }

            5 -> {
                playerl("I want for nothing!")
                stage = 7
            }

            7 -> {
                npcl("You're one of a very lucky few then.")
                endFile()
            }

            // Heroes' Quest stages 3, 4, 5, 6 and 100.
            10 -> {
                npcl("Hello. What do you want?")
                stage = 11
            }

            11 -> {
                playerl("Hi. I'm Hartigen. I've come to work here.")
                stage = 12
            }

            12 -> {
                stage = if (
                    inEquipment(player!!, Items.BLACK_FULL_HELM_1165) &&
                    inEquipment(player!!, Items.BLACK_PLATEBODY_1125) &&
                    inEquipment(player!!, Items.BLACK_PLATELEGS_1077)
                ) {
                    20
                } else {
                    30
                }
            }

            20 -> {
                npcl("I assume you have your I.D. papers then?")
                stage = 21
            }

            21 -> {
                if (inInventory(player!!, Items.ID_PAPERS_1584)) {
                    npcl("You'd better come in then, Grip will want to talk to you.")
                    if (getQuestStage(player!!, Quests.HEROES_QUEST) == 3) {
                        setQuestStage(player!!, Quests.HEROES_QUEST, 4)
                    }
                    endFile()
                } else {
                    playerl("Uh... Yeah. About that...I must have left them in my other suit of armour.")
                    endFile()
                }
            }

            23 -> {
                playerl("Uh... Yeah. About that...I must have left them in my other suit of armour.")
                endFile()
            }

            30 -> {
                npcl("Hartigen the Black Knight? I don't think so. He doesn't dress like that.")
                endFile()
            }

            100 -> endFile()
        }
    }
}
