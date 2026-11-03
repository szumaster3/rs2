package content.region.misthalin.dig_site.quest.itexam.dialogue

import content.region.misthalin.dig_site.quest.itexam.TheDigSite
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class StudentGreenDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, StudentGreenDialogueFile(), npc)
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.STUDENT_615)
}

class StudentGreenDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    getQuestStage(player!!, Quests.THE_DIG_SITE) in 6..13 || getQuestStage(player!!, Quests.THE_DIG_SITE) == 100 -> 100
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 5 -> 200
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 4 -> 300
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrGreenExam1ObtainAnswer, false) -> 400
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrGreenExam1Talked, false) -> 500
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 -> 600
                    else -> 700
                }
            }

            // Quest completed / artefact found
            100 -> {
                npcl(FaceAnim.FRIENDLY, " Oh, hi again. News of your find has spread fast; you are quite famous around here now.")
                endFile()
            }

            // Earth Sciences Level 3
            200 -> {
                playerl(FaceAnim.FRIENDLY, "Hello there.")
                stage = 201
            }

            201 -> {
                npcl(FaceAnim.FRIENDLY, "How's it going?")
                stage = 202
            }

            202 -> {
                playerl(FaceAnim.FRIENDLY, "I need more help with the exam.")
                stage = 203
            }

            203 -> {
                npcl(FaceAnim.FRIENDLY, "Well, okay, this is what I have learned since I last spoke to you...")
                stage = 204
            }

            204 -> {
                npcl(FaceAnim.FRIENDLY, "Specimen brush use: Brush carefully and slowly using short strokes.")
                stage = 205
            }

            205 -> {
                playerl(FaceAnim.FRIENDLY, "Okay, I'll remember that. Thanks for all your help.")
                stage = 206
            }

            206 -> {
                setAttribute(player!!, TheDigSite.attrGreenExam3ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 2
            300 -> {
                playerl(FaceAnim.FRIENDLY, "Hello there.")
                stage = 301
            }

            301 -> {
                npcl(FaceAnim.FRIENDLY, "How's it going?")
                stage = 302
            }

            302 -> {
                playerl(FaceAnim.FRIENDLY, "I need more help with the exam.")
                stage = 303
            }

            303 -> {
                npcl(FaceAnim.FRIENDLY, "Well, okay, this is what I have learned since I last spoke to you...")
                stage = 304
            }

            304 -> {
                npcl(FaceAnim.FRIENDLY, "Correct rock pick usage: Always handle with care; strike the rock cleanly on its cleaving point.")
                stage = 305
            }

            305 -> {
                playerl(FaceAnim.FRIENDLY, "Okay, I'll remember that.")
                stage = 306
            }

            306 -> {
                setAttribute(player!!, TheDigSite.attrGreenExam2ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - answer already known
            400 -> {
                playerl(FaceAnim.FRIENDLY, "Hello there.")
                stage = 401
            }

            401 -> {
                npcl(FaceAnim.FRIENDLY, "How's it going?")
                stage = 402
            }

            402 -> {
                playerl(FaceAnim.FRIENDLY, "I need more help with the exam.")
                stage = 403
            }

            403 -> {
                npcl(FaceAnim.FRIENDLY, "Well, okay, this is what I have learned since I last spoke to you...")
                stage = 404
            }

            404 -> {
                npcl(FaceAnim.FRIENDLY, "The study of Earth Sciences is: The study of the earth, its contents and history.")
                stage = 405
            }

            405 -> {
                playerl(FaceAnim.FRIENDLY, "Okay, I'll remember that.")
                stage = 406
            }

            406 -> {
                setAttribute(player!!, TheDigSite.attrGreenExam1ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - animal skull
            500 -> {
                if (inInventory(player!!, Items.ANIMAL_SKULL_671)) {
                    playerl(FaceAnim.FRIENDLY, "Hello there. How's the study going?")
                    stage = 511
                } else {
                    playerl(FaceAnim.FRIENDLY, "Hello there. How's the study going?")
                    stage = 502
                }
            }

            502 -> {
                npcl(FaceAnim.FRIENDLY, "Very well, thanks. Have you found my animal skull yet?")
                stage = 503
            }

            503 -> {
                playerl(FaceAnim.FRIENDLY, "No, sorry, not yet.")
                stage = 504
            }

            504 -> {
                npcl(FaceAnim.FRIENDLY, "Oh well, I am sure it's been picked up. Couldn't you try looking through some pockets?")
                stage = 505
            }

            505 -> endFile()

            511 -> {
                npcl(FaceAnim.FRIENDLY, "Very well, thanks. Have you found my animal skull yet?")
                stage = 512
            }

            512 -> {
                if (inInventory(player!!, Items.ANIMAL_SKULL_671)) {
                    removeItem(player!!, Items.ANIMAL_SKULL_671)
                }
                npcl(FaceAnim.FRIENDLY, "Oh wow! You've found it! Thank you so much. I'll be glad to tell you what I know about the exam. The study of Earth Sciences is: The study of the earth, its contents and history.")
                stage = 514
            }

            514 -> {
                setAttribute(player!!, TheDigSite.attrGreenExam1ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - initial conversation
            600 -> {
                playerl(
                    FaceAnim.FRIENDLY,
                    "Hello there. Can you help me with the Earth Sciences exams at all?"
                )
                stage = 601
            }

            601 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Well... Maybe I will if you help me with something."
                )
                stage = 602
            }

            602 -> {
                playerl(FaceAnim.FRIENDLY, "What's that?")
                stage = 603
            }

            603 -> {
                npcl(FaceAnim.FRIENDLY, "I have lost my recent good find.")
                stage = 604
            }

            604 -> {
                playerl(FaceAnim.FRIENDLY, "What does it look like?")
                stage = 605
            }

            605 -> {
                npcl(FaceAnim.FRIENDLY, "Err... Like an animal skull!")
                stage = 606
            }

            606 -> {
                playerl(
                    FaceAnim.FRIENDLY,
                    "Well, that's not too helpful, there are lots of those around here. Can you remember where you last had it?"
                )
                stage = 607
            }

            607 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "It was around here for sure. Maybe one of the workmen picked it up?"
                )
                stage = 608
            }

            608 -> {
                playerl(FaceAnim.FRIENDLY, "Okay, I'll have a look for you.")
                stage = 609
            }

            609 -> {
                setAttribute(player!!, TheDigSite.attrGreenExam1Talked, true)
                endFile()
            }

            // Default conversation
            700 -> {
                playerl(FaceAnim.FRIENDLY, "Hello there.")
                stage = 701
            }

            701 -> {
                npcl(FaceAnim.FRIENDLY, "Oh, hi. I'm studying hard for an exam.")
                stage = 702
            }

            702 -> {
                playerl(FaceAnim.FRIENDLY, "What exam is that?")
                stage = 703
            }

            703 -> {
                npcl(FaceAnim.FRIENDLY, "It's the Earth Sciences exam.")
                stage = 704
            }

            704 -> {
                playerl(FaceAnim.FRIENDLY, "Interesting....")
                stage = 705
            }

            705 -> endFile()
        }
    }
}

@Initializable
class StudentPurpleDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, StudentPurpleDialogueFile(), npc)
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.STUDENT_617)
}

class StudentPurpleDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 5 && getAttribute(player!!, TheDigSite.attrPurpleExam3ObtainAnswer, false) -> 100
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 5 && getAttribute(player!!, TheDigSite.attrPurpleExam3Talked, false) -> 200
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 5 -> 300
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 4 -> 400
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrPurpleExam1ObtainAnswer, false) -> 500
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrPurpleExam1Talked, false) -> 600
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 -> 700
                    else -> 800
                }
            }

            // Earth Sciences Level 3 - answer
            100 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 101 }
            101 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 102 }
            102 -> playerl(FaceAnim.FRIENDLY, "I am stuck on some more exam questions.").also { stage = 103 }
            103 -> npcl(FaceAnim.FRIENDLY, "Okay, I'll tell you my latest notes...").also { stage = 104 }
            104 -> npcl(FaceAnim.FRIENDLY, "Sample preparation: Samples cleaned, and carried only in specimen jars.").also { stage = 105 }
            105 -> playerl(FaceAnim.FRIENDLY, "Great, thanks for your advice.").also { stage = 106 }
            106 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam3ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 3 - opal
            200 -> {
                if (inInventory(player!!, Items.OPAL_1609) ||
                    inInventory(player!!, Items.UNCUT_OPAL_1625)
                ) {
                    playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 211 }
                } else {
                    playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 202 }
                }
            }
            202 -> npcl(FaceAnim.FRIENDLY, "Oh, hi again. Did you bring me the opal?").also { stage = 203 }
            203 -> playerl(FaceAnim.FRIENDLY, "I haven't found one yet.").also { stage = 204 }
            204 -> npcl(FaceAnim.FRIENDLY, "Oh, well, tell me when you do. Remember that they can be found around the site; perhaps try panning the river.").also { stage = 205 }
            205 -> endFile()
            211 -> npcl(FaceAnim.FRIENDLY, "Oh, hi again. Did you bring me the opal?").also { stage = 212 }
            212 -> {
                if (inInventory(player!!, Items.UNCUT_OPAL_1625)) {
                    removeItem(player!!, Items.UNCUT_OPAL_1625)
                } else if (inInventory(player!!, Items.OPAL_1609)) {
                    removeItem(player!!, Items.OPAL_1609)
                }
                playerl(FaceAnim.FRIENDLY, "Would an opal look like this by any chance?").also { stage = 214 }
            }
            214 -> npcl(FaceAnim.FRIENDLY, "Wow, great, you've found one. This will look beautiful set in my necklace. Thanks for that; now I'll tell you what I know... Sample preparation: Samples cleaned, and carried only in specimen jars.").also { stage = 215 }
            215 -> playerl(FaceAnim.FRIENDLY, "Great, thanks for your advice.").also { stage = 216 }
            216 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam3ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 3 - first conversation
            300 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 301 }
            301 -> npcl(FaceAnim.FRIENDLY, "What, you want more help?").also { stage = 302 }
            302 -> playerl(FaceAnim.FRIENDLY, "Err... Yes please!").also { stage = 303 }
            303 -> npcl(FaceAnim.FRIENDLY, "Well... it's going to cost you...").also { stage = 304 }
            304 -> playerl(FaceAnim.FRIENDLY, "Oh, well how much?").also { stage = 305 }
            305 -> npcl(FaceAnim.FRIENDLY, "I'll tell you what I would like: a precious stone. I don't find many of them. My favorite are opals; they are beautiful. Just like me! Tee hee hee!").also { stage = 306 }
            306 -> playerl(FaceAnim.FRIENDLY, "Err... OK I'll see what I can do, but I'm not sure where I'd get one.").also { stage = 307 }
            307 -> npcl(FaceAnim.FRIENDLY, "Well, I have seen people get them from panning occasionally.").also { stage = 308 }
            308 -> playerl(FaceAnim.FRIENDLY, "OK, I'll see what I can turn up for you.").also { stage = 309 }
            309 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam3Talked, true)
                endFile()
            }

            // Earth Sciences Level 2 - answer
            400 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 401 }
            401 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 402 }
            402 -> playerl(FaceAnim.FRIENDLY, "I am stuck on some more exam questions.").also { stage = 403 }
            403 -> npcl(FaceAnim.FRIENDLY, "Okay, I'll tell you my latest notes...").also { stage = 404 }
            404 -> npcl(FaceAnim.FRIENDLY, "Finds handling: Finds must be carefully handled.").also { stage = 405 }
            405 -> playerl(FaceAnim.FRIENDLY, "Great, thanks for your advice.").also { stage = 406 }
            406 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam2ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - answer
            500 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 501 }
            501 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 502 }
            502 -> playerl(FaceAnim.FRIENDLY, "I am stuck on some more exam questions.").also { stage = 503 }
            503 -> npcl(FaceAnim.FRIENDLY, "Okay, I'll tell you my latest notes...").also { stage = 504 }
            504 -> npcl(FaceAnim.FRIENDLY, "The proper health and safety points are: Leather gloves and boots to be warn at all times; proper tools must be used.").also { stage = 505 }
            505 -> playerl(FaceAnim.FRIENDLY, "Great, thanks for your advice.").also { stage = 506 }
            506 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam1ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - teddy bear
            600 -> {
                if (inInventory(player!!, Items.TEDDY_673)) {
                    playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 611 }
                } else {
                    npcl(FaceAnim.FRIENDLY, "Very well thanks. Have you found my lucky mascot yet?").also { stage = 602 }
                }
            }
            602 -> playerl(FaceAnim.FRIENDLY, "No sorry, not yet.").also { stage = 603 }
            603 -> npcl(FaceAnim.FRIENDLY, "I'm sure it's just outside the site somewhere...").also { stage = 604 }
            604 -> endFile()
            611 -> {
                if (inInventory(player!!, Items.TEDDY_673)) {
                    removeItem(player!!, Items.TEDDY_673)
                }
                playerl(FaceAnim.FRIENDLY, "Guess what I found.").also { stage = 613 }
            }
            613 -> npcl(FaceAnim.FRIENDLY, "Hey! My lucky mascot! Thanks ever so much. Let me help you with those questions now.").also { stage = 614 }
            614 -> npcl(FaceAnim.FRIENDLY, "The proper health and safety points are: Leather gloves and boots to be warn at all times; proper tools must be used.").also { stage = 615 }
            615 -> playerl(FaceAnim.FRIENDLY, "Great, thanks for your advice.").also { stage = 616 }
            616 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam1ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 - first conversation
            700 -> playerl(FaceAnim.FRIENDLY, "Hello there. Can you help me with the Earth Sciences exams at all?").also { stage = 701 }
            701 -> npcl(FaceAnim.FRIENDLY, "I can if you help me...").also { stage = 702 }
            702 -> playerl(FaceAnim.FRIENDLY, "How can I do that?").also { stage = 703 }
            703 -> npcl(FaceAnim.FRIENDLY, "I have lost my teddy bear. He was my lucky mascot.").also { stage = 704 }
            704 -> playerl(FaceAnim.FRIENDLY, "Do you know where you dropped him?").also { stage = 705 }
            705 -> npcl(FaceAnim.FRIENDLY, "Well, I was doing a lot of walking that day... Oh yes, that's right - a few of us were studying that funny looking relic in the centre of the campus. Maybe I lost my lucky mascot around there, perhaps in a bush?").also { stage = 706 }
            706 -> playerl(FaceAnim.FRIENDLY, "Leave it to me, I'll find it.").also { stage = 707 }
            707 -> npcl(FaceAnim.FRIENDLY, "Oh, great! Thanks!").also { stage = 708 }
            708 -> {
                setAttribute(player!!, TheDigSite.attrPurpleExam1Talked, true)
                endFile()
            }

            // Default conversation
            800 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 801 }
            801 -> npcl(FaceAnim.FRIENDLY, "Hi there. I'm studying for the Earth Sciences exam.").also { stage = 802 }
            802 -> playerl(FaceAnim.FRIENDLY, "Interesting... This exam seems to be a popular one!").also { stage = 803 }
            803 -> endFile()
        }
    }
}

@Initializable
class StudentBrownDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, StudentBrownDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player?): Dialogue = StudentBrownDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.STUDENT_616)
}

@Initializable
class StudentBrownDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 5 -> 100
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 4 -> 200
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrBrownExam1ObtainAnswer, false) -> 300
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 && getAttribute(player!!, TheDigSite.attrBrownExam1Talked, false) -> 400
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 3 -> 500
                    else -> 600
                }
            }

            // Earth Sciences Level 3 answer
            100 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 101 }
            101 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 102 }
            102 -> playerl(FaceAnim.FRIENDLY, "There are more exam questions I'm stuck on.").also { stage = 103 }
            103 -> npcl(FaceAnim.FRIENDLY, "Hey, I'll tell you what I've learned. That may help.").also { stage = 104 }
            104 -> npcl(FaceAnim.FRIENDLY, "The proper technique for handling bones is: Handle bones carefully and keep them away from other samples.").also { stage = 105 }
            105 -> playerl(FaceAnim.FRIENDLY, "Thanks for the information.").also { stage = 106 }
            106 -> {
                setAttribute(player!!, TheDigSite.attrBrownExam3ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 2 answer
            200 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 201 }
            201 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 202 }
            202 -> playerl(FaceAnim.FRIENDLY, "There are more exam questions I'm stuck on.").also { stage = 203 }
            203 -> npcl(FaceAnim.FRIENDLY, "Hey, I'll tell you what I've learned. That may help.").also { stage = 204 }
            204 -> npcl(FaceAnim.FRIENDLY, "Correct sample transportation: Samples taken in rough form; kept only in sealed containers.").also { stage = 205 }
            205 -> playerl(FaceAnim.FRIENDLY, "Thanks for the information.").also { stage = 206 }
            206 -> {
                setAttribute(player!!, TheDigSite.attrBrownExam2ObtainAnswer, true)
                endFile()
            }

            // Earth Sciences Level 1 answer
            300 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 301 }
            301 -> npcl(FaceAnim.FRIENDLY, "How's it going?").also { stage = 302 }
            302 -> playerl(FaceAnim.FRIENDLY, "There are more exam questions I'm stuck on.").also { stage = 303 }
            303 -> npcl(FaceAnim.FRIENDLY, "Hey, I'll tell you what I've learned. That may help.").also { stage = 304 }
            304 -> npcl(FaceAnim.FRIENDLY, "Correct sample transportation: Samples taken in rough form; kept only in sealed containers.").also { stage = 305 }
            305 -> playerl(FaceAnim.FRIENDLY, "Thanks for the information.").also { stage = 306 }
            306 -> {
                setAttribute(player!!, TheDigSite.attrBrownExam1ObtainAnswer, true)
                endFile()
            }

            // Student's special cup quest branch
            400 -> playerl(FaceAnim.FRIENDLY, "Hello there. How's the study going?").also { stage = 401 }
            401 -> npcl(FaceAnim.FRIENDLY, "I'm getting there. Have you found my special cup yet?").also { stage = 402 }
            402 -> {
                if (inInventory(player!!, Items.SPECIAL_CUP_672)) {
                    removeItem(player!!, Items.SPECIAL_CUP_672)
                    npcl(FaceAnim.FRIENDLY, "Oh wow! You've found it! Thank you so much. I'll be glad to tell you what I know about the exam. The study of Earth Sciences is: The study of the earth, its contents and history.").also { stage = 411 }
                } else {
                    playerl(FaceAnim.FRIENDLY, "No, sorry, not yet.").also { stage = 404 }
                }
            }
            403 -> playerl(FaceAnim.FRIENDLY, "No, sorry, not yet.").also { stage = 404 }
            404 -> npcl(FaceAnim.FRIENDLY, "Oh dear, I hope it didn't fall into the river. I might never find it again.").also { stage = 405 }
            405 -> endFile()
            411 -> {
                setAttribute(player!!, TheDigSite.attrBrownExam1ObtainAnswer, true)
                endFile()
            }

            // Initial conversation about the special cup
            500 -> playerl(FaceAnim.FRIENDLY, "Hello there. Can you help me with the Earth Sciences exams at all?").also { stage = 501 }
            501 -> npcl(FaceAnim.FRIENDLY, "I can't do anything unless I find my special cup.").also { stage = 502 }
            502 -> playerl(FaceAnim.FRIENDLY, "Your what?").also { stage = 503 }
            503 -> npcl(FaceAnim.FRIENDLY, "My special cup. I won it for a particularly good find last month.").also { stage = 504 }
            504 -> playerl(FaceAnim.FRIENDLY, "Oh, right. So if I find it, you'll help me?").also { stage = 505 }
            505 -> npcl(FaceAnim.FRIENDLY, "I sure will!").also { stage = 506 }
            506 -> playerl(FaceAnim.FRIENDLY, "Any ideas where it may be?").also { stage = 507 }
            507 -> npcl(FaceAnim.FRIENDLY, "All I remember is that I was working near the panning area when I lost it.").also { stage = 508 }
            508 -> playerl(FaceAnim.FRIENDLY, "Okay, I'll see what I can do.").also { stage = 509 }
            509 -> npcl(FaceAnim.FRIENDLY, "Yeah, maybe the panning guide saw it? I hope I didn't lose it in the water!").also { stage = 510 }
            510 -> {
                setAttribute(player!!, TheDigSite.attrBrownExam1Talked, true)
                endFile()
            }

            // Default conversation
            600 -> playerl(FaceAnim.FRIENDLY, "Hello there.").also { stage = 601 }
            601 -> npcl(FaceAnim.FRIENDLY, "Hello there. As you can see, I am a student.").also { stage = 602 }
            602 -> playerl(FaceAnim.FRIENDLY, "What are you doing here?").also { stage = 603 }
            603 -> npcl(FaceAnim.FRIENDLY, "I'm studying for the Earth Sciences exam.").also { stage = 604 }
            604 -> playerl(FaceAnim.FRIENDLY, "Interesting... Perhaps I should study for it as well.").also { stage = 605 }
            605 -> endFile()
        }
    }
}