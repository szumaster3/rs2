package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Eblis dialogue.
 */
@Initializable
class EblisMirrorsDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, EblisMirrorsDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return EblisMirrorsDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.EBLIS_1924, NPCs.EBLIS_1925)
    }
}
class EblisMirrorsDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, DesertTreasure.questName)) {
            8 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Ah, so you got here at last.")
                        stage++
                    }

                    1 -> {
                        npc(
                            "As you may noticed, I have made the mirrors for",
                            "the spell, and cast the enchantment upon them."
                        )
                        stage++
                    }

                    2 -> {
                        npc(
                            "By simply looking into each mirror, you will be able to",
                            "see the area where the trace magics from the Diamonds",
                            "of Azzanadra are emanating from."
                        )
                        stage++
                    }

                    3 -> {
                        npc(
                            "Unfortunately, I cannot narrow the search closer with",
                            "this kind of spell, but if you search the areas shown to",
                            "you, you may be able to find some clues leading you to",
                            "the evil warriors of Zamorak who stole the diamonds in"
                        )
                        stage++
                    }

                    4 -> {
                        npc("the first place.")
                        stage++
                    }

                    5 -> {
                        player(
                            FaceAnim.THINKING,
                            "So you can't be anymore specific about where to look",
                            "for these warriors and their diamonds?"
                        )
                        stage++
                    }

                    6 -> {
                        npc(
                            "I'm afraid not, other than the direction that the mirror",
                            "is facing will be approximately the direction you will",
                            "need to head in."
                        )
                        stage++
                    }

                    7 -> {
                        npc("Make sure to come and speak to me when you have", "retrieved all four diamonds.")
                        stage++
                    }

                    8 -> {
                        if (getQuestStage(player!!, DesertTreasure.questName) == 8) {
                            setQuestStage(player!!, DesertTreasure.questName, 9)
                        }
                        endFile()
                    }
                }
            }

            9, 10 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        if (DesertTreasure.completedAllSubstages(player!!)) {
                            player(
                                FaceAnim.THINKING,
                                "So I have all four of these Diamonds of Azzanadra, now",
                                "what?"
                            )
                            stage = 20
                        } else {
                            playerl("So can you give me any help on where to find these warriors and their diamonds?")
                            stage = 10
                        }
                    }

                    10 -> {
                        npcl(
                            "No, the magic used in this spell is powerful, but inaccurate. The direction the scrying glass faces is roughly the direction you will find the warrior, but I'm afraid I"
                        )
                        stage++
                    }

                    11 -> {
                        npcl("can't be any more help than that.")
                        stage++
                    }

                    12 -> {
                        playerl("I don't understand why there are six mirrors when there are only four diamonds...")
                        stage++
                    }

                    13 -> {
                        npcl("As I say, the enchantment is very inaccurate.")
                        stage++
                    }

                    14 -> {
                        npcl(
                            "I can only focus upon the aura the diamonds have left behind them, so any place where the Diamonds were present for a significant period of time will still be shown - such as the Bandit Camp where I make my home."
                        )
                        stage++
                    }

                    15 -> {
                        npcl("My apologies, but magic is an inaccurate art in many respects.")
                        stage++
                    }

                    16 -> {
                        npcl("Don't forget to come back here when you have collected all four diamonds.")
                        endFile()
                    }

                    20 -> {
                        npc(
                            "Azzanadra was our greatest ever hero.",
                            "He was unkillable, and the cowardly traitors who stole",
                            "our lands did not know what to do with him, for his",
                            "hatred for them was as strong as his magics."
                        )
                        stage++
                    }

                    21 -> {
                        npc(
                            FaceAnim.SAD,
                            "In the end, they cast a spell upon him, to trap him in",
                            "the stone structure to the South of here."
                        )
                        stage++
                    }

                    22 -> {
                        npc(
                            FaceAnim.ANNOYED,
                            "They stole his very life force, the essence of his power,",
                            "and trapped it within four crystals - the very same",
                            "Four Diamonds which you have now recovered from",
                            "the brigands who stole from us."
                        )
                        stage++
                    }

                    23 -> {
                        npc(
                            FaceAnim.ANNOYED,
                            "The four pillars surrounding the structure are keeping",
                            "the containment spell intact.",
                            "By placing a diamond into each, you will breach the",
                            "magical defenses and begin to restore Azzanadra's"
                        )
                        stage++
                    }

                    24 -> {
                        npcl(FaceAnim.ANNOYED, "power, and be able to enter the structure.")
                        stage++
                    }

                    25 -> {
                        npcl("Go, place the diamonds, and free my lord Azzanadra!")
                        stage++
                    }

                    26 -> {
                        npc(
                            FaceAnim.FRIENDLY,
                            "The path will be hard, for his prison is full of traps",
                            "and danger to prevent his rescue, but he will reward you",
                            "beyond your wildest dreams when freed!"
                        )
                        stage++
                    }

                    27 -> {
                        npc(
                            "Quickly...",
                            "After all these centuries, Lord Azzanadra is nearly free!",
                            "You must spare no time, place the Diamonds upon the",
                            "pillars and enter the pyramid so that you may free him!"
                        )
                        endFile()
                    }
                }
            }

            100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        if (inInventory(player!!, Items.ANCIENT_STAFF_4675)) {
                            playerl("Hello again.")
                            stage = 10
                        } else {
                            npcl("So have you spoken to my Lord Azzanadra yet?")
                            stage = 20
                        }
                    }

                    10 -> {
                        npcl("Greetings. I await the return of my Lord Azzanadra and of our god. I do not know why, but I feel this spot has some significance...")
                        endFile()
                    }

                    20 -> {
                        playerl("Yes I have.")
                        stage++
                    }

                    21 -> {
                        npcl("And what words did he have for his followers?")
                        stage++
                    }

                    22 -> {
                        playerl("Er... He didn't really mention you at all, but he did teach me some cool new magic spells.")
                        stage++
                    }

                    23 -> {
                        npcl("It is understandable perhaps... His poor mind must be addled after all of those years of confinement, he would not willingly ignore his followers...")
                        stage++
                    }

                    24 -> {
                        npcl("Anyway, if he has taught you our ancient magics, you may be interested in purchasing an ancient heirloom that was passed down to me. My ancestor fought in the ancient battles using the")
                        stage++
                    }

                    25 -> {
                        npcl("magic of our god. This heirloom will help you with the speed of your spell-casting.")
                        stage++
                    }

                    26 -> {
                        npcl("Normally I could not bear to part with such a priceless relic, but for your help in freeing my Lord Azzanadra, I will be prepared to sell it to you for a mere 80,000 gold.")
                        stage++
                    }

                    27 -> {
                        npcl("Are you interested?")
                        options("Yes please", "No thanks")
                        stage++
                    }

                    28 -> {
                        when (buttonID) {
                            1 -> {
                                if (inInventory(player!!, Items.COINS_995, 80000)) {
                                    npcl("Take care of it, it is the only heirloom from those times I possess, although rumour has it many of our ancient warriors were buried with identical weapons so that they could continue to fight for my Lord in their deaths.")
                                    stage = 30
                                } else {
                                    dialogue("You don't have enough money to buy that.")
                                    endFile()
                                }
                            }

                            2 -> {
                                playerl("No, not really.")
                                stage = 40
                            }
                        }
                    }

                    30 -> {
                        if (removeItem(player!!, Item(Items.COINS_995, 80000))) {
                            addItemOrDrop(player!!, Items.ANCIENT_STAFF_4675)
                        }
                        endFile()
                    }

                    40 -> {
                        npcl("As you wish. Bear my offer in mind should you ever change your decision, I will remain here.")
                        endFile()
                    }
                }
            }
        }
    }
}