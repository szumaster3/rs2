package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Rasolo dialogue.
 */
@Initializable
class RasoloDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, RasoloDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return RasoloDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.RASOLO_1972)
    }
}

class RasoloDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, DesertTreasure.questName)
        val shadowStage = DesertTreasure.getSubStage(player!!, DesertTreasure.attributeShadowStage)

        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    questStage in 0..8 -> 10
                    questStage >= 9 && shadowStage == 0 -> 100
                    questStage >= 9 && shadowStage in 1..2 -> 200
                    questStage >= 9 && shadowStage == 3 -> 300
                    shadowStage > 3 && questStage >= 9 || questStage >= 10 -> 400
                    else -> {
                        endFile()
                        return
                    }
                }
            }

            // Initial shop dialogue
            10 -> {
                npc("Greetings friend.", "I am Rasolo, the famous merchant.", "Would you care to see my wares?")
                stage = 11
            }

            11 -> {
                options("Yes", "No")
                stage = 12
            }

            12 -> {
                when (buttonID) {
                    1 -> {
                        openNpcShop(player!!, npc!!.id)
                        endFile()
                    }

                    2 -> {
                        playerl("No, not really.")
                        stage = 13
                    }
                }
            }

            13 -> {
                npcl("As you wish. I will travel wherever the business takes me.")
                endFile()
            }

            // Shadow Diamond introduction
            100 -> {
                npc("Greetings friend.", "I am Rasolo, the famous merchant.", "Would you care to see my wares?")
                stage = 101
            }

            101 -> {
                options("Yes", "No", "Ask about the Diamonds of Azzanadra")
                stage = 102
            }

            102 -> {
                when (buttonID) {
                    1 -> {
                        openNpcShop(player!!, npc!!.id)
                        endFile()
                    }

                    2 -> {
                        playerl("No, not really.")
                        stage = 103
                    }

                    3 -> {
                        playerl("No, actually I was looking for something specific...")
                        stage = 110
                    }
                }
            }

            103 -> {
                npcl("As you wish. I will travel wherever the business takes me.")
                endFile()
            }

            110 -> {
                npcl("Hmmmm? And what would that be?")
                stage = 111
            }

            111 -> {
                playerl("I am looking for one of the Diamonds of Azzanadra. I have reason to believe it may be somewhere around here...")
                stage = 112
            }

            112 -> {
                npcl("Ahhh... The Shadow Diamond...")
                stage = 113
            }

            113 -> {
                npcl("I know the object of which you speak. It is guarded by a fearsome warrior known as Damis, they say, who lives in the shadows, invisible to prying eyes...")
                stage = 114
            }

            114 -> {
                playerl("How can I find this 'Damis' then?")
                stage = 115
            }

            115 -> {
                npcl("Well now... perhaps we can help each other out here.")
                stage = 116
            }

            116 -> {
                npcl("I have in my possession a small trinket, a ring, that allows its wearer to see the unseen...")
                stage = 117
            }

            117 -> {
                playerl("How much do you want for it?")
                stage = 118
            }

            118 -> {
                npcl("Ah, but it is not for sale... As such...")
                stage = 119
            }

            119 -> {
                npcl("I am offering to trade it for an item that was rightfully mine, but that was stolen by a bandit named Laheeb.")
                stage = 120
            }

            120 -> {
                npcl("The item is question is a gilded cross, that has some sentimental value to myself. I wish for you to recover this item for me, and I will happily let you have my ring of visibility.")
                stage = 121
            }

            121 -> {
                playerl("Where can I find this Laheeb?")
                stage = 122
            }

            122 -> {
                npcl("Well, as a travelling merchant I have roamed these lands for many years...")
                stage = 123
            }

            123 -> {
                npcl("To the far east of here there is an area that is dry and barren like the desert... it is called...")
                stage = 124
            }

            124 -> {
                playerl("Al Kharid?")
                stage = 125
            }

            125 -> {
                npcl("...Yes, Al Kharid. Now to the south of Al Kharid there is a passageway, it is called the...")
                stage = 126
            }

            126 -> {
                playerl("Shantay Pass.")
                stage = 127
            }

            127 -> {
                npcl("...Yes. I didn't realise you had travelled there yourself.")
                stage = 128
            }

            128 -> {
                npcl("Anyway, when you have gone through the Shantay Pass, you will find yourself in a hostile desert... You will need to bring water with you to keep your life. Now, to the south-west of this pass, you will find a small")
                stage = 129
            }

            129 -> {
                npcl("village...")
                stage = 130
            }

            130 -> {
                playerl("The Bedabin camp.")
                stage = 131
            }

            131 -> {
                npc(FaceAnim.ANNOYED, "If you know where Laheeb lives, why did you ask me?")
                stage = 132
            }

            132 -> {
                playerl("I don't. Sorry, please continue.")
                stage = 133
            }

            133 -> {
                npcl("Well, okay then. Anyway, south of this encampment there is an area where few have ever been... It is a village of murderous bandits, and treacherous")
                stage = 134
            }

            134 -> {
                npcl("thieves... This is where Laheeb makes his home.")
                stage = 135
            }

            135 -> {
                npcl("He will have hidden his stolen treasure somewhere in that village, I am sure of it. When you find his loot, you will find my gilded cross. Return it to me, and I will reward you with my ring")
                stage = 136
            }

            136 -> {
                npcl("of visibility, so that you may find Damis. Does this seem fair to you?")
                stage = 137
            }

            137 -> {
                options("Yes", "No")
                stage = 138
            }

            138 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Not a problem. I'll be back with your cross before you know it.")
                        stage = 139
                    }

                    2 -> {
                        playerl("Sounds like too much effort to me. I'll find this Damis by myself.")
                        stage = 140
                    }
                }
            }

            139 -> {
                if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeShadowStage) == 0) {
                    DesertTreasure.setSubStage(player!!, DesertTreasure.attributeShadowStage, 1)
                }
                endFile()
            }

            140 -> {
                npcl("As you wish.")
                endFile()
            }

            // Gilded Cross follow-up
            200 -> {
                npcl("Have you retrieved my gilded cross for me yet?")
                stage = 201
            }

            201 -> {
                if (shadowStage == 2 && inInventory(player!!, Items.GILDED_CROSS_4674)) {
                    playerl("Yes I have!")
                    stage = 211
                } else {
                    playerl("No, not yet...")
                    stage = 221
                }
            }

            211 -> {
                npcl("Excellent, excellent. Here, take this ring. While you wear it, you will be able to see the things that live in shadows...")
                stage = 212
            }

            212 -> {
                npcl("And you will be able to find the entrance to Damis' lair.")

                if (removeItem(player!!, Items.GILDED_CROSS_4674)) {
                    if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeShadowStage) == 2) {
                        addItemOrDrop(player!!, Items.RING_OF_VISIBILITY_4657)
                        DesertTreasure.setSubStage(player!!, DesertTreasure.attributeShadowStage, 3)
                    }
                }

                endFile()
            }

            221 -> {
                npcl("Well what seems to be the problem?")
                stage = 222
            }

            222 -> {
                options(
                    "Where can I find Laheeb?",
                    "Can't I just buy your ring?",
                    "Is Damis near here, then?"
                )
                stage = 223
            }

            223 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Where can I find this Laheeb?")
                        stage = 230
                    }

                    2 -> {
                        playerl("Can't I just buy your ring?")
                        stage = 260
                    }

                    3 -> {
                        playerl("Is Damis near here, then?")
                        stage = 270
                    }
                }
            }

            // Where can I find Laheeb?
            230 -> {
                npcl("Well, as a travelling merchant I have roamed these lands for many years...")
                stage = 231
            }

            231 -> {
                npcl("To the far east of here there is an area that is dry and barren like the desert... it is called...")
                stage = 232
            }

            232 -> {
                playerl("Al Kharid?")
                stage = 233
            }

            233 -> {
                npcl("...Yes, Al Kharid. Now to the south of Al Kharid there is a passageway, it is called the...")
                stage = 234
            }

            234 -> {
                playerl("Shantay Pass.")
                stage = 235
            }

            235 -> {
                npcl("...Yes. I didn't realise you had travelled there yourself.")
                stage = 236
            }

            236 -> {
                npcl("Anyway, when you have gone through the Shantay Pass, you will find yourself in a hostile desert... You will need to bring water with you to keep your life. Now, to the south-west of this pass, you will find a small")
                stage = 237
            }

            237 -> {
                npcl("village...")
                stage = 238
            }

            238 -> {
                playerl("The Bedabin camp.")
                stage = 239
            }

            239 -> {
                npcl("If you know where Laheeb lives, why did you ask me?")
                stage = 240
            }

            240 -> {
                playerl("I don't. Sorry, please continue.")
                stage = 241
            }

            241 -> {
                npcl("Well, okay then. Anyway, south of this encampment there is an area where few have ever been... It is a village of murderous bandits, and treacherous")
                stage = 242
            }

            242 -> {
                npcl("thieves... This is where Laheeb makes his home.")
                endFile()
            }

            // Can't I just buy your ring?
            260 -> {
                npcl("No, it is not for sale. Some things are more important than money, and the return of my gilded cross is one of them.")
                endFile()
            }

            // Is Damis near here?
            270 -> {
                npcl("You would be surprised to know just how close he is...")
                endFile()
            }

            // Shadow Diamond follow-up after receiving the ring
            300 -> {
                npcl("So how goes your quest? Did you managed to find the Diamond you were looking for yet?")
                stage = 301
            }

            301 -> {
                if (inInventory(player!!, Items.RING_OF_VISIBILITY_4657)) {
                    playerl("Not yet...")
                    stage = 311
                } else {
                    playerl("I lost that ring you gave me...")
                    stage = 321
                }
            }

            311 -> {
                npcl("Well, his lair is very close to here. I suggest you look around for it.")
                endFile()
            }

            321 -> {
                npc("Then by all means, take another.", "Only a foolish merchant would give away his only stock!")
                addItem(player!!, Items.RING_OF_VISIBILITY_4657)
                endFile()
            }

            // Final shop dialogue
            400 -> {
                npcl("Many thanks for returning my heirloom to me, adventurer. Would you like to buy something?")
                stage = 401
            }

            401 -> {
                if (inInventory(player!!, Items.RING_OF_VISIBILITY_4657)) {
                    options("Yes", "No")
                } else {
                    options("Yes", "No", "I lost that ring you gave me...")
                }
                stage = 402
            }

            402 -> {
                when (buttonID) {
                    1 -> {
                        openNpcShop(player!!, npc!!.id)
                        endFile()
                    }

                    2 -> {
                        playerl("No, not really.")
                        stage = 403
                    }

                    3 -> {
                        if (!inInventory(player!!, Items.RING_OF_VISIBILITY_4657)) {
                            playerl("I lost that ring you gave me...")
                            stage = 404
                        }
                    }
                }
            }

            403 -> {
                npcl("As you wish. I will travel wherever the business takes me.")
                endFile()
            }

            404 -> {
                npcl("Then by all means, take another. Only a foolish merchant would give away his only stock!")
                addItem(player!!, Items.RING_OF_VISIBILITY_4657)
                endFile()
            }
        }
    }
}

class RasoloTradeListeners : InteractionListener {
    override fun defineListeners() {
        on(NPCs.RASOLO_1972, NPC, "trade") { player, node ->
            openNpcShop(player, node.id)
            return@on true
        }
    }
}