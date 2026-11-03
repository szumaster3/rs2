package content.region.karamja.brimhaven.dialogue

import content.region.asgarnia.burthorpe.quest.hero.HeroesQuest
import core.api.getQuestStage
import core.api.openDialogue
import core.api.setQuestStage
import core.game.dialogue.*
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Charlie The Cook dialogue.
 */
@Initializable
class CharlieTheCookDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, CharlieTheCookDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("I'm looking for a gherkin...", "I'm a fellow member of the Phoenix Gang.", "Just exploring.").also { stage++ }
            1 -> when (buttonId) {
                1 -> player("I'm looking for a gherkin...").also { stage = 3 }
                2 -> player("I'm a fellow member of the Phoenix Gang.").also { stage = 3 }
                3 -> player("Just exploring.").also { stage = 2 }
            }
            2 -> npc(FaceAnim.ANNOYED, "Well, get out! This kitchen isn't for exploring.", "It's a private establishment! It's out of bounds", "to customers!").also { stage = END_DIALOGUE }
            3 -> npc(FaceAnim.THINKING, "Ah, a fellow Phoenix! So, tell me compadre...", "What brings you to sunny Brimhaven?").also { stage++ }
            4 -> options("Sun, sand, and the fresh sea air!", "I want to steal Scarface Pete's candlesticks.").also { stage++ }
            5 -> when (buttonId) {
                1 -> player(FaceAnim.HAPPY, "Sun, sand, and the fresh sea air!").also { stage++ }
                2 -> player(FaceAnim.NEUTRAL, "I want to steal Scarface Pete's candlesticks.").also { stage = 8 }
            }
            6 -> npc(FaceAnim.NEUTRAL, "Well, can't say I blame you, compadre.", "I used to be a city boy myself, but have to admit", "it's a lot nicer living here nowadays. Brimhaven's", "certainly good for it.").also { stage++ }
            7 -> npc(FaceAnim.NEUTRAL, "I also want to steal Scarface Pete's candlesticks.").also { stage++ }
            8 -> npc(FaceAnim.NEUTRAL, "Ah yes, of course. The candlesticks.", "Well, I have to be honest with you, compadre, we haven't made", "much progress in that task ourselves so far.").also { stage++ }
            9 -> npc(FaceAnim.NEUTRAL, "We can however offer a little assistance.", "Setting up this restaurant was the start of things;", "we have a secret door out the back of here that leads", "through the back of Cap'n Arnav's garden.").also { stage++ }
            10 -> npc(FaceAnim.NEUTRAL,"Now, at the other side of Cap'n Arnav's garden,", "is an old side entrance to Scarface Pete's mansion.", "It seems to have been blocked off from the rest of", "the mansion some years ago and we can't seem to find a way through.").also { stage++ }
            11 -> npc(FaceAnim.NEUTRAL,"We're positive this is the key to entering", "the house undetected, however, and I promise to let", "you know if we find anything there.").also { stage++ }
            12 -> player(FaceAnim.HALF_ASKING,"Mind if I check it out for myself?").also { stage++ }
            13 -> npc(FaceAnim.FRIENDLY,"Not at all! The more minds we have working", "on the problem, the quicker we get that loot!").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = CharlieTheCookDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.CHARLIE_THE_COOK_794)
}

class CharlieTheCookDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> {
                npc(FaceAnim.ANGRY, "Hey! What are you doing back here?")
                stage = 2
            }

            2 -> {
                if (getQuestStage(player!!, Quests.HEROES_QUEST) >= 3 && HeroesQuest.isPhoenix(player!!)) {
                    options("I'm looking for a gherkin...", "I'm a fellow member of the Phoenix Gang.", "Just exploring.")
                } else {
                    player("Just exploring.")
                }
                stage = 3
            }

            3 -> {
                val isPhoenix = getQuestStage(player!!, Quests.HEROES_QUEST) >= 3 &&
                        HeroesQuest.isPhoenix(player!!)

                when {
                    isPhoenix && buttonID == 1 -> stage = 10
                    isPhoenix && buttonID == 2 -> stage = 20
                    isPhoenix && buttonID == 3 -> stage = 30
                    !isPhoenix && buttonID == 1 -> stage = 30
                }
            }

            // Looking for a gherkin.
            10 -> {
                playerl("I'm looking for a gherkin...")
                stage = 40
            }

            // Fellow member of the Phoenix Gang.
            20 -> {
                playerl("I'm a fellow member of the Phoenix Gang.")
                stage = 40
            }

            30 -> {
                playerl("Just exploring.")
                stage = 31
            }

            31 -> {
                npcl(
                    FaceAnim.ANGRY,
                    "Well, get out! This kitchen isn't for exploring. It's a private establishment! It's out of bounds to customers!"
                )
                endFile()
            }

            40 -> {
                npcl("Ah, a fellow Phoenix! So, tell me compadre... What brings you to sunny Brimhaven?")
                stage = 41
            }

            41 -> {
                options(
                    "Sun, sand, and the fresh sea air!",
                    "I want to steal Scarface Pete's candlesticks."
                )
                stage = 42
            }

            42 -> when (buttonID) {
                1 -> {
                    playerl("Sun, sand, and the fresh sea air!")
                    stage = 51
                }
                2 -> {
                    playerl("I want to steal Scarface Pete's candlesticks.")
                    stage = 70
                }
            }

            51 -> {
                playerl("Sun, sand, and the fresh sea air!")
                stage = 52
            }

            52 -> {
                npcl(
                    "Well, can't say I blame you, compadre. I used to be a city boy myself, but have to admit it's a lot nicer living here nowadays. Brimhaven's certainly good for it."
                )
                stage = 53
            }

            53 -> {
                playerl("I also want to steal Scarface Pete's candlesticks.")
                stage = 70
            }

            70 -> {
                npcl(
                    "Ah yes, of course. The candlesticks. Well, I have to be honest with you, compadre, we haven't made much progress in that task ourselves so far."
                )
                stage = 71
            }

            71 -> {
                npcl(
                    "We can however offer a little assistance. Setting up this restaurant was the start of things; we have a secret door out the back of here that leads through the back of Cap'n Arnav's garden."
                )
                stage = 72
            }

            72 -> {
                npcl(
                    "Now, at the other side of Cap'n Arnav's garden, is an old side entrance to Scarface Pete's mansion. It seems to have been blocked off from the rest of the mansion some years ago and we can't seem to find a way through."
                )
                stage = 73
            }

            73 -> {
                npcl(
                    "We're positive this is the key to entering the house undetected, however, and I promise to let you know if we find anything there."
                )
                stage = 74
            }

            74 -> {
                playerl("Mind if I check it out for myself?")
                stage = 75
            }

            75 -> {
                npcl("Not at all! The more minds we have working on the problem, the quicker we get that loot!")
                stage = 76
            }

            76 -> {
                if (getQuestStage(player!!, Quests.HEROES_QUEST) == 3) {
                    setQuestStage(player!!, Quests.HEROES_QUEST, 4)
                }
                endFile()
            }
        }
    }
}