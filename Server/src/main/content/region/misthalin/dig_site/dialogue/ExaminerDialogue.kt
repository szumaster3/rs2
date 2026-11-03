package content.region.misthalin.dig_site.dialogue

import content.region.misthalin.dig_site.quest.itexam.TheDigSite
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.game.world.GameWorld
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Examiner dialogue.
 */
@Initializable
class ExaminerDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, ExaminerDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player?): Dialogue = ExaminerDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.EXAMINER_618, NPCs.EXAMINER_4566, NPCs.EXAMINER_4567)
}

class ExaminerDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when (getQuestStage(player!!, Quests.THE_DIG_SITE)) {
                    100 -> 900
                    in 6..12 -> 10
                    5 -> 100
                    4 -> 200
                    in 1..3 -> 300
                    else -> 600
                }
            }

            // Completed The Dig Site.
            900 -> {
                npcl(
                    FaceAnim.HAPPY,
                    "Hello there! My colleague tells me you helped to uncover a hidden altar to the god Zaros."
                )
                stage = 901
            }

            901 -> {
                npcl(
                    FaceAnim.HAPPY,
                    "A great scholar and archaeologist indeed! Good health and prosperity to you."
                )
                stage = 902
            }

            902 -> {
                options("Thanks.", "I have lost my trowel.")
                stage = 903
            }

            903 -> when (buttonID) {
                1 -> {
                    playerl(FaceAnim.HAPPY, "Thanks!")
                    endFile()
                }

                2 -> {
                    playerl("I have lost my trowel.")
                    stage = 904
                }
            }

            904 -> {
                npcl("Deary me... That was a good one as well. It's a good job I have another. Here you go...")
                stage = 905
            }

            905 -> {
                addItemOrDrop(player!!, Items.TROWEL_676)
                endFile()
            }

            // Trowel recovery during later quest stages.
            10 -> {
                stage = if (inInventory(player!!, Items.TROWEL_676)) 11 else 12
            }

            11 -> {
                npcl(FaceAnim.ANNOYED, "Well, what are you doing here? Get digging!")
                endFile()
            }

            12 -> {
                playerl(FaceAnim.HALF_GUILTY, "I have lost my trowel.")
                stage = 13
            }

            13 -> {
                npcl(
                    FaceAnim.THINKING,
                    "Deary me. That was a good one as well. It's a good job I have another. Here you go..."
                )
                stage = 14
            }

            14 -> {
                addItemOrDrop(player!!, Items.TROWEL_676)
                endFile()
            }

            // Earth Sciences level 3 exam.
            100 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 101
            }

            101 -> {
                npcl(FaceAnim.FRIENDLY, "Ah, hello again.")
                stage = 102
            }

            102 -> {
                options(
                    "I am ready for the last exam...",
                    "I am stuck on a question.",
                    "Sorry, I didn't mean to disturb you.",
                    "I have lost my trowel."
                )
                stage = 103
            }

            103 -> when (buttonID) {
                1 -> {
                    playerl("I am ready for the last exam...")
                    stage = 110
                }

                2 -> {
                    playerl("I am stuck on a question.")
                    stage = 104
                }

                3 -> {
                    playerl("Sorry, I didn't mean to disturb you.")
                    stage = 106
                }

                4 -> {
                    playerl("I have lost my trowel.")
                    stage = 107
                }
            }

            104 -> {
                npcl(
                    "Well, well, have you not been doing any studies? I am not giving you the answers, talk to the other students and remember the answers."
                )
                endFile()
            }

            106 -> {
                npcl("Oh, no problem at all.")
                endFile()
            }

            107 -> {
                stage = if (inInventory(player!!, Items.TROWEL_676)) 108 else 109
            }

            108 -> {
                npcl("Really? Look in your backpack and make sure first.")
                endFile()
            }

            109 -> {
                npcl("Deary me. That was a good one as well. It's a good job I have another. Here you go...")
                stage = 1091
            }

            1091 -> {
                addItemOrDrop(player!!, Items.TROWEL_676)
                endFile()
            }

            110 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Attention, this is the final part of the Earth Sciences exam: Earth Sciences level 3 - Advanced."
                )
                stage = 111
            }

            111 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Question 1 - Sample preparation. Can you tell me how we prepare samples?"
                )
                stage = 112
            }

            112 -> {
                options(
                    "Samples may be mixed together safely.",
                    "Samples cleaned, and carried only in specimen jars.",
                    "Sample types catalogued and carried by hand only.",
                    "Samples to be spread thickly with mashed banana."
                )
                stage = 113
            }

            113 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 0)
                    stage = 114
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 1)
                    stage = 114
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 2)
                    stage = 114
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 3)
                    stage = 114
                }
            }

            114 -> {
                npcl("Okay, next question...")
                stage = 115
            }

            115 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 3, question 2 - Specimen brush use. What is the proper way to use a specimen brush?"
                )
                stage = 116
            }

            116 -> {
                options(
                    "Brush quickly using a wet brush.",
                    "Brush carefully and slowly using short strokes.",
                    "Dipped in glue and stuck to a sheep's back.",
                    "Brush quickly and with force."
                )
                stage = 117
            }

            117 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 0)
                    stage = 118
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 1)
                    stage = 118
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 2)
                    stage = 118
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 3)
                    stage = 118
                }
            }

            118 -> {
                npcl("Okay, next question...")
                stage = 119
            }

            119 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 3, question 3 - Advanced techniques. Can you describe the technique for handling bones?"
                )
                stage = 120
            }

            120 -> {
                options(
                    "Bones must not be taken from the site.",
                    "Feed to hungry dogs.",
                    "Bones to be ground and tested for mineral content.",
                    "Handle bones very carefully and keep them away from other samples."
                )
                stage = 121
            }

            121 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 0)
                    stage = 122
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 1)
                    stage = 122
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 2)
                    stage = 122
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 3)
                    stage = 122
                }
            }

            122 -> {
                npcl("Okay, that concludes the level 3 Earth Sciences exam.")
                stage = 123
            }

            123 -> {
                npcl(FaceAnim.HAPPY, "Let me add up the results...")
                stage = 124
            }

            124 -> {
                var ansCount = 0

                if (
                    getAttribute(player!!, TheDigSite.attributeFirstQuestion, -1) == 0 &&
                    getAttribute(player!!, TheDigSite.attrPurpleExam3ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeSecondQuestion, -1) == 0 &&
                    getAttribute(player!!, TheDigSite.attrGreenExam3ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeThirdQuestion, -1) == 3 &&
                    getAttribute(player!!, TheDigSite.attrBrownExam3ObtainAnswer, false)
                ) {
                    ansCount++
                }

                stage = when (ansCount) {
                    0 -> 130
                    1 -> 140
                    2 -> 150
                    else -> 160
                }
            }

            130 -> {
                npcl(
                    FaceAnim.ANGRY,
                    "I cannot believe this! Absolutely none right at all. I doubt you did any research before you took this exam..."
                )
                stage = 131
            }

            131 -> {
                playerl(FaceAnim.SAD, "Ah... Yes... Erm.... I think I had better go and revise first!")
                endFile()
            }

            140 -> {
                npcl(FaceAnim.FRIENDLY, "You got one question correct. Try harder!")
                stage = 141
            }

            141 -> {
                playerl(FaceAnim.FRIENDLY, "Oh bother!")
                endFile()
            }

            150 -> {
                npcl(FaceAnim.FRIENDLY, "You got two questions correct. A little more study and you will pass it.")
                stage = 151
            }

            151 -> {
                playerl(FaceAnim.FRIENDLY, "I'm nearly there...")
                endFile()
            }

            160 -> {
                npcl(FaceAnim.FRIENDLY, "You got all the questions correct, well done!")
                stage = 161
            }

            161 -> {
                playerl(FaceAnim.FRIENDLY, "Hooray!")
                stage = 162
            }

            162 -> {
                addItemOrDrop(player!!, Items.LEVEL_3_CERTIFICATE_693)
                stage = 163
            }

            163 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Congratulations! You have now passed the Earth Sciences level 3 exam. Here is your level 3 certificate."
                )
                stage = 164
            }

            164 -> {
                playerl(FaceAnim.FRIENDLY, "I can dig wherever I want now!")
                stage = 165
            }

            165 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Perhaps you should use your newfound skills to find an artefact on the digsite that will impress the archaeological expert."
                )
                stage = 166
            }

            166 -> {
                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 5) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 6)
                }
                openInterface(player!!, 444)
                sendString(player!!, player!!.username, 444, 5)
                endFile()
            }

            // Earth Sciences level 2 exam.
            200 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 201
            }

            201 -> {
                npcl(FaceAnim.FRIENDLY, "Hello again.")
                stage = 202
            }

            202 -> {
                options(
                    "I am ready for the next exam.",
                    "I am stuck on a question.",
                    "Sorry, I didn't mean to disturb you.",
                    "I have lost my trowel."
                )
                stage = 203
            }

            203 -> when (buttonID) {
                1 -> {
                    playerl("I am ready for the next exam.")
                    stage = 210
                }

                2 -> {
                    playerl("I am stuck on a question.")
                    stage = 204
                }

                3 -> {
                    playerl("Sorry, I didn't mean to disturb you.")
                    stage = 206
                }

                4 -> {
                    playerl("I have lost my trowel.")
                    stage = 207
                }
            }

            204 -> {
                npcl(
                    "Well, well, have you not been doing any studies? I am not giving you the answers, talk to the other students and remember the answers."
                )
                endFile()
            }

            206 -> {
                npcl("Oh, no problem at all.")
                endFile()
            }

            207 -> {
                stage = if (inInventory(player!!, Items.TROWEL_676)) 208 else 209
            }

            208 -> {
                npcl("Really? Look in your backpack and make sure first.")
                endFile()
            }

            209 -> {
                npcl("Deary me. That was a good one as well. It's a good job I have another. Here you go...")
                stage = 2091
            }

            2091 -> {
                addItemOrDrop(player!!, Items.TROWEL_676)
                endFile()
            }

            210 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Okay, this is the next part of the Earth Sciences exam: Earth Sciences level 2 - Intermediate."
                )
                stage = 211
            }

            211 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Question 1 - Sample transportation. Can you tell me how we transport samples?"
                )
                stage = 212
            }

            212 -> {
                options(
                    "Samples cut and cleaned before transportation.",
                    "Samples ground and suspended in an acid solution.",
                    "Samples to be given to the melon-collecting monkey. ",
                    "Samples taken in rough form; kept only in sealed containers."
                )
                stage = 213
            }

            213 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 0)
                    stage = 214
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 1)
                    stage = 214
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 2)
                    stage = 214
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 3)
                    stage = 214
                }
            }

            214 -> {
                npcl("Okay, next question...")
                stage = 215
            }

            215 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 2, question 2 - Handling of finds. What is the proper way to handle finds?"
                )
                stage = 216
            }

            216 -> {
                options(
                    "Finds must not be handled by anyone.",
                    "Finds must be carefully handled.",
                    "Finds to be given to the site workmen.",
                    "Drop them on the floor and jump on them."
                )
                stage = 217
            }

            217 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 0)
                    stage = 218
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 1)
                    stage = 218
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 2)
                    stage = 218
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 3)
                    stage = 218
                }
            }

            218 -> {
                npcl("Okay, next question...")
                stage = 219
            }

            219 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 2, question 3 - Rock pick usage. Can you tell me the proper use for a rock pick?"
                )
                stage = 220
            }

            220 -> {
                options(
                    "Strike rock repeatedly until powdered.",
                    "Rock pick must be used flat and with strong force.",
                    "Always handle with care; strike cleanly on its cleaving point.",
                    "Protective clothing to be worn; tools kept away from site."
                )
                stage = 221
            }

            221 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 0)
                    stage = 222
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 1)
                    stage = 222
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 2)
                    stage = 222
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 3)
                    stage = 222
                }
            }

            222 -> {
                npcl("Okay, that covers the level 2 Earth Sciences exam.")
                stage = 223
            }

            223 -> {
                npcl(FaceAnim.HAPPY, "Let me add up your total...")
                stage = 224
            }

            224 -> {
                var ansCount = 0

                if (
                    getAttribute(player!!, TheDigSite.attributeFirstQuestion, -1) == 3 &&
                    getAttribute(player!!, TheDigSite.attrBrownExam2ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeSecondQuestion, -1) == 0 &&
                    getAttribute(player!!, TheDigSite.attrPurpleExam2ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeThirdQuestion, -1) == 2 &&
                    getAttribute(player!!, TheDigSite.attrGreenExam2ObtainAnswer, false)
                ) {
                    ansCount++
                }

                stage = when (ansCount) {
                    0 -> 230
                    1 -> 240
                    2 -> 250
                    else -> 260
                }
            }

            230 -> {
                npcl(FaceAnim.ANGRY, "No, no, no! This will not do. They are all wrong. Start again!")
                stage = 231
            }

            231 -> {
                playerl(FaceAnim.SAD, "Oh no!")
                stage = 232
            }

            232 -> {
                npcl("More studying for you my @g[boy,girl].")
                endFile()
            }

            240 -> {
                npcl(FaceAnim.FRIENDLY, "You got one question correct. At least it's a start.")
                stage = 241
            }

            241 -> {
                playerl(FaceAnim.FRIENDLY, "Oh well..")
                stage = 242
            }

            242 -> {
                npcl(FaceAnim.FRIENDLY, "Get out and explore the site, talk to people and learn!")
                endFile()
            }

            250 -> {
                npcl(FaceAnim.FRIENDLY, "You got two questions correct. Not too bad, but you can do better...")
                stage = 251
            }

            251 -> {
                playerl(FaceAnim.FRIENDLY, "Nearly got it.")
                endFile()
            }

            260 -> {
                npcl(FaceAnim.FRIENDLY, "You got all the questions correct, well done!")
                stage = 261
            }

            261 -> {
                playerl(FaceAnim.FRIENDLY, "Great, I'm getting good at this.")
                stage = 262
            }

            262 -> {
                addItemOrDrop(player!!, Items.LEVEL_2_CERTIFICATE_692)
                stage = 263
            }

            263 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You have now passed the Earth Sciences level 2 intermediate exam. Here is your certificate. Of course, you'll want to get studying for your next exam now!"
                )
                stage = 264
            }

            264 -> {
                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 4) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 5)
                }
                openInterface(player!!, 441)
                sendString(player!!, player!!.username, 441, 5)
                endFile()
            }

            // Initial letter and level 1 exam.
            300 -> {
                stage = when {
                    getQuestStage(player!!, Quests.THE_DIG_SITE) == 2 &&
                            !inInventory(player!!, Items.SEALED_LETTER_683) -> 301

                    else -> getQuestStage(player!!, Quests.THE_DIG_SITE)
                }
            }

            301 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 302
            }

            302 -> {
                npcl(FaceAnim.FRIENDLY, "Hello again.")
                stage = 303
            }

            303 -> {
                npcl(FaceAnim.FRIENDLY, "I am still waiting for your letter of recommendation.")
                stage = 304
            }

            304 -> {
                options(
                    "I have lost the letter you gave me.",
                    "Alright I'll try and get it."
                )
                stage = 305
            }

            305 -> when (buttonID) {
                1 -> {
                    playerl("I have lost the letter you gave me.")
                    stage = 306
                }

                2 -> {
                    playerl("Alright I'll try and get it.")
                    stage = 311
                }
            }

            306 -> {
                stage = when {
                    inInventory(player!!, Items.UNSTAMPED_LETTER_682) -> 307
                    inBank(player!!, Items.UNSTAMPED_LETTER_682) -> 308
                    else -> 309
                }
            }

            307 -> {
                npcl("Oh now come on. You have it with you!")
                endFile()
            }

            308 -> {
                npcl("You already have the letter in your bank.")
                endFile()
            }

            309 -> {
                npcl("That was foolish. Take this one and don't lose it!")
                stage = 310
            }

            310 -> {
                addItemOrDrop(player!!, Items.UNSTAMPED_LETTER_682)
                endFile()
            }

            311 -> {
                npcl("I am sure you won't get any problems. Speak to the Curator of Varrock's museum.")
                endFile()
            }

            // Stage 2: hand in the stamped letter.
            2 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 320
            }

            320 -> {
                npcl(FaceAnim.FRIENDLY, "Hello again.")
                stage = 321
            }

            321 -> {
                playerl(FaceAnim.FRIENDLY, "Here is the stamped letter you asked for.")
                stage = 322
            }

            322 -> {
                if (inInventory(player!!, Items.SEALED_LETTER_683)) {
                    removeItem(player!!, Items.SEALED_LETTER_683)
                }

                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 2) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 3)
                }
                stage = 323
            }

            323 -> {
                npcl(FaceAnim.NEUTRAL, "Good, good. We will begin the exam...")
                stage = 500
            }

            // Stage 3: retry the level 1 exam.
            3 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 330
            }

            330 -> {
                npcl(FaceAnim.FRIENDLY, "Hello again. Are you ready for another shot at the exam?")
                stage = 331
            }

            331 -> {
                options("Yes, I certainly am.", "No, not at the moment.")
                stage = 332
            }

            332 -> when (buttonID) {
                1 -> {
                    playerl("Yes, I certainly am.")
                    stage = 500
                }

                2 -> {
                    playerl("No, not at the moment.")
                    stage = 333
                }
            }

            333 -> {
                npcl(FaceAnim.NEUTRAL, "Okay, take your time if you wish.")
                endFile()
            }

            // Earth Sciences level 1 exam.
            500 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Okay, we will start with the first exam: Earth Sciences level 1 - Beginner."
                )
                stage = 501
            }

            501 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Question 1 - Earth Sciences overview. Can you tell me what Earth Sciences is?"
                )
                stage = 502
            }

            502 -> {
                options(
                    "The study of gardening, planing and fruiting vegetation.",
                    "The study of the earth, its contents and history.",
                    "The study of planets and the history of worlds.",
                    "The combination of archaeology and vegetarianism."
                )
                stage = 503
            }

            503 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 0)
                    stage = 504
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 1)
                    stage = 504
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 2)
                    stage = 504
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeFirstQuestion, 3)
                    stage = 504
                }
            }

            504 -> {
                npcl("Okay, next question...")
                stage = 505
            }

            505 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 1, question 2 - Eligibility. Can you tell me which people are allowed to use the digsite?"
                )
                stage = 506
            }

            506 -> {
                options(
                    "Magic users, miners and their escorts.",
                    "Professors, students and workmen only.",
                    "Local residents, contractors and small pink fish.",
                    "All that have passed the appropriate Earth Sciences exam."
                )
                stage = 507
            }

            507 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 0)
                    stage = 508
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 1)
                    stage = 508
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 2)
                    stage = 508
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeSecondQuestion, 3)
                    stage = 508
                }
            }

            508 -> {
                npcl("Okay, next question...")
                stage = 509
            }

            509 -> {
                npcl(
                    FaceAnim.NEUTRAL,
                    "Earth Sciences level 1, question 3 - Health and safety. Can you tell me the proper safety points when working on a digsite?"
                )
                stage = 510
            }

            510 -> {
                options(
                    "Heat-resistant clothing to be worn at all times.",
                    "Rubber chickens to be worn on the head at all times.",
                    "Gloves and boots to be warn at all times; proper tools must be used.",
                    "Protective clothing to be worn; tools kept away from site."
                )
                stage = 511
            }

            511 -> when (buttonID) {
                1 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 0)
                    stage = 512
                }

                2 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 1)
                    stage = 512
                }

                3 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 2)
                    stage = 512
                }

                4 -> {
                    setAttribute(player!!, TheDigSite.attributeThirdQuestion, 3)
                    stage = 512
                }
            }

            512 -> {
                npcl("Okay, that covers the level 1 Earth Sciences exam.")
                stage = 513
            }

            513 -> {
                npcl(FaceAnim.HAPPY, "Let's see how you did...")
                stage = 514
            }

            514 -> {
                var ansCount = 0

                if (
                    getAttribute(player!!, TheDigSite.attributeFirstQuestion, -1) == 1 &&
                    getAttribute(player!!, TheDigSite.attrGreenExam1ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeSecondQuestion, -1) == 3 &&
                    getAttribute(player!!, TheDigSite.attrBrownExam1ObtainAnswer, false)
                ) {
                    ansCount++
                }

                if (
                    getAttribute(player!!, TheDigSite.attributeThirdQuestion, -1) == 2 &&
                    getAttribute(player!!, TheDigSite.attrPurpleExam1ObtainAnswer, false)
                ) {
                    ansCount++
                }

                stage = when (ansCount) {
                    0 -> 520
                    1 -> 530
                    2 -> 540
                    else -> 550
                }
            }

            520 -> {
                npcl(
                    FaceAnim.ANGRY,
                    "Oh dear me! This is appalling, none correct at all! I suggest you go and study properly."
                )
                stage = 521
            }

            521 -> {
                playerl(FaceAnim.SAD, "Oh dear...")
                stage = 522
            }

            522 -> {
                npcl(
                    "Why don't you use the resources here? There are books and the researchers... and you could even ask other students who are also studying for these exams."
                )
                endFile()
            }

            530 -> {
                npcl(FaceAnim.FRIENDLY, "You got one question correct. Better luck next time.")
                stage = 531
            }

            531 -> {
                playerl(FaceAnim.FRIENDLY, "Oh bother!")
                stage = 532
            }

            532 -> {
                npcl(FaceAnim.FRIENDLY, "Do some more research. I'm sure other students could help you out.")
                endFile()
            }

            540 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You got two questions correct. Not bad, just a little more revision needed."
                )
                stage = 541
            }

            541 -> {
                playerl(FaceAnim.FRIENDLY, "Oh well...")
                endFile()
            }

            550 -> {
                npcl(FaceAnim.FRIENDLY, "You got all the questions correct. Well done!")
                stage = 551
            }

            551 -> {
                playerl(FaceAnim.FRIENDLY, "Hey! Excellent!")
                stage = 552
            }

            552 -> {
                addItemOrDrop(player!!, Items.TROWEL_676)
                addItemOrDrop(player!!, Items.LEVEL_1_CERTIFICATE_691)
                stage = 553
            }

            553 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You have now passed the Earth Sciences level 1 general exam. Here is your certificate to prove it. You also get a decent trowel to dig with. Of course, you'll want to get studying for your next exam now!"
                )
                stage = 554
            }

            554 -> {
                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 3) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 4)
                }
                openInterface(player!!, 440)
                sendString(player!!, player!!.username, 440, 5)
                endFile()
            }

            // Initial conversation with the examiner.
            600 -> {
                playerl(FaceAnim.FRIENDLY, "Hello.")
                stage = 601
            }

            601 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Ah hello there! I am the resident lecturer on antiquities and artefacts. I also set the Earth Sciences exams."
                )
                stage = 602
            }

            602 -> {
                playerl(FaceAnim.FRIENDLY, "Earth Sciences?")
                stage = 603
            }

            603 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "That is right dear, the world of ${GameWorld.settings!!.name} holds many wonders beneath its surface. Students come to me to take exams so that they may join in on the archaeological dig going on just north of here."
                )
                stage = 604
            }

            604 -> {
                playerl(FaceAnim.FRIENDLY, "So if they don't pass the exams they can't dig at all?")
                stage = 605
            }

            605 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "That's right! We have to make sure that students know enough to be able to dig safely and not damage the artefacts."
                )
                stage = 606
            }

            606 -> {
                options("Can I take an exam?", "Interesting...")
                stage = 607
            }

            607 -> when (buttonID) {
                1 -> {
                    playerl("Can I take an exam?")
                    stage = 608
                }

                2 -> {
                    playerl("Interesting...")
                    stage = 620
                }
            }

            608 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You can if you get this letter stamped by the Curator of Varrock's museum."
                )
                stage = 609
            }

            609 -> {
                addItemOrDrop(player!!, Items.UNSTAMPED_LETTER_682)
                stage = 610
            }

            610 -> {
                playerl(FaceAnim.FRIENDLY, "Why's that then?")
                stage = 611
            }

            611 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Because he is a very knowledgeable man and employs our archaeological expert. I'm sure he knows a lot about your exploits and can judge whether you'd make a good archaeologist or not."
                )
                stage = 612
            }

            612 -> {
                npcl(FaceAnim.FRIENDLY, "Besides, the museum contributes funds to the dig.")
                stage = 613
            }

            613 -> {
                playerl(FaceAnim.FRIENDLY, "But why are you writing the letter? Shouldn't he?")
                stage = 614
            }

            614 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "He's also a very busy man, so I write the letters and he justs stamps them if he approves."
                )
                stage = 615
            }

            615 -> {
                playerl(
                    FaceAnim.FRIENDLY,
                    "Oh, I see. I'll ask him if he'll approve me, and bring my stamped letter back here. Thanks."
                )
                stage = 616
            }

            616 -> {
                if (getQuestStage(player!!, Quests.THE_DIG_SITE) == 0) {
                    setQuestStage(player!!, Quests.THE_DIG_SITE, 1)
                }
                endFile()
            }

            620 -> {
                npcl(FaceAnim.FRIENDLY, "You could gain much with an understanding of the world below.")
                endFile()
            }
        }
    }
}