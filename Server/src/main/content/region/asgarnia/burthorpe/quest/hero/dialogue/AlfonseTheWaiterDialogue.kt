package content.region.asgarnia.burthorpe.quest.hero.dialogue

import content.region.asgarnia.burthorpe.quest.hero.HeroesQuest
import core.api.getQuestStage
import core.api.openDialogue
import core.api.openNpcShop
import core.api.setQuestStage
import core.game.dialogue.*
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Alfonse The Waiter dialogue.
 *
 * # Relations
 * - [HeroesQuest]
 */
@Initializable
class AlfonseTheWaiterDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, AlfonseTheWaiterDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("Yes, please.", "No, thank you.", "Where do you get your Karambwan from?").also { stage++ }
            1 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "Yes, please.").also { stage = 10 }
                2 -> player(FaceAnim.HALF_GUILTY, "No, thank you.").also { stage = END_DIALOGUE }
                3 -> player(FaceAnim.HALF_GUILTY, "Where do you get your Karambwan from?").also { stage = 30 }
            }

            10 -> {
                end()
                openNpcShop(player, NPCs.ALFONSE_THE_WAITER_793)
            }

            30 -> npc(
                FaceAnim.HALF_GUILTY,
                "We buy directly off Lubufu, a local fisherman. He",
                "seems to have a monopoly over Karambwan sales.",
            ).also { stage = END_DIALOGUE }
        }
        return false
    }

    override fun newInstance(player: Player?): Dialogue = AlfonseTheWaiterDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.ALFONSE_THE_WAITER_793)
}

/**
 * Represents the Alfonse The Waiter dialogue file.
 *
 * # Relations
 * - [Heroes Quest][content.region.asgarnia.burthorpe.quest.hero.HeroesQuest]
 */
class AlfonseTheWaiterDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                npc("Welcome to the Shrimp and Parrot.", "Would you like to order, sir?")
                stage++
            }

            1 -> {
                options("Yes please.", "No thank you.", "Do you sell Gherkins?", "Where do you get your Karambwan from?")
                stage++
            }

            2 -> {
                when (buttonID) {
                    1 -> {
                        endFile()
                        openNpcShop(player!!, npc!!.id)
                    }

                    2 -> endFile()

                    3 -> {
                        if (getQuestStage(player!!, Quests.HEROES_QUEST) >= 2 && HeroesQuest.isPhoenix(player!!)) {
                            player("Do you sell Gherkins?")
                            stage = 3
                        } else {
                            options("Yes please.", "No thank you.", "Do you sell Gherkins?", "Where do you get your Karambwan from?")
                            stage = 2
                        }
                    }

                    4 -> {
                        npc("We buy directly off Lubufu, a local fisherman. He", "seems to have a monopoly over Karambwan sales.")
                        stage = 4
                    }
                }
            }

            3 -> {
                npc("Hmmmm Gherkins eh? Ask Charlie the cook, round the", "back. He may have some 'gherkins' for you!")
                stage = 5
            }

            4 -> endFile()

            5 -> {
                dialogue("Alfonse winks at you.")
                stage = 6
            }

            6 -> {
                if (getQuestStage(player!!, Quests.HEROES_QUEST) == 2) {
                    setQuestStage(player!!, Quests.HEROES_QUEST, 3)
                }
                endFile()
            }
        }
    }
}
