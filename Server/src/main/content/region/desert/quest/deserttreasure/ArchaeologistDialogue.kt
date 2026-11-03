package content.region.desert.quest.deserttreasure

import content.global.plugins.item.books_and_scrolls.BookInterface
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class ArchaeologistDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, ArchaeologistDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return ArchaeologistDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.ARCHAEOLOGIST_1918)
    }
}

/**
 * Represents the Archaeologist dialogue file.
 *
 * # Relations
 * - [Desert Treasure][DesertTreasure]
 */
class ArchaeologistDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when (getQuestStage(player!!, DesertTreasure.questName)) {
                    0 -> {
                        player(FaceAnim.FRIENDLY, "Hello there.")
                        stage = 1
                    }

                    1, 2 -> {
                        npc("So what did Terry Balando say about those etchings? Did he give you a translation for me?")
                        stage = 100
                    }

                    3 -> {
                        player("Hello there.")
                        stage = 200
                    }

                    4 -> {
                        player("Hello there.")
                        stage = 300
                    }

                    5 -> {
                        player("Hello there.")
                        stage = 400
                    }

                    6, 7, 8 -> {
                        player("Hello there.")
                        stage = 410
                    }
                }
            }

            // Quest stage 0: check requirements.
            1 -> {
                if (DesertTreasure.hasRequirements(player!!)) {
                    npc(
                        FaceAnim.FRIENDLY,
                        "Howdy stranger. What brings you out to these parts?"
                    )
                    stage = 2
                } else {
                    dialogue("He seems to be lost in his own thoughts...")
                    stage = 900
                }
            }

            2 -> {
                options(
                    "What are you doing here?",
                    "Do you have any quests?",
                    "Who are you?",
                    "Nothing really."
                )
                stage = 3
            }

            3 -> {
                when (buttonID) {
                    // What are you doing here?
                    1 -> {
                        player(
                            "Nothing much - What are you doing here?",
                            "It doesn't seem like there's much to do out here in the",
                            "desert!"
                        )
                        stage = 10
                    }

                    // Do you have any quests?
                    2 -> {
                        player(
                            FaceAnim.FRIENDLY,
                            "Do you have any quests?",
                            "Call it a hunch, but you look like the type of man who",
                            "might..."
                        )
                        stage = 20
                    }

                    // Who are you?
                    3 -> {
                        playerl(FaceAnim.THINKING, "Who are you, anyway?")
                        stage = 40
                    }

                    // Nothing really.
                    4 -> {
                        playerl("Nothing really. I'm just wandering around for no good reason.")
                        stage = 50
                    }
                }
            }

            // What are you doing here?
            10 -> {
                npc(
                    "Well, that's where you'd be wrong.",
                    "I work for the Archaeological Society of Varrock, and",
                    "have been excavating around here recently."
                )
                stage = 11
            }

            11 -> {
                npcl("Was there something you specifically wanted?")
                stage = 2
            }

            // Do you have any quests?
            20 -> {
                npc(
                    FaceAnim.HALF_THINKING,
                    "Well, it's funny you should say that.",
                    "I'm not sure if I would really call it a quest as such,",
                    "but I found this ancient stone tablet in one of my",
                    "excavations, and it would really help me out if you"
                )
                stage = 21
            }

            21 -> {
                npc(
                    FaceAnim.FRIENDLY,
                    "could go and take it back to the digsite for me and get",
                    "it examined."
                )
                stage = 22
            }

            22 -> {
                npc(
                    FaceAnim.NEUTRAL,
                    "It's very old, and I don't recognise and of the",
                    "inscriptions on it."
                )
                stage = 23
            }

            23 -> {
                options(
                    "Yes, I'll help you.",
                    "No thanks, I don't want to help."
                )
                stage = 24
            }

            24 -> {
                when (buttonID) {
                    1 -> {
                        addItemOrDrop(player!!, Items.ETCHINGS_4654)
                        player(
                            "Sure, I was heading that way anyway.",
                            "Any particular person at the digsite you want me to",
                            "talk to?"
                        )
                        stage = 25
                    }

                    2 -> {
                        playerl("No thanks. Playing delivery boy doesn't really sound like much fun to me. ")
                        stage = 30
                    }
                }
            }

            25 -> {
                npc(
                    FaceAnim.NEUTRAL,
                    "His name's Terry Balando. Give it to nobody but him.",
                    "I'm sorry, I can't entrust you with the actual tablet I",
                    "found, but it is far too valuable to give away, but I took",
                    "these etchings - they should be enough for him to make"
                )
                stage = 26
            }

            26 -> {
                npc(
                    FaceAnim.NEUTRAL,
                    "a preliminary translation on.",
                    "Come back and let me know what he says, I would hate",
                    "to waste my time excavating anything that isn't worth",
                    "my time as a world famous archaeologist!"
                )
                stage = 27
            }

            27 -> {
                if (getQuestStage(player!!, DesertTreasure.questName) == 0) {
                    setQuestStage(player!!, DesertTreasure.questName, 1)
                }
                endFile()
            }

            30 -> {
                npc(
                    " Well, okay. Can't say as I blame you for thinking that. I'll go take it there myself later I guess."
                )
                endFile()
            }

            // Who are you?
            40 -> {
                npc(
                    FaceAnim.EXTREMELY_SHOCKED,
                    "You don't recognize me???",
                    "I am the world famous Asgarnia Smith, Archaeologist",
                    "extraordinaire!"
                )
                stage = 41
            }

            41 -> {
                npc(
                    FaceAnim.HALF_THINKING,
                    "I am the one who discovered the long forgotten",
                    "Temple of Ikov!",
                    "The one who unearthed the strange trap filled arena",
                    "in Brimhaven!"
                )
                stage = 42
            }

            42 -> {
                npc(
                    FaceAnim.HALF_THINKING,
                    "I'm the leading archaeological expert of our time!",
                    "I was voted archaeologist of the year four years",
                    "running by the Varrock Herald! Are you REALLY",
                    "sure you've never heard of me?"
                )
                stage = 43
            }

            43 -> {
                playerl(FaceAnim.THINKING, "No, I really haven't.")
                stage = 44
            }

            44 -> {
                npc(
                    FaceAnim.EXTREMELY_SHOCKED,
                    "Well then, I'm confused.",
                    "Why did you come over and speak to me if you don't",
                    "know who I am? What do you want?"
                )
                stage = 2
            }

            // Nothing really.
            50 -> {
                npcl("Uh-huh. Well, if you'll excuse me, I have a lot more work to do before I can bed down for the night.")
                endFile()
            }

            // Quest stages 1 and 2.
            100 -> {
                playerl("Um...yeah...about that... I kind of didn't go and speak to him yet...")
                stage = 101
            }

            101 -> {
                npcl("Well what are you waiting for? A written invitation? All I want you to do is take those etchings up to the digsite for me!")
                stage = 102
            }

            102 -> {
                playerl("Okay, I'll do that then.")
                endFile()
            }

            // Quest stage 3: check for the translation.
            200 -> {
                npcl("So what did Terry Balando say about those etchings? Did he give you a translation for me?")

                if (inInventory(player!!, Items.TRANSLATION_4655)) {
                    stage = 201
                } else {
                    stage = 210
                }
            }

            // Translation available.
            201 -> {
                playerl("Yeah, he did. I have it here in this book.")
                stage = 202
            }

            202 -> {
                npcl("Did you take a read of this? It will do you good to understand how this profession works. Here, have a read.")
                stage = 203
            }

            203 -> {
                options("Read book", "Don't read book")
                stage = 204
            }

            204 -> {
                when (buttonID) {
                    1 -> {
                        BookInterface.openBook(
                            player!!,
                            BookInterface.FANCY_BOOK_3_49,
                            TranslationBook.Companion::display
                        )
                        endFile()
                    }

                    2 -> {
                        playerl("Yeah, I did. Kind of boring really.")
                        stage = 205
                    }
                }
            }

            205 -> {
                npcl("Excellent. Just give me a moment to read this, and talk to me again in a second.")

                if (getQuestStage(player!!, DesertTreasure.questName) == 3) {
                    removeItem(player!!, Items.TRANSLATION_4655)
                    setQuestStage(player!!, DesertTreasure.questName, 4)
                }
                endFile()
            }

            // Translation not available.
            210 -> {
                playerl("Yeah, he did. But I don't have it with me.")
                stage = 211
            }

            211 -> {
                npcl("...")
                stage = 212
            }

            212 -> {
                npcl("I see.")
                stage = 213
            }

            213 -> {
                npcl("Well could you go and get me that translation? It could be vital!")
                endFile()
            }

            // Quest stage 4: treasure proposal.
            300 -> {
                npcl("Hmmm. Interesting. It seems to me like there's some kind of treasure hidden out in the desert.")
                stage = 301
            }

            301 -> {
                npcl("So what do you say? Fancy being a treasure hunter like me?")
                stage = 302
            }

            302 -> {
                playerl("Uh... don't you mean archaeologist?")
                stage = 303
            }

            303 -> {
                npcl("Yeeees... An archaeologist...")
                stage = 304
            }

            304 -> {
                npcl("It's all this hot sun getting to me I think.")
                stage = 305
            }

            305 -> {
                npcl("Well anyway, let's just assume there is a treasure hidden in the desert somewhere, and let's just say for the sake of argument that maybe if I found a big stash of gold and treasure I wouldn't necessarily just hand it")
                stage = 306
            }

            306 -> {
                npcl("all over to the Museum of Varrock.")
                stage = 307
            }

            307 -> {
                npcl("Let's also say, purely hypothetically, that if there were such a big stash of treasure and someone were to help me find it, then that hypothetical personal might be entitled to, oh, let's say a purely for the sake of")
                stage = 308
            }

            308 -> {
                npcl("argument thirty percent split...")
                stage = 309
            }

            309 -> {
                playerl("Fifty percent.")
                stage = 310
            }

            310 -> {
                npcl("That's right!")
                stage = 311
            }

            311 -> {
                npcl("A purely for the sake of argument fifty-fifty split on this hypothetical treasure, should it exist...")
                stage = 312
            }

            312 -> {
                npcl("Do you see where I'm going with this?")
                stage = 313
            }

            313 -> {
                playerl("You want me to help you find some treasure for a fifty percent share, as long as I don't tell anybody, and your reputation as an esteemed archaeologist with the Museum of Varrock remains intact, and nobody")
                stage = 314
            }

            314 -> {
                playerl("discovers you're actually just a treasure hunter?")
                stage = 315
            }

            315 -> {
                npcl("Uh... yes, but the way you said it makes it sound like I'm doing something wrong...")
                stage = 316
            }

            316 -> {
                npcl("So what do you say? Partners?")
                stage = 317
            }

            317 -> {
                options("Help him", "Don't help him")
                stage = 318
            }

            318 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Well... I guess nobody is really going to lose out on anything, and we don't even know if there is any treasure...")
                        stage = 319
                    }

                    2 -> {
                        playerl("This all sounds very immoral, and I don't want any part of your seedy little schemes.")
                        stage = 330
                    }
                }
            }

            // First "Help him" option.
            319 -> {
                playerl("Aw, go on then. Count me in.")
                stage = 320
            }

            320 -> {
                npcl("Good @g[lad,lass]! Well, if we split up we'll be able to find treasure quicker.")
                stage = 321
            }

            321 -> {
                npcl("I'll continue searching around here in this Bedabin Camp, you head due South to the Bandit Village. If either of us find anything, we'll come find each other and say what, okay?")
                stage = 322
            }

            322 -> {
                npcl("You head due South, see what you can find out about this tablet.")
                if (getQuestStage(player!!, DesertTreasure.questName) == 4) {
                    setQuestStage(player!!, DesertTreasure.questName, 5)
                }
                endFile()
            }

            // First "Don't help him" option.
            330 -> {
                npcl("Aw, c'mon, cut me a break here man! Look, nobody is really getting hurt, right? This treasure is either going to lie around in some underground ruin, or lie around in some museum for")
                stage = 331
            }

            331 -> {
                npcl("people to look at!")
                stage = 332
            }

            332 -> {
                npcl("It makes sense for the people to go to all the risk to get a little payback for it, right? Besides, anything of real historical value we can hand over to the museum - we'll just keep the cash for")
                stage = 333
            }

            333 -> {
                npcl("ourselves!")
                stage = 334
            }

            334 -> {
                npcl("C'mon... what do you say? Partners?")
                stage = 335
            }

            335 -> {
                options("Help him", "Don't help him")
                stage = 336
            }

            336 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Well... I guess nobody is really going to lose out on anything, and we don't even know if there is any treasure...")
                        stage = 319
                    }

                    2 -> {
                        playerl("I don't trust anything about your little scheme, including you! I want nothing to do with this!")
                        stage = 340
                    }
                }
            }

            // Final refusal.
            340 -> {
                npcl("Eh, you'll come running back when you realize just how much money we could be talking here.")
                stage = 341
            }

            341 -> {
                npcl("I'll see you again when you come crawling back, begging to be cut in on the deal.")
                endFile()
            }

            // Quest stage 5.
            400 -> {
                npcl("Hello again. You find any signs of that treasure yet?")
                stage = 401
            }

            401 -> {
                playerl("Not yet...")
                stage = 402
            }

            402 -> {
                npcl("Well, head south to that bandit camp. I bet if there's anything out here, they'll know about it. Don't let on it might be valuable though! Those thieves will steal it as soon as look at you!")
                endFile()
            }

            // Quest stages 6, 7 and 8.
            410 -> {
                npcl("Hello again. You find any signs of that treasure yet?")
                stage = 411
            }

            411 -> {
                playerl("Not as such... Although I think I'm onto something of a promising lead...")
                stage = 412
            }

            412 -> {
                playerl("Did you ever hear of something called the Diamonds of Azzanadra?")
                stage = 413
            }

            413 -> {
                npcl("Diamonds of Azzanadra? That sounds very promising indeed!")
                stage = 414
            }

            414 -> {
                playerl("So you have heard of them?")
                stage = 415
            }

            415 -> {
                npcl("Nope, never heard of them before, but you said diamonds! Diamonds are always promising!")
                endFile()
            }

            900 -> endFile()
        }
    }
}