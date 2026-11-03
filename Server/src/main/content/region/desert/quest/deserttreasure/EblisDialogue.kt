package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Eblis dialogue.
 */
@Initializable
class EblisDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, EblisDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return EblisDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.EBLIS_1923)
    }

}

class EblisDialogueFile : DialogueFile() {

    companion object {
        fun checkAllGiven(player: Player): Boolean {
            return (
                    getAttribute(player, DesertTreasure.attributeCountMagicLogs, 0) >= 12 &&
                            getAttribute(player, DesertTreasure.attributeCountSteelBars, 0) >= 6 &&
                            getAttribute(player, DesertTreasure.attributeCountMoltenGlass, 0) >= 6 &&
                            getAttribute(player, DesertTreasure.attributeCountBones, 0) >= 1 &&
                            getAttribute(player, DesertTreasure.attributeCountAshes, 0) >= 1 &&
                            getAttribute(player, DesertTreasure.attributeCountCharcoal, 0) >= 1 &&
                            getAttribute(player, DesertTreasure.attributeCountBloodRune, 0) >= 1
                    )
        }
    }

    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, DesertTreasure.questName)

        when (questStage) {
            in 0..4 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Leave us to our fate. We care nothing for the world that betrayed us, or those that come from it.")
                        stage = 1
                    }

                    1 -> endFile()
                }
            }

            5, 6 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        playerl("Hello. I represent the Museum of Varrock, and I have reason to believe there may be some kinds of artefacts of historical significance in the nearby area...")
                        stage = 1
                    }

                    1 -> {
                        npcl("Ah yes. The only time people care about our existence is when they think they have something to gain from us.")
                        stage = 2
                    }

                    2 -> {
                        npcl("I have nothing to say to you. You and your kind are not welcome here.")
                        stage = 3
                    }

                    3 -> {
                        playerl("Please, if I can just have a few minutes of your time to ask some questions...?")
                        stage = 4
                    }

                    4 -> {
                        npcl("(sigh) I suppose I can spare you that. What do you wish to know about?")
                        stage = 5
                    }

                    // Main options
                    5 -> {
                        if (questStage == 6) {
                            options(
                                "Why is this village so hostile?",
                                "Do you know anything about treasure near here?",
                                "Do you know anything about a fortress near here?",
                                "Tell me of the four diamonds of Azzanadra.",
                                "Nothing thanks."
                            )
                        } else {
                            options(
                                "Why is this village so hostile?",
                                "Do you know anything about treasure near here?",
                                "Do you know anything about a fortress near here?",
                                "Nothing thanks."
                            )
                        }
                        stage = 6
                    }

                    6 -> {
                        when (buttonID) {
                            // Why is this village so hostile?
                            1 -> {
                                playerl("Why are all of the people here so hostile? You would think I was asking you for money instead of just for answers to a few questions...")
                                stage = 10
                            }

                            // Do you know anything about treasure near here?
                            2 -> {
                                playerl("I was wondering if you knew anything about some treasure somewhere around here?")
                                stage = 40
                            }

                            // Do you know anything about a fortress near here?
                            3 -> {
                                playerl("Do you know anything about some kind of fortress nearby? I have reason to believe there is, or at least used to be, some kind of fortress very close to here...")
                                stage = 50
                            }

                            // Tell me of the four diamonds of Azzanadra.
                            4 -> {
                                if (questStage == 6) {
                                    playerl("So tell me... Did you ever hear of something called the Diamonds of Azzanadra?")
                                    stage = 60
                                } else {
                                    playerl("Actually, there was nothing I really wanted to ask you about.")
                                    stage = 90
                                }
                            }

                            // Nothing thanks.
                            5 -> {
                                playerl("Actually, there was nothing I really wanted to ask you about.")
                                stage = 90
                            }
                        }
                    }

                    // Hostility story
                    10 -> {
                        npcl("It is a long story, and I doubt you have much interest in hearing it. Your sort never are, you just take what you can of ours, and then abandon us once more to the desert.")
                        stage = 11
                    }

                    11 -> {
                        options(
                            "No, I want to hear this story.",
                            "I don't care about your story."
                        )
                        stage = 12
                    }

                    12 -> {
                        when (buttonID) {
                            1 -> {
                                playerl("Actually, I'd be quite interested to hear what it is you have to say to excuse the attitude everybody in this village seems to have.")
                                stage = 20
                            }

                            2 -> {
                                playerl("I don't really care what your story is to be honest, there is no excuse for such rudeness or hostility.")
                                stage = 30
                            }
                        }
                    }

                    // No, I want to hear this story.
                    20 -> {
                        npcl("Ah, it all begun many generations ago, when our ancestors were the proud rulers of these lands...")
                        stage = 21
                    }

                    21 -> {
                        npcl("My ancestors lived far to the North of here, and our lands stretched from the sea in the East to the river Lum, and the mountain of ice. From coast to coast, North to South, our domain was absolute.")
                        stage = 22
                    }

                    22 -> {
                        npcl("Our god was kind to us, and blessed us with prosperity and happiness, and in return we were merciless to his enemies wherever we found them.")
                        stage = 23
                    }

                    23 -> {
                        npcl("Then came the betrayal.")
                        stage = 24
                    }

                    24 -> {
                        npcl("Our god was banished, leaving us helpless to our fates.")
                        stage = 25
                    }

                    25 -> {
                        npcl("Without his protection, we were forced to fend for ourselves once more, against the enemies that sought to destroy us through their petty jealousies.")
                        stage = 26
                    }

                    26 -> {
                        npcl("But we did not succumb without fighting! The spiteful Saradomin and pathetic Zamorak warred with each other, but the hatred they had for each other was as nothing to the hatred they held towards us!")
                        stage = 27
                    }

                    27 -> {
                        npcl("With each battle they waged, we lost more and more land, unable to fight on all fronts, and were pushed further and further South into this gods-forsaken desert.")
                        stage = 28
                    }

                    28 -> {
                        npcl("Our greatest hero, Azzanadra, was finally trapped in a strange stone structure to the South of here, and bound within by terrible powers...")
                        stage = 29
                    }

                    29 -> {
                        npcl("And with that our lands, our homes, our very lives were stolen from us! Too weak to reclaim what was rightfully ours, we made our homes here, knowing that someday Azzanadra will")
                        stage = 291
                    }

                    291 -> {
                        npcl("return with his magnificent power, and bring us back to our former glory...")
                        stage = 292
                    }

                    292 -> {
                        playerl("So you're upset because of something that happened hundreds of years ago?")
                        stage = 293
                    }

                    293 -> {
                        playerl("Seems to me like maybe you should find some closure, and let the past go...")
                        stage = 294
                    }

                    294 -> {
                        npcl("The insults heaped upon my race will never be forgotten, will never be forgiven and will never again be overlooked.")
                        stage = 295
                    }

                    295 -> {
                        npcl("Someday, a harsh wind will blow upon this land, uncovering the wrongs of the past, and we will get back what is rightfully ours. Until such a day we will bide our time here, and will")
                        stage = 296
                    }

                    296 -> {
                        npcl("always be ready with our blades for our righteous vengeance.")
                        endFile()
                    }

                    // I don't care about your story.
                    30 -> {
                        playerl("I have done nothing wrong to you, but everybody here treats me like I have committed some great crime against the village.")
                        stage = 31
                    }

                    31 -> {
                        npcl("That is because, from our point of view, you have.")
                        stage = 32
                    }

                    32 -> {
                        playerl("What? Just because I entered your village?")
                        stage = 33
                    }

                    33 -> {
                        npcl("You have no right to be here! You have no right to the life you have, for it was taken at our expense!")
                        stage = 34
                    }

                    34 -> {
                        playerl("Whatever... No wonder all you loonies live out here in the desert by yourselves.")
                        endFile()
                    }

                    // Do you know anything about treasure near here?
                    40 -> {
                        playerl("I have some evidence that there might be some kind of treasure hidden very close to this village...")
                        stage = 41
                    }

                    41 -> {
                        npcl("If I knew of any treasure I would not choose to spend my life in this gods-forsaken desert.")
                        endFile()
                    }

                    // Do you know anything about a fortress near here?
                    50 -> {
                        npcl("Nobody would build anything in this wasteland unless they were forced to, to survive.")
                        stage = 51
                    }

                    51 -> {
                        npcl("I know of no fortress, I know of no reason why anyone would ever bother doing anything out here in the desert.")
                        endFile()
                    }

                    // Tell me of the four diamonds of Azzanadra.
                    60 -> {
                        npcl("This is the treasure which you seek???")
                        stage = 61
                    }

                    61 -> {
                        npcl("Please accept my apologies noble @g[sir,madam]! I thought you were but some opportunistic thief, looking to steal what heritage we have left! Now I see that you are in fact a brave adventurer,")
                        stage = 62
                    }

                    62 -> {
                        npcl("looking to restore our glories back upon us!")
                        stage = 63
                    }

                    63 -> {
                        playerl("Uh... yeah... So anyway, you have heard of them?")
                        stage = 64
                    }

                    64 -> {
                        npcl("Heard of them? Of course I have heard of them! They are the legacy of the great Mahjarrat hero, Azzanadra!")
                        stage = 65
                    }

                    65 -> {
                        playerl("So... do you have any idea where they might be? I have a feeling they will be very valuable.")
                        stage = 66
                    }

                    66 -> {
                        playerl("Uh, valuable as historical artefacts I mean, obviously.")
                        stage = 67
                    }

                    67 -> {
                        npcl("They were stolen by warriors of the false god Zamorak generations ago. When you find the warriors, you will find the diamonds.")
                        stage = 68
                    }

                    68 -> {
                        npc(
                            "Beware too, for these warriors are very powerful;",
                            "they have taken the powers of the diamonds into themselves!"
                        )
                        stage = 69
                    }

                    69 -> {
                        playerl("How do you mean?")
                        stage = 70
                    }

                    70 -> {
                        npcl("Each diamond has an elemental quality...")
                        stage = 71
                    }

                    71 -> {
                        npcl("There is the Diamond of Blood, the Diamond of Ice, the Diamond of Smoke and the Diamond of Shadow.")
                        stage = 72
                    }

                    72 -> {
                        npcl("You should expect the warriors to have taken some aspect of these diamonds as their own...")
                        stage = 73
                    }

                    73 -> {
                        playerl("Do you have any idea how I could track down these warriors somehow, then?")
                        stage = 74
                    }

                    74 -> {
                        npcl("There is an ancient spell I know of that may spy upon such power... But it will require a few ingredients for it to work.")
                        stage = 75
                    }

                    75 -> {
                        npcl("Should you be willing to get these ingredients for me, I will be able to locate the rough area where each of these warriors has taken refuge. The spell is imprecise, but it should help you get on the")
                        stage = 76
                    }

                    76 -> {
                        npcl("right track in your search.")
                        stage = 77
                    }

                    77 -> {
                        npcl("Is your desire for our freedom strong enough? Will you gather the ingredients for this spell for me?")
                        stage = 78
                    }

                    78 -> {
                        options("Yes", "No")
                        stage = 79
                    }

                    79 -> {
                        when (buttonID) {
                            // Yes
                            1 -> {
                                playerl("Sure, what do you need?")
                                stage = 80
                            }

                            // No
                            2 -> {
                                playerl("Actually I don't feel like going on a shopping trip for you right now.")
                                stage = 100
                            }
                        }
                    }

                    // Explain ingredients
                    80 -> {
                        npcl("For this spell, I will need to make some scrying glasses. I will need enough so that we can view the realm in its entirety.")
                        stage = 81
                    }

                    81 -> {
                        npcl("When enchanted, the scrying glass will be able to let us view any area that has been influenced by the presence of the Diamonds of Azzanadra.")
                        stage = 82
                    }

                    82 -> {
                        playerl("Okay, but what exactly do you need for this spell?")
                        stage = 83
                    }

                    83 -> {
                        npcl("Well, six scrying glasses should be sufficient. For each scrying glass, I will need two magic logs, a steel bar and some molten glass. This makes a total of 12 magic logs, 6 pieces of molten")
                        stage = 84
                    }

                    84 -> {
                        npcl("glass, and 6 steel bars.")
                        stage = 85
                    }

                    85 -> {
                        npcl("In addition, for the actual spell to enchant the glasses, I will require one set of normal bones, some ash, some charcoal and a single blood rune.")
                        stage = 86
                    }

                    86 -> {
                        npcl("Do you understand me, adventurer?")
                        stage = 87
                    }

                    87 -> {
                        playerl("Quick question; what kind of bones do you need?")
                        stage = 88
                    }

                    88 -> {
                        npcl("Standard bones. Other types of bones are of no use to me in this spell.")
                        stage = 89
                    }

                    89 -> {
                        options(
                            "Yes, I will go get those for you.",
                            "No, please repeat those ingredients."
                        )
                        stage = 90
                    }

                    90 -> {
                        when (buttonID) {
                            // Yes, I will go get those for you.
                            1 -> {
                                playerl("It's a slightly odd collection of ingredients, but I shouldn't have too much trouble getting those for you.")
                                stage = 91
                            }

                            // No, please repeat those ingredients.
                            2 -> {
                                npcl("Before I can complete the spell I will still need the following items;")
                                stage = 92
                            }
                        }
                    }

                    91 -> {
                        if (getQuestStage(player!!, DesertTreasure.questName) == 6) {
                            setQuestStage(player!!, DesertTreasure.questName, 7)
                        }
                        endFile()
                    }

                    92 -> {
                        npc(
                            "12 magic logs",
                            "6 steel bars",
                            "6 molten glass"
                        )
                        stage = 93
                    }

                    93 -> {
                        npc(
                            "1 bones,",
                            "1 ashes,",
                            "1 charcoal",
                            "and 1 blood rune."
                        )
                        stage = 94
                    }

                    94 -> {
                        if (getQuestStage(player!!, DesertTreasure.questName) == 6) {
                            setQuestStage(player!!, DesertTreasure.questName, 7)
                        }
                        endFile()
                    }

                    // No, I don't want to gather ingredients.
                    100 -> {
                        npcl("As you wish. I should have known not to get my hopes up that our long cursed life may soon be at an end...")
                        endFile()
                    }

                    // Nothing thanks.
                    101 -> endFile()
                }
            }

            // Quest stage 7 - ingredient check
            7 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        if (checkAllGiven(player!!)) {
                            npcl("Excellent! Those are all the ingredients I need to create the scrying glasses.")
                            stage = 1
                        } else {
                            npcl("Before I can complete the spell I will still need the following items;")
                            stage = 10
                        }
                    }

                    1 -> {
                        npcl("I will find a suitable spot in the desert to the East of here, and set them up. When you are ready to begin your search, please come and find me there, I will show you how to utilise the")
                        stage = 2
                    }

                    2 -> {
                        npcl("mirrors to find the diamonds.")
                        stage = 3
                    }

                    3 -> {
                        if (getQuestStage(player!!, DesertTreasure.questName) == 7) {
                            setQuestStage(player!!, DesertTreasure.questName, 8)
                        }
                        endFile()
                    }

                    // Missing ingredients
                    10 -> {
                        npc(
                            FaceAnim.NEUTRAL,
                            "${12 - getAttribute(player!!, DesertTreasure.attributeCountMagicLogs, 0)} magic logs",
                            "${6 - getAttribute(player!!, DesertTreasure.attributeCountSteelBars, 0)} steel bars",
                            "${6 - getAttribute(player!!, DesertTreasure.attributeCountMoltenGlass, 0)} molten glass"
                        )
                        stage = 11
                    }

                    11 -> {
                        npc(
                            FaceAnim.NEUTRAL,
                            "${1 - getAttribute(player!!, DesertTreasure.attributeCountBones, 0)} bones,",
                            "${1 - getAttribute(player!!, DesertTreasure.attributeCountAshes, 0)} ashes,",
                            "${1 - getAttribute(player!!, DesertTreasure.attributeCountCharcoal, 0)} charcoal",
                            "and ${1 - getAttribute(player!!, DesertTreasure.attributeCountBloodRune, 0)} blood rune."
                        )
                        endFile()
                    }
                }
            }

            // Quest stages 8, 9, 10
            in 8..10 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Meet me again in the desert East of here, I will use these ingredients to create a scrying glass for you.")
                        stage = 1
                    }

                    1 -> endFile()
                }
            }

            // Quest completed
            100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Meet me again in the desert East of here.")
                        stage = 1
                    }

                    1 -> endFile()
                }
            }

            else -> endFile()
        }
    }
}

class EblisCollectionsListeners : InteractionListener {
    override fun defineListeners() {

        onUseWith(
            IntType.NPC,
            intArrayOf(Items.MAGIC_LOGS_1513, Items.MAGIC_LOGS_1514),
            NPCs.EBLIS_1923
        ) { player, used, with ->
            for (i in 0..11) {
                if (inInventory(player, used.id)) {
                    if (getAttribute(player, DesertTreasure.attributeCountMagicLogs, 0) < 12) {
                        if (removeItem(player, used.id)) {
                            setAttribute(
                                player, DesertTreasure.attributeCountMagicLogs,
                                getAttribute(player, DesertTreasure.attributeCountMagicLogs, 0) + 1
                            )
                            sendMessage(player, "You hand over a magic log.")
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            return@onUseWith true
        }

        onUseWith(
            IntType.NPC,
            intArrayOf(Items.STEEL_BAR_2353, Items.STEEL_BAR_2354),
            NPCs.EBLIS_1923
        ) { player, used, with ->
            for (i in 0..5) {
                if (inInventory(player, used.id)) {
                    if (getAttribute(player, DesertTreasure.attributeCountSteelBars, 0) < 6) {
                        if (removeItem(player, used.id)) {
                            setAttribute(
                                player, DesertTreasure.attributeCountSteelBars,
                                getAttribute(player, DesertTreasure.attributeCountSteelBars, 0) + 1
                            )
                            sendMessage(player, "You hand over a steel bar.")
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            return@onUseWith true
        }

        onUseWith(
            IntType.NPC,
            intArrayOf(Items.MOLTEN_GLASS_1775, Items.MOLTEN_GLASS_1776),
            NPCs.EBLIS_1923
        ) { player, used, with ->
            for (i in 0..5) {
                if (inInventory(player, used.id)) {
                    if (getAttribute(player, DesertTreasure.attributeCountMoltenGlass, 0) < 6) {
                        if (removeItem(player, used.id)) {
                            setAttribute(
                                player, DesertTreasure.attributeCountMoltenGlass,
                                getAttribute(player, DesertTreasure.attributeCountMoltenGlass, 0) + 1
                            )
                            sendMessage(player, "You hand over some molten glass.")
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            return@onUseWith true
        }

        onUseWith(IntType.NPC, intArrayOf(Items.BONES_526, Items.BONES_527), NPCs.EBLIS_1923) { player, used, with ->
            if (getAttribute(player, DesertTreasure.attributeCountBones, 0) < 1) {
                if (removeItem(player, used.id)) {
                    setAttribute(
                        player, DesertTreasure.attributeCountBones,
                        getAttribute(player, DesertTreasure.attributeCountBones, 0) + 1
                    )
                    sendNPCDialogue(player, NPCs.EBLIS_1923, "Thank you, those are enough bones for the spell.")
                }
            }
            return@onUseWith true
        }

        onUseWith(IntType.NPC, intArrayOf(Items.ASHES_592, Items.ASHES_593), NPCs.EBLIS_1923) { player, used, with ->
            if (getAttribute(player, DesertTreasure.attributeCountAshes, 0) < 1) {
                if (removeItem(player, used.id)) {
                    setAttribute(
                        player, DesertTreasure.attributeCountAshes,
                        getAttribute(player, DesertTreasure.attributeCountAshes, 0) + 1
                    )
                    sendNPCDialogue(player, NPCs.EBLIS_1923, "Thank you, that is enough ash for the spell.")
                }
            }
            return@onUseWith true
        }

        onUseWith(
            IntType.NPC,
            intArrayOf(Items.CHARCOAL_973, Items.CHARCOAL_974),
            NPCs.EBLIS_1923
        ) { player, used, with ->
            if (getAttribute(player, DesertTreasure.attributeCountCharcoal, 0) < 1) {
                if (removeItem(player, used.id)) {
                    setAttribute(
                        player, DesertTreasure.attributeCountCharcoal,
                        getAttribute(player, DesertTreasure.attributeCountCharcoal, 0) + 1
                    )
                    sendNPCDialogue(player, NPCs.EBLIS_1923, "Thank you, that is enough charcoal for the spell.")
                }
            }
            return@onUseWith true
        }

        onUseWith(IntType.NPC, intArrayOf(Items.BLOOD_RUNE_565), NPCs.EBLIS_1923) { player, used, with ->
            if (getAttribute(player, DesertTreasure.attributeCountBloodRune, 0) < 1) {
                if (removeItem(player, used.id)) {
                    setAttribute(
                        player, DesertTreasure.attributeCountBloodRune,
                        getAttribute(player, DesertTreasure.attributeCountBloodRune, 0) + 1
                    )
                    sendNPCDialogue(
                        player,
                        NPCs.EBLIS_1923,
                        "Thank you, that blood rune should be sufficient for the spell."
                    )
                }
            }
            return@onUseWith true
        }

    }
}