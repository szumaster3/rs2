package content.region.misthalin.varrock.quest.surok.dialogue

import content.data.Quests

import content.region.misthalin.varrock.diary.dialogue.RatBurgissDiaryDialogue
import content.region.misthalin.varrock.quest.surok.WhatLiesBelow
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Rat Burgiss dialogue.
 * @author Vexia
 */
@Initializable
class RatBurgissDialogue(player: Player? = null) : Dialogue(player) {

    private var quest: Quest? = null
    private var beforeQuestStage = 0
    private var afterQuestStage  = 0

    override fun open(vararg args: Any): Boolean {
        npc = args[0] as NPC
        quest = player.getQuestRepository().getQuest(Quests.WHAT_LIES_BELOW.questName)
        options("Hello there!", "I have a question about my Achievement Diary.")
        stage = -1
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        if (stage == -1) {
            when (buttonId) {
                1 -> player("Hello there!").also {
                    stage = if(getStatLevel(player, Skills.RUNECRAFTING) < 35) {
                        1500
                    } else if (quest?.isCompleted(player) == true) {
                        1000
                    } else {
                        5
                    }
                }
                2 -> {
                    player("I have a question about my Achievement Diary.")
                    loadFile(RatBurgissDiaryDialogue())
                }
            }
            return true
        }
        if(stage == 1500){
            when(beforeQuestStage){
                0 -> npcl(FaceAnim.FRIENDLY,"Oh, hello. I'd love to chat right now, but I'm a bit busy. Perhaps you could come back and chat some other time?").also { beforeQuestStage++ }
                1 -> playerl(FaceAnim.FRIENDLY, "Of course. Sorry to bother you!").also { beforeQuestStage++ }
                2 -> npcl(FaceAnim.FRIENDLY, "Not at all! Farewell!").also { stage = END_DIALOGUE }
            }
        }
        if(stage == 1000){
            when(afterQuestStage){
                0 -> npcl(FaceAnim.FRIENDLY,"Ah! ${player.username}! You did a fine service for us. You might make a good member of the VPSG one day, with a little training and a bit more muscle!").also { afterQuestStage++ }
                1 -> playerl(FaceAnim.HALF_ASKING, "So, do you have any more jobs for me to do?").also { afterQuestStage++ }
                2 -> npcl(FaceAnim.FRIENDLY, "At the moment, no. Things seem pretty quiet. However, I have heard a rumour about something strange going on in...hmm, no, I think we can handle this one for now.").also { afterQuestStage++ }
                3 -> npcl(FaceAnim.FRIENDLY, "But, who knows? We may need your assistance again soon. Thank you, ${player.username}").also { afterQuestStage++ }
                4 -> playerl(FaceAnim.FRIENDLY, "Any time, Rat!").also { stage = END_DIALOGUE }
            }
        }
        when (quest!!.getStage(player)) {
            0 -> when (stage) {
                5 -> npc(FaceAnim.FRIENDLY, "Oh, hello. I'm Rat.").also { stage++ }
                6 -> player(FaceAnim.ASKING,"You're a what?").also { stage++ }
                7 -> npc(FaceAnim.STRUGGLE, "No, no. My name is Rat. Rat Burgiss.").also { stage++ }
                8 -> player(FaceAnim.NEUTRAL, "Ohhhhh, well, what's up, Ratty?").also { stage++ }
                9 -> npc("It's Rat, thank you. And I, uh..heh...I seem to", "be in a bit of trouble here, as", "you can probably see.").also { stage++ }
                10 -> player("Why, what seems to be the matter?").also { stage++ }
                11 -> npc("Well, I'm a trader by nature and I was on the way to", "Varrock with my cart here when I was set upon by", "outlaws! They ransacked my cart and stole", "some very important papers that I must get back.").also { stage++ }
                12 -> player("Shall I get them back for you?").also { stage++ }
                13 -> npc("You mean you want to help?").also { stage++ }
                14 -> player("Of course! Tell me what you need me to do.").also { stage++ }
                15 -> npc("Right, now I heard those outlaws say something about", "having a small campsite somewhere to the west of the", "Grand Exchange. They headed off to the north-west of", "here, taking five pages with them.").also { stage++ }
                16 -> npc("Kill the outlaws and get those papers back from them for", "me. Here's a folder in which you can put the", "pages. Be careful, though; those outlaws are tough.").also { stage++ }
                17 -> npc("When you find all 5 pages, put them back in the folder", "and bring them back to me!").also { stage++ }
                18 -> player("Don't worry, Ratty! I won't let you down!").also { stage++ }
                19 -> npc("...").also { stage++ }
                20 -> {
                    quest!!.start(player)
                    end()
                }
            }

            10 -> when (stage) {
                0 -> npc("Hello again! How are things going?").also { stage++ }
                1 -> if (inInventory(player, WhatLiesBelow.FULL_FOLDER, 1)) {
                    player("I got your pages back!").also { stage = 5 }
                } else if (!inInventory(player, WhatLiesBelow.EMPTY_FOLDER, 1) &&
                    !inInventory(player, WhatLiesBelow.USED_FOLDER, 1) &&
                    !inInventory(player, WhatLiesBelow.FULL_FOLDER, 1) &&
                    !inBank(player, WhatLiesBelow.EMPTY_FOLDER, 1) &&
                    !inBank(player, WhatLiesBelow.USED_FOLDER, 1) &&
                    !inBank(player, WhatLiesBelow.FULL_FOLDER, 1))
                {
                    player(FaceAnim.HALF_ASKING, "I lost the folder you gave me. Do you have another", "one?").also { stage = 3 }
                } else {
                    player("Good!").also { stage++ }
                }

                2 -> end()
                3 -> {
                    npc("Sure. Here you go. I'll add it to your account.")
                    addItem(player, WhatLiesBelow.EMPTY_FOLDER, 1, Container.INVENTORY)
                    stage++
                }

                4 -> end()
                5 -> npc("Excellent! I knew you could help! Let me take", "those from you, there.").also { stage++ }
                6 -> npc("Now, I liked the way you handled yourself on that last", "little 'mission' I gave you there, so I'm going to let", "you in on a little secret!").also { stage++ }
                7 -> player("Wait! Wait! Let me guess! You're a actually a rich prince", "in disguise who wants to help poor people", "like me!?").also { stage++ }
                8 -> npc("Uhhh...no. No, that's not it. You know, on second thought. I", "think I'll keep my secret for now. Look, instead", "you can do another job for me.").also { stage++ }
                9 -> player("All work and no play makes " + player.username + " a dull adventurer!").also { stage++ }
                10 -> npc("Yes, well, I'm sure that may be the case. However, what", "I want you to do is take this letter to someone", "for me. It's in a different language so, trust me you", "won't be able to read it.").also { stage++ }
                11 -> npc("Take it to a wizard named Surok Magis who resides in", "the Varrock Palace Library. I'll see about some sort of", "reward for your work when I get myself sorted out here.").also { stage++ }
                12 -> player("Letter. Wizard. Varrock. Library. Got it!").also { stage++ }
                13 -> npc("Yes, good luck then.").also { stage++ }
                14 -> {
                    if(removeItem(player, WhatLiesBelow.FULL_FOLDER, Container.INVENTORY)) {
                        addItem(player, WhatLiesBelow.RATS_LETTER, 1, Container.INVENTORY)
                        quest!!.setStage(player, 20)
                    }
                    end()
                }
            }

            20 -> when (stage) {
                0 -> npc("Ah, hello. How is your task going?").also { stage++ }
                1 -> if (!inInventory(player, WhatLiesBelow.RATS_LETTER, 1) && !inBank(player, WhatLiesBelow.RATS_LETTER, 1)) {
                    player("I think I lost that letter you gave me!").also { stage = 3 }
                } else {
                    player("Good!").also { stage++ }
                }
                2 -> end()
                3 -> npc("Goodness me! Not much of a messenger, are you? Here's", "another one; try not to lose it this time! I've charged the", "parchment to your account.").also { stage++ }
                4 -> player("Will you take cheque?").also { stage++ }
                5 -> npc("No thanks. I prefer tartan.").also { stage++ }
                6 -> end().also {
                    if (!inInventory(player, WhatLiesBelow.RATS_LETTER, 1)) {
                        addItem(player, WhatLiesBelow.RATS_LETTER, 1, Container.INVENTORY)
                    }
                }
            }

            50 -> when (stage) {
                0 -> npc("Ah, " + player.username + "! You've returned!").also { stage++ }
                1 -> if (!inInventory(player, WhatLiesBelow.SUROKS_LETTER, 1)) {
                    player("I'm still searching!").also { stage = 2 }
                } else {
                    player("Yes! I have a letter for you.").also { stage = 3 }
                }
                2 -> end()
                3 -> npc("A letter for me? Let me see.").also { stage++ }
                4 -> if (inInventory(player, WhatLiesBelow.SUROKS_LETTER, 1)) {
                    npc("This letter is treasonous! This does indeed confirm my", "worst fears. It is time I let you into my", "secret and hopefully this will answer any questions", "you may have.")
                    stage++
                } else end()
                5 -> player("Okay, go on.").also { stage++ }
                6 -> npc("I am not really a trader. I am the Commander of", "the Varrock Palace Secret Guard, VPSG for short.").also { stage++ }
                7 -> player("Okay, I had a feeling you weren't a real trader due to", "the fact that you had nothing to sell! So why", "the secrecy?").also { stage++ }
                8 -> npc("I'm just getting to that. A short while ago, we received", "word that Surok had discovered a powerful mind-control", "spell and intended to use it on King Roald himself!").also { stage++ }
                9 -> npc("He could control the whole kingdom that way!").also { stage++ }
                10 -> player("I think I can believe that. Surok's not the nicest", "person in Misthalin!").also { stage++ }
                11 -> npc("Yes, but until now, the spell has been useless to him", "as he is currently under guard at the palace and not", "allowed to leave. He could not get the tools for the spell", "because if he left the palace he would be arrested!").also { stage++ }
                12 -> player("Uh oh! I think I may have helped him by mistake, here.", "He promised me a big reward if I collected some items for", "him..but he said it was for a spell to make gold!").also { stage++ }
                13 -> npc("I assume you did not know of his plans; that is", "why you weren't arrested!").also { stage++ }
                14 -> player("Thank you! How can I help fix this mistake?").also { stage++ }
                15 -> npc("Okay, here's what I need you to do. One of my contacts", "has devised a spell that he is sure will be able to", "counteract the effects of the mind-control spell. I need", "you to visit him.").also { stage++ }
                16 -> player("Okay, who is it?").also { stage++ }
                17 -> npc("His name is Zaff. He runs a staff shop in Varrock. Go", "and speak to him and he will tell you what you should", "do. I will send word to him to let him know that", "you are coming.").also { stage++ }
                18 -> player("Yes, sir! I'm on my way!").also { stage++ }
                19 -> {
                    if (removeItem(player, WhatLiesBelow.SUROKS_LETTER, Container.INVENTORY)) {
                        quest!!.setStage(player, 60)
                    }
                    end()
                }
            }

            60, 70 -> when (stage) {
                0 -> npc("Yes, " + player.username + "?").also { stage++ }
                1 -> player("Nevermind.").also { stage++ }
                2 -> end()
            }

            80, 90 -> when (stage) {
                0 -> npc("Well, " + player.username + ", how did it go?").also { stage++ }
                1 -> player("You should have been there! There was this...and Surok", "was like...and I was...and then King...and", "and...uh...ahem! The mission was accomplished and the", "king has been saved.").also { stage++ }
                2 -> npc("I take it that it went alright, then? That's great news!").also { stage++ }
                3 -> npc("Zaff has already briefed me on the events. We will", "arrange for Surok to be fed and watched. I think he", "will not to be a problem any more.").also { stage++ }
                4 -> player("You know, one thing bother's me. He's now stuck in the", "library, but wasn't that the reason we were in this mess", "in the first place?").also { stage++ }
                5 -> npc("Yes, you are right. But rest assured, we will be", "watching him much more closely from now on.").also { stage++ }
                6 -> npc("You've done very well and have been a credit to the", "VPSG; perhaps one day there may be a place for you", "here!").also { stage++ }
                7 -> npc("In the meantime, let me reward you for what you've", "done. I will be sure to call on you if we ever need help", "in the future.").also { stage++ }
                8 -> {
                    quest!!.finish(player)
                    end()
                }
            }

            100 -> when (stage) {
                0 -> npc("Ah, " + player.username + "! You did a fine service for us. You might", "make a good member of the VPSG one day. With a little", "training and a bit more muscle!").also { stage++ }
                1 -> end()
            }
            else -> end()
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue {
        return RatBurgissDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.SUROK_RAT_5833)
    }
}
