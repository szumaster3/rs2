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
class BobDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, BobDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return BobDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.BOB_815, 6193)
    }
}

class BobDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, Quests.MURDER_MYSTERY.questName)) {
            0 -> sendDialogue(player!!, "They are ignoring you.").also { stage = END_DIALOGUE }
            1 -> when (stage) {
                0 -> playerl(FaceAnim.NEUTRAL, "I'm here to help the guards with their investigation.").also { stage++ }
                1 -> npcl("I suppose I had better talk to you then.").also { stage++ }
                2 -> showTopics(Topic("Who do you think is responsible?", 3),
                    Topic("Where were you when the murder happened?", 4),
                    IfTopic(FaceAnim.THINKING, "Do you recognise this thread?", 7, inInventory(player!!, Items.CRIMINALS_THREAD_1808) || inInventory(player!!, Items.CRIMINALS_THREAD_1809) || inInventory(player!!, Items.CRIMINALS_THREAD_1810)),
                    IfTopic("Why'd you buy poison the other day?", 10, getAttribute(player!!,
                        MurderMystery.attributePoisonClue, 0) > 0))

                3 -> npcl("I don't really care as long as no one thinks it's me. Maybe it was that strange poison seller who headed towards the seers village.").also { stage = 2 }
                4 -> npcl("I was walking by myself in the garden.").also { stage++ }
                5 -> playerl("And can anyone vouch for that?").also { stage++ }
                6 -> npcl("No. But I was.").also { stage = END_DIALOGUE }
                7 -> sendDialogue(player!!, "You show him the thread you discovered.").also {
                    if (inInventory(player!!, Items.CRIMINALS_THREAD_1808)
                    ) stage++ else stage = 9
                }
                8 -> npcl("It's some red thread. I suppose you think that's some kind of clue? It looks like the material my trousers are made of.").also { stage = 2 }
                9 -> npcl(FaceAnim.THINKING, "It's some thread. Great clue. No, really.").also { stage = 2 }
                10 -> npcl(FaceAnim.THINKING, "What's it to you anyway?").also { stage++ }
                11 -> npcl(FaceAnim.ANGRY, "If you absolutely must know, we had a problem with the beehive in the garden, and as all of our servants are so pathetically useless, I decided I would deal with it myself. So I did.").also { setAttribute(player!!,
                    MurderMystery.attributeAskPoisonBob, true) }
                    .also { stage = 2 }
            }

            100 -> npcl("Apparently you aren't as stupid as you look.").also { stage = END_DIALOGUE }
        }
    }
}