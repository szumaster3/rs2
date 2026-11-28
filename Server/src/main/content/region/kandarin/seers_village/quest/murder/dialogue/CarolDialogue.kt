package content.region.kandarin.seers_village.quest.murder.dialogue

import content.data.Quests

import content.region.kandarin.seers_village.quest.murder.MurderMystery
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class CarolDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, CarolDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return CarolDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.CAROL_SINCLAIR_816, 6194)
    }
}

class CarolDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, Quests.MURDER_MYSTERY.questName)) {
            0 -> sendDialogue(player!!, "They are ignoring you.").also { stage = END_DIALOGUE }
            1 -> when (stage) {
                0 -> playerl(FaceAnim.NEUTRAL, "I'm here to help the guards with their investigation.").also { stage++ }
                1 -> npcl("Well, ask what you want to know then.").also { stage++ }
                2 -> showTopics(
                    Topic("Who do you think is responsible?", 3),
                    Topic("Where were you when the murder happened?", 4),
                    IfTopic("Do you recognise this thread?", 5, inInventory(player!!, Items.CRIMINALS_THREAD_1808) || inInventory(player!!, Items.CRIMINALS_THREAD_1809) || inInventory(player!!, Items.CRIMINALS_THREAD_1810)),
                    IfTopic("Why'd you buy poison the other day?", 8, getAttribute(player!!,
                        MurderMystery.attributePoisonClue, 0) > 0)
                )

                3 -> npcl("I don't know. I think it's very convenient that you have arrived here so soon after it happened. Maybe it was you.").also { stage = 2 }
                4 -> npcl("Why? Are you accusing me of something? You seem to have a very high opinion of yourself. I was in my room if you must know, alone.").also { stage = 2 }

                5 -> sendDialogue(player!!, "You show Carol the thread found at the crime scene.").also { if (inInventory(player!!, Items.CRIMINALS_THREAD_1808)) stage++ else stage = 7 }
                6 -> npcl("It's some red thread... it kind of looks like the Same material as my trousers. But obviously it's not.").also { stage = 2 }
                7 -> npcl("It's some thread. Sorry, do you have a point here? Or do you just enjoy wasting peoples time?").also { stage = 2 }
                8 -> npcl(FaceAnim.THINKING, "I don't see what on earth it has to do with you, but the drain outside was").also { stage++ }
                9 -> npcl(FaceAnim.ANNOYED, "blocked, and as nobody else here has the intelligence to even unblock a simple drain I felt I had to do it myself.")
                    .also {
                        setAttribute(player!!, MurderMystery.attributeAskPoisonCarol, true)
                        stage = END_DIALOGUE
                    }
            }
            100 -> npcl("Apparently you aren't as stupid as you look.").also { stage = END_DIALOGUE }
        }
    }
}