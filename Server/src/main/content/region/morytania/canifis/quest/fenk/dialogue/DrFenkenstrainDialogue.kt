package content.region.morytania.canifis.quest.fenk.dialogue

import content.region.morytania.canifis.quest.fenk.CreatureOfFenkenstrain
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class DrFenkenstrainDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, DrFenkenstrainDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player?): Dialogue = DrFenkenstrainDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.WEREWOLF_1668, NPCs.WEREWOLF_1669, NPCs.DR_FENKENSTRAIN_1670)
}

class DrFenkenstrainDialogueFile : DialogueFile() {

    companion object {
        private fun allPartsSubmitted(player: Player): Boolean =
            getAttribute(player, CreatureOfFenkenstrain.attributeArms, false) &&
                    getAttribute(player, CreatureOfFenkenstrain.attributeLegs, false) &&
                    getAttribute(player, CreatureOfFenkenstrain.attributeTorso, false) &&
                    getAttribute(player, CreatureOfFenkenstrain.attributeHead, false)

        private fun reqArms(player: Player): Boolean =
            !getAttribute(player, CreatureOfFenkenstrain.attributeArms, false) &&
                    inInventory(player, Items.ARMS_4195)

        private fun reqLegs(player: Player): Boolean =
            !getAttribute(player, CreatureOfFenkenstrain.attributeLegs, false) &&
                    inInventory(player, Items.LEGS_4196)

        private fun reqTorso(player: Player): Boolean =
            !getAttribute(player, CreatureOfFenkenstrain.attributeTorso, false) &&
                    inInventory(player, Items.TORSO_4194)

        private fun reqHead(player: Player): Boolean =
            !getAttribute(player, CreatureOfFenkenstrain.attributeHead, false) &&
                    inInventory(player, Items.DECAPITATED_HEAD_4198)

        private fun hasAnyPart(player: Player): Boolean =
            allPartsSubmitted(player) ||
                    reqArms(player) ||
                    reqLegs(player) ||
                    reqTorso(player) ||
                    reqHead(player)

        private fun submitPart(
            player: Player,
            item: Item,
            attribute: String
        ): Boolean {
            if (getAttribute(player, attribute, false) ||
                !inInventory(player, item.id, item.amount)
            ) {
                return false
            }

            setAttribute(player, attribute, true)
            removeItem(player, item)
            return true
        }
    }

    override fun handle(componentID: Int, buttonID: Int) {
        val p = player ?: return

        when (getQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN)) {

            // Quest stage 0
            0 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Have you come to apply for the job?")
                        stage = 1
                    }

                    1 -> {
                        playerl(FaceAnim.THINKING, "What job?")
                        stage = 2
                    }

                    2 -> {
                        npcl("I've posted a note on the signpost in Canifis about it. Go take a look at it first.")
                        endFile()
                    }
                }
            }

            // Quest stage 1
            1 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Have you come to apply for the job?")
                        stage = 10
                    }

                    10 -> {
                        options("Yes", "No.")
                        stage = 11
                    }

                    11 -> {
                        when (buttonID) {
                            1 -> {
                                playerl("Yes, if it pays well.")
                                stage = 12
                            }

                            2 -> {
                                playerl("No.")
                                endFile()
                            }
                        }
                    }

                    12 -> {
                        npcl("I'll have to ask you some questions first.")
                        stage = 13
                    }

                    13 -> {
                        playerl("Okay...")
                        stage = 14
                    }

                    14 -> {
                        npcl("How would you describe yourself in one word?")
                        stage = 15
                    }

                    15 -> {
                        options(
                            "Stunning.",
                            "Awe-inspiring.",
                            "Breathtaking.",
                            "Braindead."
                        )
                        stage = 16
                    }

                    16 -> {
                        if (buttonID in 1..4) {
                            setAttribute(
                                p,
                                "creature-of-fenkenstrain:first-question",
                                buttonID - 1
                            )
                            playerl(
                                arrayOf(
                                    "Stunning.",
                                    "Awe-inspiring.",
                                    "Breathtaking.",
                                    "Braindead."
                                )[buttonID - 1]
                            )
                            stage = 17
                        }
                    }

                    17 -> {
                        npcl("Mmmm, I see.")
                        stage = 18
                    }

                    18 -> {
                        npcl("Just one more question. What would you say is your greatest skill?")
                        stage = 19
                    }

                    19 -> {
                        options(
                            "Combat.",
                            "Magic.",
                            "Cooking.",
                            "Grave-digging."
                        )
                        stage = 20
                    }

                    20 -> {
                        if (buttonID in 1..4) {
                            setAttribute(
                                p,
                                "creature-of-fenkenstrain:second-question",
                                buttonID - 1
                            )
                            playerl(
                                arrayOf(
                                    "Combat.",
                                    "Magic.",
                                    "Cooking.",
                                    "Grave-digging."
                                )[buttonID - 1]
                            )
                            stage = 21
                        }
                    }

                    21 -> {
                        npcl("Mmmm, I see.")
                        stage = 22
                    }

                    22 -> {
                        val correctAnswers =
                            getAttribute(
                                p,
                                "creature-of-fenkenstrain:first-question",
                                -1
                            ) == 3 &&
                                    getAttribute(
                                        p,
                                        "creature-of-fenkenstrain:second-question",
                                        -1
                                    ) == 3

                        if (!correctAnswers) {
                            npcl("Looks like you're not the @g[man,woman] for the job.")
                            endFile()
                        } else {
                            npcl("Looks like you're just the @g[man,woman] for the job! Welcome aboard!")
                            stage = 23
                        }
                    }

                    23 -> {
                        playerl("Is there anything you'd like me to do for you, sir?")
                        stage = 24
                    }

                    24 -> {
                        npcl("Yes, there is. You're highly skilled at grave-digging, yes?")
                        stage = 25
                    }

                    25 -> {
                        playerl("Err...yes, that's what I said.")
                        stage = 26
                    }

                    26 -> {
                        npcl("Excellent. Now listen carefully. I need you to find some...stuff...for me.")
                        stage = 27
                    }

                    27 -> {
                        playerl("Stuff?")
                        stage = 28
                    }

                    28 -> {
                        npcl("That's what I said...stuff.")
                        stage = 29
                    }

                    29 -> {
                        playerl("What kind of stuff?")
                        stage = 30
                    }

                    30 -> {
                        npcl("Well...dead stuff.")
                        stage = 31
                    }

                    31 -> {
                        playerl("Go on...")
                        stage = 32
                    }

                    32 -> {
                        npcl(
                            "I need you to get me enough dead body parts for me to stitch together a complete body, which I plan to bring to life."
                        )
                        stage = 33
                    }

                    33 -> {
                        playerl("Right...okay...if you insist.")
                        stage = 34
                    }

                    34 -> {
                        if (getQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN) == 1) {
                            setQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN, 2)
                        }
                        endFile()
                    }
                }
            }

            // Quest stage 2
            2 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        showPartsOptions(p)
                    }

                    200 -> {
                        val hasParts = hasAnyPart(p)
                        val optionsList = mutableListOf<String>()

                        if (hasParts) {
                            optionsList.add("I have some body parts for you.")
                        }

                        if (!hasParts) {
                            optionsList.add("Do you know where I could find body parts?")
                        }

                        optionsList.add("Remind me what you want me to do.")
                        optionsList.add("Why are you trying to make this creature?")
                        optionsList.add("Will this creature put me out of a job?")
                        optionsList.add("I must get back to work, sir.")

                        options(*optionsList.toTypedArray())
                        stage = 201
                    }

                    201 -> {
                        val hasParts = hasAnyPart(p)

                        when (buttonID) {
                            1 -> {
                                if (hasParts) {
                                    playerl("I have some body parts for you.")
                                    stage = 202
                                } else {
                                    playerl("Do you know where I could find body parts?")
                                    stage = 210
                                }
                            }

                            2 -> {
                                playerl("Remind me what you want me to do.")
                                stage = 220
                            }

                            3 -> {
                                playerl("Why are you trying to make this creature?")
                                stage = 230
                            }

                            4 -> {
                                playerl("Will this creature put me out of a job?")
                                stage = 240
                            }

                            5 -> {
                                playerl("I must get back to work, sir.")
                                endFile()
                            }
                        }
                    }

                    // Submit body parts
                    202 -> {
                        if (submitPart(p, Item(Items.ARMS_4195, 1), CreatureOfFenkenstrain.attributeArms)
                        ) {
                            npcl("Great, you've brought me some arms.")
                            stage = 203
                        } else {
                            stage = 203
                            handle(componentID, buttonID)
                        }
                    }

                    203 -> {
                        if (submitPart(
                                p,
                                Item(Items.LEGS_4196, 1),
                                CreatureOfFenkenstrain.attributeLegs
                            )
                        ) {
                            npcl("Excellent, you've brought me some legs.")
                            stage = 204
                        } else {
                            stage = 204
                            handle(componentID, buttonID)
                        }
                    }

                    204 -> {
                        if (submitPart(
                                p,
                                Item(Items.TORSO_4194, 1),
                                CreatureOfFenkenstrain.attributeTorso
                            )
                        ) {
                            npcl("Splendid, you've brought me a torso.")
                            stage = 205
                        } else {
                            stage = 205
                            handle(componentID, buttonID)
                        }
                    }

                    205 -> {
                        if (submitPart(
                                p,
                                Item(Items.DECAPITATED_HEAD_4198, 1),
                                CreatureOfFenkenstrain.attributeHead
                            )
                        ) {
                            npcl("Fantastic, you've brought me a head.")
                            stage = 206
                        } else {
                            stage = 206
                            handle(componentID, buttonID)
                        }
                    }

                    206 -> {
                        if (!allPartsSubmitted(p)) {
                            endFile()
                        } else {
                            npcl("Superb! Those are all the parts I need. Now to sew them together...")
                            stage = 207
                        }
                    }

                    207 -> {
                        npcl("Oh bother! I haven't got a needle or thread!")
                        stage = 208
                    }

                    208 -> {
                        npcl("Go and get me a needle, and I'll need 5 lots of thread.")
                        stage = 209
                    }

                    209 -> {
                        if (getQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN) == 2) {
                            setQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN, 3)
                        }
                        endFile()
                    }

                    // Where to find body parts
                    210 -> {
                        npcl(
                            "The soil of Morytania is unique in its ability to preserve the bodies of the dead, which is one reason why I have chosen to carry out my experiments here."
                        )
                        stage = 211
                    }

                    211 -> {
                        npcl(
                            "I recommend digging up some graves in the local area. To the south-east you will find the Haunted Woods; I believe there are many graves there."
                        )
                        stage = 212
                    }

                    212 -> {
                        npcl(
                            "There is also a mausoleum on an island west of this castle. I expect the bodies that are buried there to be extremely well preserved, as they were wealthy in life."
                        )
                        endFile()
                    }

                    // Remind me what you want
                    220 -> {
                        npcl(
                            "I need you to get me enough dead body parts for me to stitch together a complete body, which I plan to bring to life."
                        )
                        stage = 221
                    }

                    221 -> {
                        playerl("Right...okay...if you insist.")
                        endFile()
                    }

                    // Why are you making this creature?
                    230 -> {
                        npcl(
                            "I came to the land of Morytania many years ago, to find a safe sanctuary for my experiments. This abandoned castle suited my purposes exactly."
                        )
                        stage = 231
                    }

                    231 -> {
                        playerl("What were you experimenting in?")
                        stage = 232
                    }

                    232 -> {
                        npcl("Oh, perfectly innocent experiments - for the good of mankind.")
                        stage = 233
                    }

                    233 -> {
                        playerl("Then why did you need to come to Morytania?")
                        stage = 234
                    }

                    234 -> {
                        npcl("Enough questions, now. Get back to your work.")
                        endFile()
                    }

                    // Will this creature put me out of a job?
                    240 -> {
                        npcl("No, my friend. I have a very special purpose in mind for this creature.")
                        endFile()
                    }
                }
            }

            // Quest stage 3
            3 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Where are my needle and thread, @name?")
                        stage = 300
                    }

                    300 -> {
                        if (submitPart(
                                p,
                                Item(Items.NEEDLE_1733, 1),
                                CreatureOfFenkenstrain.attributeNeedle
                            )
                        ) {
                            npcl("Ah, a needle. Wonderful.")
                            stage = 301
                        } else {
                            stage = 301
                            handle(componentID, buttonID)
                        }
                    }

                    301 -> {
                        if (submitPart(
                                p,
                                Item(Items.THREAD_1734, 5),
                                CreatureOfFenkenstrain.attributeThread
                            )
                        ) {
                            npcl("Some thread. Excellent.")
                            stage = 302
                        } else {
                            stage = 302
                            handle(componentID, buttonID)
                        }
                    }

                    302 -> {
                        val hasNeedle = getAttribute(
                            p,
                            CreatureOfFenkenstrain.attributeNeedle,
                            false
                        )
                        val hasThread = getAttribute(
                            p,
                            CreatureOfFenkenstrain.attributeThread,
                            false
                        )

                        if (!hasNeedle || !hasThread) {
                            endFile()
                        } else {
                            setVarp(
                                p,
                                Vars.VARP_QUEST_CREATURE_OF_FENKENSTRAIN_PROGRESS_399,
                                3,
                                true
                            )
                            dialogue(
                                "Fenkenstrain uses the needle and thread to sew the body parts",
                                "together. Soon, a hideous creature lies inanimate on the ritual table."
                            )
                            stage = 303
                        }
                    }

                    303 -> {
                        npcl("Perfect. But I need one more thing from you - flesh and bones by themselves do not make life.")
                        stage = 304
                    }

                    304 -> {
                        playerl("Really?")
                        stage = 305
                    }

                    305 -> {
                        npcl(
                            "I have honed to perfection an ancient ritual that will give life to this creature, but for this I must harness the very power of Nature."
                        )
                        stage = 306
                    }

                    306 -> {
                        playerl("And what power is this?")
                        stage = 307
                    }

                    307 -> {
                        npcl("The power of lightning.")
                        stage = 308
                    }

                    308 -> {
                        playerl("Sorry, can't make lightning, you've got the wrong @g[man,woman]-")
                        stage = 309
                    }

                    309 -> {
                        npcl(
                            "Silence your insolent tongue! The storm that brews overhead will create the lightning. What I need you to do is to repair the lightning conductor on the balcony above."
                        )
                        stage = 310
                    }

                    310 -> {
                        playerl(
                            "Repair the lightning conductor, right. Can I have a break, soon? By law I'm entitled to 15 minutes every-"
                        )
                        stage = 311
                    }

                    311 -> {
                        npcl("Repair the conductor and BEGONE!!")
                        stage = 312
                    }

                    312 -> {
                        if (getQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN) == 3) {
                            setQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN, 4)
                        }
                        endFile()
                    }
                }
            }

            // Quest stage 4
            4 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        playerl(FaceAnim.THINKING, "How do I repair the lighting conductor?")
                        stage = 400
                    }

                    400 -> {
                        npcl(
                            "Oh, it would be easier to do it myself! If you find a conductor mould you should be able to cast a new one."
                        )
                        stage = 401
                    }

                    401 -> {
                        npcl("Remember this, @name, my experiment will only work with a conductor made from silver.")
                        endFile()
                    }
                }
            }

            // Quest stage 5
            5 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        playerl("So did it work, then?")
                        stage = 500
                    }

                    500 -> {
                        npcl("Yes, I'm afraid it did, @name - all too well.")
                        stage = 501
                    }

                    501 -> {
                        playerl(FaceAnim.SUSPICIOUS, "I can't see it anywhere.")
                        stage = 502
                    }

                    502 -> {
                        npcl("I tricked it into going up to the Tower, and there it remains, imprisoned.")
                        stage = 503
                    }

                    503 -> {
                        playerl(FaceAnim.SUSPICIOUS, "So the creature wasn't all you'd hoped then?")
                        stage = 504
                    }

                    504 -> {
                        npcl("...oh, what have I done...")
                        stage = 505
                    }

                    505 -> {
                        playerl(FaceAnim.SUSPICIOUS, "Oh, I see, we're developing a sense of right and wrong now are we?")
                        stage = 506
                    }

                    506 -> {
                        playerl(FaceAnim.SUSPICIOUS, "Bit late for that, I'd say.")
                        stage = 507
                    }

                    507 -> {
                        npcl("I have no control over it! It's coming to get me!")
                        stage = 508
                    }

                    508 -> {
                        playerl(FaceAnim.SUSPICIOUS, "What do you want me to do about it?")
                        stage = 509
                    }

                    509 -> {
                        npcl("Destroy it!!! Take the key to the Tower and take back the life I never should have granted!!!")
                        stage = 510
                    }

                    510 -> {
                        if (getQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN) == 5) {
                            setQuestStage(p, Quests.CREATURE_OF_FENKENSTRAIN, 6)
                        }
                        addItemOrDrop(p, Items.TOWER_KEY_4185)
                        endFile()
                    }
                }
            }

            // Quest stage 6
            6 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("So have you destroyed it?!!?")
                        stage = 600
                    }

                    600 -> {
                        playerl("Not yet.")
                        stage = 601
                    }

                    601 -> {
                        npcl("Please, hurry - save me!!!!")
                        endFile()
                    }
                }
            }

            // Quest stage 7
            7 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("So have you destroyed it?!!?")
                        stage = 700
                    }

                    700 -> {
                        playerl("Never, now that he has told me the truth!")
                        stage = 701
                    }

                    701 -> {
                        npcl("Oh my, oh my, this is exactly what I feared!")
                        stage = 702
                    }

                    702 -> {
                        npcl("Why did you have to pick Rologarth's brain of all brains?!?")
                        stage = 703
                    }

                    703 -> {
                        playerl("I'm through working for you.")
                        stage = 704
                    }

                    704 -> {
                        npcl(
                            "No! I refuse to release you! You must help me build another creature to destroy this dreadful mistake!!"
                        )
                        endFile()
                    }
                }
            }

            // Quest stages 8 and 100
            8, 100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("theyrecomingtogetme theyrecomingtogetme...")
                        stage = 800
                    }

                    800 -> {
                        playerl(
                            "It is all you deserve. Lord Rologarth is master of this castle once more. Let him protect you - if he wants to."
                        )
                        stage = 801
                    }

                    801 -> {
                        npcl("theyrecomingtogetme theyrecomingtogetme...")
                        endFile()
                    }
                }
            }
        }
    }

    private fun showPartsOptions(player: Player) {
        stage = 200
        handle(0, 0)
    }
}