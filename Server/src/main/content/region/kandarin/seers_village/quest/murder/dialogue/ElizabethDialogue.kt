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
class ElizabethDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, ElizabethDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return ElizabethDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.ELIZABETH_SINCLAIR_818, 6196)
    }
}

class ElizabethDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, Quests.MURDER_MYSTERY.questName)) {
            0 -> sendDialogue(player!!, "They are ignoring you.").also { stage = END_DIALOGUE }
            1 -> when (stage) {
                0 -> playerl(FaceAnim.NEUTRAL, "I'm here to help the guards with their investigation.").also { stage++ }
                1 -> npcl("What's so important you need to bother me with then?").also { stage++ }
                2 -> showTopics(
                    Topic("Who do you think is responsible?", 3),
                    Topic("Where were you when the murder happened?", 4),
                    IfTopic("Do you recognise this thread?", 7, inInventory(player!!, Items.CRIMINALS_THREAD_1808) || inInventory(player!!, Items.CRIMINALS_THREAD_1809) || inInventory(player!!, Items.CRIMINALS_THREAD_1810)),
                    IfTopic("Why'd you buy poison the other day?", 12, getAttribute(player!!,
                        MurderMystery.attributePoisonClue, 0) > 0)
                )
                3 -> npcl("Could have been anyone. The old man was an idiot. He's been asking for it for years.").also { stage = 2 }
                4 -> npcl("I was out.").also { stage++ }
                5 -> playerl("Care to be any more specific?").also { stage++ }
                6 -> npcl("Not really. I don't have to justify myself to the likes of you, you know. I know the King personally you know. Now are we finished here?").also { stage = 2 }
                7 -> sendDialogue(player!!, "You show her the thread from the study window.").also { if (inInventory(player!!, Items.CRIMINALS_THREAD_1810)) stage++ else stage = 11 }
                8 -> npcl(" Looks like Blue thread to me. If you can't work that out for yourself I don't hold much hope of you solving this crime.").also { stage++ }
                9 -> playerl("It looks a lot like the material your trousers are made of doesn't it?").also { stage++ }
                10 -> npcl("I suppose it does. So what?").also { stage = 2 }
                11 -> npcl(" It's some thread. You're not very good at this whole investigation thing are you?").also { stage = 2 }
                12 -> npcl("There was a nest of mosquitos under the fountain in the garden, which I killed with poison the other day. You can see for yourself if you're capable of managing that, which I somehow doubt.").also { stage++ }
                13 -> playerl(FaceAnim.ANNOYED, "I hate mosquitos!").also { stage++ }
                14 -> npcl("Doesn't everyone?")
                    .also { setAttribute(player!!, MurderMystery.attributeAskPoisonElizabeth, true) }
                    .also { stage = END_DIALOGUE }
            }
            100 -> npcl("Apparently you aren't as stupid as you look.").also { stage = END_DIALOGUE }
        }
    }
}