package content.region.karamja.brimhaven.dialogue

import content.region.asgarnia.burthorpe.quest.hero.HeroesQuest
import core.api.getAttribute
import core.api.getQuestStage
import core.api.openDialogue
import core.api.setAttribute
import core.game.dialogue.*
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Grubor dialogue.
 */
@Initializable
class GruborDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, GruborDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("Would you like your hedges trimming?", "I want to come in.", "Do you want to trade?").also { stage++ }
            1 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_ASKING, "Would you like your hedges trimming?").also { stage = 10 }
                2 -> player(FaceAnim.HAPPY, "I want to come in.").also { stage = 20 }
                3 -> player(FaceAnim.HALF_ASKING, "Do you want to trade?").also { stage = 30 }
            }
            10 -> npc(FaceAnim.HALF_GUILTY, "Eh? Don't be daft! We don't even HAVE any hehdges!").also { stage = END_DIALOGUE }
            20 -> npc(FaceAnim.ANNOYED, "No, go away.").also { stage = END_DIALOGUE }
            30 -> npc(FaceAnim.NEUTRAL, "No, I'm busy.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = GruborDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.GRUBOR_789)
}

class GruborDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    getQuestStage(player!!, Quests.HEROES_QUEST) >= 2 &&
                            getAttribute(player!!, HeroesQuest.attributeGruborLetsYouIn, false) &&
                            HeroesQuest.isBlackArm(player!!) -> 1

                    getQuestStage(player!!, Quests.HEROES_QUEST) >= 2 &&
                            !getAttribute(player!!, HeroesQuest.attributeGruborLetsYouIn, false) &&
                            HeroesQuest.isBlackArm(player!!) -> 10

                    else -> 30
                }
            }

            // Already allowed inside.
            1 -> {
                playerl("Hi.")
                stage = 2
            }

            2 -> {
                npcl("Hi, I'm a little busy right now.")
                endFile()
            }

            // Black Arm member who has not been let inside.
            10 -> {
                npcl(FaceAnim.THINKING, "Yes? What do you want?")
                stage = 11
            }

            11 -> {
                options(
                    "Rabbit's foot.",
                    "Four leaved clover.",
                    "Lucky horseshoe.",
                    "Black cat."
                )
                stage = 12
            }

            12 -> when (buttonID) {
                1 -> {
                    playerl("Rabbit's foot.")
                    stage = 14
                }
                2 -> {
                    playerl("Four leaved clover.")
                    stage = 16
                }
                3 -> {
                    playerl("Lucky horseshoe.")
                    stage = 18
                }
                4 -> {
                    playerl("Black cat.")
                    stage = 20
                }
            }

            14 -> {
                npcl("Eh? What are you on about? Go away!")
                endFile()
            }

            16 -> {
                npcl("Oh you're one of the gang are you? Ok, hold up a second, I'll just let you in through here.")
                stage = 21
            }

            18 -> {
                npcl("Eh? What are you on about? Go away!")
                endFile()
            }

            20 -> {
                npcl("Eh? What are you on about? Go away!")
                endFile()
            }

            21 -> {
                dialogue("You hear the door being unbarred from inside.")
                stage = 22
            }

            22 -> {
                setAttribute(player!!, HeroesQuest.attributeGruborLetsYouIn, true)
                endFile()
            }

            // Everyone else.
            30 -> {
                npcl(FaceAnim.THINKING, "Yes? What do you want?")
                stage = 31
            }

            31 -> {
                options(
                    "Would you like your hedges trimming?",
                    "I want to come in.",
                    "Do you want to trade?"
                )
                stage = 32
            }

            32 -> when (buttonID) {
                1 -> {
                    playerl("Would you like your hedges trimming?")
                    stage = 34
                }
                2 -> {
                    playerl("I want to come in.")
                    stage = 36
                }
                3 -> {
                    playerl("Do you want to trade?")
                    stage = 38
                }
            }

            34 -> {
                npcl("Eh? Don't be daft! We don't even HAVE any hedges!")
                endFile()
            }

            36 -> {
                npcl("No, go away.")
                endFile()
            }

            38 -> {
                npcl("No, I'm busy.")
                endFile()
            }
        }
    }
}