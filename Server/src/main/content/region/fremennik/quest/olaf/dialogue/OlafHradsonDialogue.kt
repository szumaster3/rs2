package content.region.fremennik.quest.olaf.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.*

/**
 * Represents the Olaf Hradson dialogue.
 */
@Initializable
class OlafHradsonDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg objects: Any?): Boolean {
        npc = objects[0] as NPC
        // If The Fremennik Trials has not been completed.
        if(!isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS)) {
            npcl(FaceAnim.ANGRY, "Outerlander, I have work to be getting on with... Please stop bothering me.").also { stage = END_DIALOGUE }
            return false
        }
        // Talking to Olaf after the quest.
        if (isQuestComplete(player, Quests.OLAFS_QUEST)) {
            npc(FaceAnim.NEUTRAL, "Good to see you again!")
            stage = END_DIALOGUE
            return true
        }
        // Start the Olaf quest.
        if (getQuestStage(player, Quests.OLAFS_QUEST) == 0) {
            handleIntroduction(stage)
            return true
        }

        // Talking to Olaf with the windswept logs.
        if (getQuestStage(player, Quests.OLAFS_QUEST) == 1) {
            if (removeItem(player, Items.WINDSWEPT_LOGS_11035)) {
                handleCarvings(stage)
            } else {
                // Talking to Olaf again without the windswept logs.
                npcl(FaceAnim.HALF_ASKING, "So... Got the logs for me yet?")
                stage = 100
            }
            return true
        }

        if (getQuestStage(player, Quests.OLAFS_QUEST) == 2) {
            handleFreezing(stage)
            return true
        }

        // Talking to Olaf Hradson again after lighting the fire.
        if (getQuestStage(player, Quests.OLAFS_QUEST) == 3) {
            handleFire(stage)
            return true
        }

        // Talking to Olaf Hradson again after receiving Sven's last map.
        if (getQuestStage(player, Quests.OLAFS_QUEST) == 4) {
            // If the player has Sven's last map.
            if(inInventory(player, Items.SVENS_LAST_MAP_11034)){
                npcl(FaceAnim.NEUTRAL, "Hey! I'm trying to carve and you're standing in my light! Move it!")
                stage = END_DIALOGUE
            } else {
                playerl(FaceAnim.NEUTRAL, "Olaf, I seem to have lost your map, can I have another?")
                stage = 110
            }
            return true
        }

        if (getQuestStage(player, Quests.OLAFS_QUEST) == 100) {
            npc(FaceAnim.NEUTRAL, "I'll sit here and practise carving.")
            stage = END_DIALOGUE
            return true
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            100 -> {
                player("Not yet. I just popped back to see how you were doing.")
                stage++
            }

            101 -> {
                npcl(FaceAnim.SAD, "Still cold. Still damp. 'Bout average, really.")
                stage = END_DIALOGUE
            }

            110 -> {
                npcl(FaceAnim.ANGRY, "You lost the treasured map of Sven the Helmsman? How dare you!")
                stage++
            }

            111 -> {
                if(freeSlots(player) == 0) {
                    npcl(FaceAnim.ANGRY, "You don't even have free space for a new one! Go away and don't bother me until you do.")
                } else {
                    npcl(FaceAnim.NEUTRAL, "You'd best take better care of this one, or else!")
                    addItemOrDrop(player, Items.SVENS_LAST_MAP_11034)
                }
                stage = END_DIALOGUE
            }

            else -> {
                when (getQuestStage(player, Quests.OLAFS_QUEST)) {
                    0 -> handleIntroduction(stage)
                    1 -> handleCarvings(stage)
                    2 -> handleFreezing(stage)
                    3 -> handleFire(stage)
                }
            }
        }

        return true
    }

    private fun handleIntroduction(s:Int) {
        val p = player!!

        when (stage) {
            // If The Fremennik Trials has been completed.
            0 -> npc(FaceAnim.NEUTRAL, "Who is that? Identify yourself!").also { stage++ }
            1 -> player("It's me, ${p.username}. What are you doing out here?").also { stage++ }
            2 -> npc(FaceAnim.NEUTRAL, "Nothing... Just carving these horribly broken pieces of", "driftwood.").also { stage++ }
            3 -> npc(FaceAnim.SAD, "In the middle of nowhere.").also { stage++ }
            4 -> npc(FaceAnim.SAD, "In damp clothing.").also { stage++ }
            5 -> player("Okay...").also { stage++ }
            6 -> player("Well, have a nice day.").also { stage++ }

            7 -> npc(FaceAnim.NEUTRAL, "Wait! Would you do me a favour?").also { stage++ }
            8 -> player(FaceAnim.HALF_ASKING, "What kind of a favour?").also { stage++ }
            9 -> npc(FaceAnim.SAD, "Look, you see that heap of driftwood over there? That is","my ship, and all that is left of my one shot at making","my ancestors proud of me.").also { stage++ }
            10 -> npc(FaceAnim.SAD, "My family have always been great explorers and","seafarers, and I recently set off on my maiden voyage.").also { stage++ }
            11 -> npc(FaceAnim.SAD, "However, a big storm - yes, a huge storm in fact - blew", "my ship onto the rocks. Three times.").also { stage++ }
            12 -> npc(FaceAnim.SAD, "I've been sitting here ever since, trying to think of a","way to make my family proud, and if you will help","me...I think I have a way.").also { stage++ }
            13 -> showTopics(
                Topic("Okay, I'll help you out.", 14),
                Topic("Possibly later, when you don't smell so damp.", END_DIALOGUE)
            )
            14 -> npc(FaceAnim.FRIENDLY, "Thank you! I don't have much apart from my carvings,", "some driftwood and the map I was following when", "I had my little...").also { stage++ }
            15 -> npc(FaceAnim.NEUTRAL, "...accident.").also { stage++ }
            16 -> npc(FaceAnim.FRIENDLY, "If you help me out, then I'll gladly hand it over.").also { stage++ }
            17 -> player("So, what is your plan?").also { stage++ }
            18 -> npc(FaceAnim.NEUTRAL, "Well, I have a little food left, although it's a little briny.").also { stage++ }
            19 -> npc(FaceAnim.NEUTRAL, "If you take a couple of my carvings to my wife and son in", "the village over the hill and tell them that I sent you", "with them from a distant island, then I can wait out", "here for a week or so before returning.").also { stage++ }
            20 -> player("Alright, hand me over the carvings and I'll be on my way.").also { stage++ }
            21 -> npc(FaceAnim.NEUTRAL, "Well, none of these are any good. I'll need some fresh", "wood from a decent tree.").also { stage++ }
            22 -> npc(FaceAnim.NEUTRAL, "When I was swimming for my life I saw an odd-looking", "windswept tree to the east of here, up that cliff.").also { stage++ }
            23 -> npc(FaceAnim.NEUTRAL, "Could you bring me some of that wood so I can work", "it?").also { stage++ }
            24 -> player("Wow, you must have pretty good eyes to be able to see", "a single tree from the water during a heavy storm.").also { stage++ }
            25 -> npc(FaceAnim.NEUTRAL, "Yes...great eyes...they run in the family.").also {
                setQuestStage(p, Quests.OLAFS_QUEST, 1)
                setVarbit(p, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 1, save = true)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleCarvings(s: Int) {
        when (stage) {
            0 -> player("Olaf. I cut some logs from the tree. Here's the wood.").also { stage++ }
            1 -> npc(FaceAnim.NEUTRAL, "Wonderful! I'll carve them up now.").also { stage++ }
            2 -> {
                lock(player, 1)
                sendDialogue(player, "Olaf carves furiously.")
                animate(npc, Animations.FLETCH_LOGS_1248)
                stage++
            }
            3 -> {
                npc(FaceAnim.NEUTRAL, "Done!")
                addItemOrDrop(player, Items.CRUDE_CARVING_11032)
                addItemOrDrop(player, Items.CRUDER_CARVING_11033)
                stage++
            }
            4 -> player("That's it?").also { stage++ }
            5 -> npc(FaceAnim.FRIENDLY, "Yep!").also { stage++ }
            6 -> npc(FaceAnim.FRIENDLY, "This is for my wife, Ingrid. I'm sure she will love it.", "Tell her it's tribal or something.").also { stage++ }
            7 -> player("Sure...").also { stage++ }
            8 -> npc(FaceAnim.FRIENDLY, "And this one is for my son, Volf. Kids love planks in", "my experience. Tell him I said hello.").also { stage++ }
            9 -> player("Alright. They live in Rellekka, right?").also { stage++ }
            10 -> npc(FaceAnim.NEUTRAL, "Yes. Hurry back! And don't forget the story: I'm not", "here, I'm someplace far away.").also {
                setQuestStage(player, Quests.OLAFS_QUEST, 2)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleFreezing(s: Int) {
        when (stage) {
            // Talking to Olaf Hradson before delivering the carvings.
            0 -> {
                val progress = getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534)

                if (progress < 3) {
                    val needWife = progress < 2
                    val needSon = !getAttribute(player, GameAttributes.OLAF_VOLF_DELIVERED, false)
                    val lostCrude = needWife && !inInventory(player, Items.CRUDE_CARVING_11032)
                    val lostCruder = needSon && !inInventory(player, Items.CRUDER_CARVING_11033)

                    if (lostCrude || (needWife && !lostCruder)) {
                        npc("So, how did my wife like her carving?").also { stage = 9 }
                    } else {
                        npc("Did my son like his present? I'll bet he was thrilled.").also { stage = 12 }
                    }
                } else if (progress == 4) {
                    // Talking to Olaf Hradson again before lighting the fire.
                    if (inInventory(player, Items.DAMP_PLANKS_11031)) {
                        npcl(FaceAnim.SAD, "So...very...cold. Almost cold enough to tear up an old map with my fevered shivering. Cough, cough.")
                        stage = END_DIALOGUE
                    } else if (freeSlots(player) == 0) {
                        playerl(FaceAnim.NEUTRAL, "Hey, Olaf. I dropped those slimy planks you gave me. They were not going to burn at all.")
                        stage = 15
                    } else {
                        playerl(FaceAnim.NEUTRAL, "Hey, Olaf. I dropped those slimy planks you gave me. Sorry, they were just too slippery to hold on to.")
                        stage = 16
                    }
                } else {
                    player("Olaf, are you all right?").also { stage++ }
                }
            }
            1 -> npc(FaceAnim.NEUTRAL, "So... cold... need... fire.").also {
                if(getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534) != 3){
                stage = END_DIALOGUE
            } else {
                stage++
            }}
            2 -> player("I delivered your carvings as you asked.", "Your family would like to see you again.").also { stage++ }
            3 -> player("Here, let me get you to the village before you freeze to","death.").also { stage++ }
            4 -> npc(FaceAnim.ANGRY, "I won't...go...anywhere.", "I don't...care... if I...freeze.").also { stage++ }
            5 -> npc(FaceAnim.ANGRY, "I'd rather...die...than suffer...the shame.").also { stage++ }
            // If the player does not have at least one free backpack space.
            6 -> if(freeSlots(player) == 0) {
                npcl(FaceAnim.SAD, "Just get the...fire burning again. I have...some driftwood...you can use...when you can carry it.").also { stage = END_DIALOGUE }
            } else {
                npc(FaceAnim.ANGRY, "Just get the...fire burning. Use...these.")
                addItemOrDrop(player, Items.DAMP_PLANKS_11031, 1)
                stage++
            }
            7 -> player("Well, at least give me the map like you promised!").also { stage++ }
            8 -> npc(FaceAnim.NEUTRAL, "Too...cold...to...hear...whining.").also {
                setVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 4, true)
                stage = END_DIALOGUE
            }
            9 -> if(!inInventory(player, Items.CRUDE_CARVING_11032)){
                player("I don't know... I may have accidentially lost it.").also { stage++ }
            } else {
                player("I don't know... I still have to give it to her.").also { stage = 11 }
            }
            10 -> if(freeSlots(player) == 0) {
                npcl(FaceAnim.ANGRY, "Useless! Here: I carved a few more while you were gone - give her this one and be quick about it!")
                stage = END_DIALOGUE
            } else {
                npcl(FaceAnim.FRIENDLY, "Well, I have some more for you, but if you can't carry it now you will have to come back when you can!")
                addItemOrDrop(player, Items.CRUDE_CARVING_11032)
                stage = END_DIALOGUE
            }
            11 -> npcl(FaceAnim.FRIENDLY, "Well, hurry up and give it to her then!").also { stage = END_DIALOGUE }
            12 -> if (!inInventory(player, Items.CRUDER_CARVING_11033)) {
                player("I don't know... I may have accidentially lost it.").also { stage++ }
            } else {
                player("I'll bet he will be... As soon as I go and give it to him.").also { stage = 14 }
            }
            13 -> if(freeSlots(player) == 0) {
                npcl(FaceAnim.ANGRY, "So, you can lose my son's carving and still have time to fill your backpack with rubbish. I'll make a replacement - you can come and get it when you can carry it.")
                stage = END_DIALOGUE
            } else {
                npcl(FaceAnim.FRIENDLY, "Gah! Are you doing this deliberately now? Here, have another - and try not to lose this one!")
                addItemOrDrop(player, Items.CRUDER_CARVING_11033)
                stage = END_DIALOGUE
            }
            14 -> npcl(FaceAnim.NEUTRAL, "Well, take your time over it, why don't you.").also { stage = END_DIALOGUE}
            15 -> npcl(FaceAnim.NEUTRAL, "Well...when you feel...like helping me...I'll give you some more. But you will need...to be able...to carry them.").also { stage = END_DIALOGUE}
            16 -> npcl(FaceAnim.NEUTRAL, "Well, here, have another load. And get it right this time. I can feel...my life slipping away...cough, cough. Splutter.").also {
                addItemOrDrop(player, Items.DAMP_PLANKS_11031, 1)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleFire(s: Int) {
        when (stage) {
            // Talking to Olaf Hradson again after lighting the fire.
            0 -> if(getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534) != 4) {
                npcl(FaceAnim.SAD,"So...very...cold. Almost cold enough to tear up an old map with my fevered shivering. Cough, cough.").also { stage = END_DIALOGUE }
            } else {
                npc(FaceAnim.NEUTRAL, "Ahhhhhhhh... Much better.").also { stage++ }
            }
            1 -> player("Well, I'm glad you're happy. Can I have that map now?").also { stage++ }
            // If the player does not have any bread or cooked fish.
            2 -> if(!inInventory(player, Items.BREAD_2309) || !inInventory(player, Items.SHARK_385)){
                npcl(FaceAnim.NEUTRAL, "Well, regardless, you've more than earned this map. It was the last one my grandfather, Sven the Helmsman, ever made.")
                stage = 16
            } else {
                npc(FaceAnim.NEUTRAL, "Well, okay... but could I have a little of that food first?", "I've had nothing but seaweed and biscuits for the past","week.").also { stage++ }
            }
            3 -> showTopics(
                Topic("Alright, here, have some food. Now give me the map.", 4),
                Topic("Not a chance.", END_DIALOGUE)
            )
            4 -> npc(FaceAnim.NEUTRAL, "Oh... That was a banquet compared to the stuff I've", "been living off.").also { stage++ }
            5 -> npc(FaceAnim.NEUTRAL, "Look, I have a bit of a confession. I think that the map","may well be cursed.").also { stage++ }
            6 -> player("What? All this running about for a cursed map!", "Couldn't you have made this little revelation before I fed","you?").also { stage++ }
            7 -> npc(FaceAnim.NEUTRAL, "Look, I'm sorry, okay. Remember that I followed this", "map and ended up on the rocks.").also { stage++ }
            8 -> npc(FaceAnim.NEUTRAL, "It may be nothing, after all, but I think you should be", "very careful if you go looking for the treasure.").also { stage++ }
            9 -> npc(FaceAnim.NEUTRAL, "Well, regardless, you've more than earned this map. It", "was the last one my grandfather, Sven the Helmsman,","ever made.").also { stage++ }
            10 -> npc(FaceAnim.NEUTRAL, "It's supposed to show the location of one of his stashes", "of treasure, but I have never been able to decipher the", "strange, runic inscriptions that point the way.").also { stage++ }
            11 -> npc(FaceAnim.NEUTRAL, "I wish you luck!").also { stage++ }
            12 -> player("Well, thanks. What will you do now?").also { stage++ }
            13 -> npc(FaceAnim.NEUTRAL, "I'll sit here and practise carving.").also { stage++ }
            14 -> npc(FaceAnim.NEUTRAL, "If you pass this way again, I'll be glad if you drop my", "next load of work off with my family.").also { stage++ }
            15 -> npc(FaceAnim.NEUTRAL, "That or bring me some food before I starve.","Whichever.").also {
                setQuestStage(player, Quests.OLAFS_QUEST, 4)
                addItemOrDrop(player, Items.SVENS_LAST_MAP_11034)
                stage = END_DIALOGUE
            }
            16 -> npcl(FaceAnim.NEUTRAL,"It supposed to show the location of one of his stashes of treasure, but I have never been able to decipher the strange, runic inscriptions that point the way.").also { stage = 11 }
        }
    }
    
    override fun newInstance(player: Player?): Dialogue = OlafHradsonDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.OLAF_HRADSON_2621)
}