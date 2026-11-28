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
class DavidDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, DavidDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return DavidDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.DAVID_SINCLAIR_817, 6195)
    }
}

class DavidDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, Quests.MURDER_MYSTERY.questName)) {
            0 -> sendDialogue(player!!, "They are ignoring you.").also { stage = END_DIALOGUE }
            1 -> when (stage) {
                0 -> playerl(FaceAnim.NEUTRAL, "I'm here to help the guards with their investigation.").also { stage++ }
                1 -> npcl("And? Make this quick. I have better things to do than be interrogated by halfwits all day.").also { stage++ }
                2 -> showTopics(
                    Topic("Who do you think is responsible?", 3),
                    Topic("Where were you when the murder happened?", 5),
                    IfTopic(
                        "Do you recognise this thread?",
                        6,
                        inInventory(player!!, Items.CRIMINALS_THREAD_1808) || inInventory(
                            player!!,
                            Items.CRIMINALS_THREAD_1809
                        ) || inInventory(player!!, Items.CRIMINALS_THREAD_1810)
                    ),
                    IfTopic(
                        "Why'd you buy poison the other day?",
                        9,
                        getAttribute(player!!, MurderMystery.attributePoisonClue, 0) > 0
                    )
                )

                3 -> npcl("I don't really know or care. Frankly, the old man deserved to die.").also { stage++ }
                4 -> npcl("There was a suspicious red headed man who came to the house the other day selling poison now I think about it. Last I saw he was headed towards the tavern in the Seers village.").also {
                    stage = END_DIALOGUE
                }

                5 -> npcl("That is none of your business. Are we finished now, or are you just going to stand there irritating me with your idiotic questions all day?").also {
                    stage = END_DIALOGUE
                }

                6 -> sendDialogue(player!!, "You show him the thread you found on the study window.")
                    .also { if (inInventory(player!!, Items.CRIMINALS_THREAD_1809)) stage++ else stage = 8 }

                7 -> npcl("It's some Green thread, like my trousers are made of. Are you finished? I'm not sure which I dislike more bout you, your face or your general bad odour.").also {
                    stage = END_DIALOGUE
                }

                8 -> npcl("No. Can I go yet? Your face irritates me.").also { stage = END_DIALOGUE }

                9 -> npcl(
                    FaceAnim.ANGRY,
                    "There was a nest of spiders upstairs between the two servants' quarters. Obviously I had to kill them before our pathetic servants whined at my father some more."
                ).also { stage++ }

                10 -> npcl(
                    FaceAnim.THINKING,
                    "Honestly, it's like they expect to be treated like royalty! If I had my way I would fire the whole workshy lot of them!"
                )
                    .also { setAttribute(player!!, MurderMystery.attributeAskPoisonDavid, true) }
                    .also { stage = END_DIALOGUE }
            }

            100 -> npcl("Apparently you aren't as stupid as you look.").also { stage = END_DIALOGUE }
        }
    }
}