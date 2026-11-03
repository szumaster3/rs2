package content.region.karamja.brimhaven.dialogue

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class TrobertDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, TrobertDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean = true

    override fun newInstance(player: Player?): Dialogue = TrobertDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.TROBERT_1884)
}

private class TrobertDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        val p = player ?: return

        when (getQuestStage(p, Quests.HEROES_QUEST)) {

            0, 1 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Welcome to our Brimhaven headquarters. I'm Trobert and I'm in charge here.")
                        stage = 1
                    }

                    1 -> {
                        playerl("Pleased to meet you.")
                        stage = 2
                    }

                    2 -> {
                        npcl("Likewise.")
                        endFile()
                    }
                }
            }

            2 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Welcome to our Brimhaven headquarters. I'm Trobert and I'm in charge here.")
                        stage = 10
                    }

                    10 -> {
                        options(
                            "So can you help me get Scarface Pete's candlesticks?",
                            "Pleased to meet you."
                        )
                        stage = 11
                    }

                    11 -> {
                        when (buttonID) {
                            1 -> {
                                playerl("So can you help me get Scarface Pete's candlesticks?")
                                stage = 12
                            }

                            2 -> {
                                playerl("Pleased to meet you.")
                                stage = 20
                            }
                        }
                    }

                    12 -> {
                        npcl(
                            "Well, we have made some progress there. We know that one of the only keys to Pete's treasure room is carried by Grip, the head guard, so we thought it might be good to get close to him somehow."
                        )
                        stage = 13
                    }

                    13 -> {
                        npcl(
                            "Grip was taking on a new deputy called Hartigen, an Asgarnian Black Knight who was deserting the Black Knight Fortress and seeking new employment here on Brimhaven."
                        )
                        stage = 14
                    }

                    14 -> {
                        npcl(
                            "We managed to waylay him on the journey here, and steal his I.D. papers. Now all we need is to find somebody willing to impersonate him and take the deputy role to get that key for us."
                        )
                        stage = 15
                    }

                    15 -> {
                        options(
                            "I volunteer to undertake that mission!",
                            "Well, good luck then."
                        )
                        stage = 16
                    }

                    16 -> {
                        when (buttonID) {
                            1 -> {
                                playerl("I volunteer to undertake that mission!")
                                stage = 17
                            }

                            2 -> {
                                playerl("Well, good luck then.")
                                stage = 18
                            }
                        }
                    }

                    17 -> {
                        npcl(
                            "Good good. Well, here's the ID papers, take them and introduce yourself to the guards at Scarface Pete's mansion, we'll have that treasure in no time."
                        )
                        stage = 19
                    }

                    19 -> {
                        addItemOrDrop(p, Items.ID_PAPERS_1584)
                        if (getQuestStage(p, Quests.HEROES_QUEST) == 2) {
                            setQuestStage(p, Quests.HEROES_QUEST, 3)
                        }
                        endFile()
                    }

                    18 -> {
                        npcl("Someone will show up eventually.")
                        endFile()
                    }

                    20 -> {
                        npcl("Likewise.")
                        endFile()
                    }
                }
            }

            3, 4, 5 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        if (inInventory(p, Items.ID_PAPERS_1584)) {
                            stage = 30
                        } else {
                            stage = 40
                        }
                        handle(componentID, buttonID)
                    }

                    30 -> {
                        npcl("How's it going?")
                        stage = 31
                    }

                    31 -> {
                        playerl("Fine, thanks.")
                        endFile()
                    }

                    40 -> {
                        playerl("I have lost Hartigen's ID papers.")
                        stage = 41
                    }

                    41 -> {
                        npcl(
                            "Well, that was careless of you, wasn't it? Fortunately for you, he had a spare. Take this one, but please try to be more careful with this one."
                        )
                        stage = 42
                    }

                    42 -> {
                        addItemOrDrop(p, Items.ID_PAPERS_1584)
                        endFile()
                    }
                }
            }

            6, 100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("How's it going?")
                        stage = 50
                    }

                    50 -> {
                        playerl("Fine, thanks.")
                        endFile()
                    }
                }
            }
        }
    }
}