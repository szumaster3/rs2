
package content.region.asgarnia.burthorpe.quest.hero.dialogue

import core.api.*
import core.game.dialogue.DialogueFile
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.Quests

/**
 * Represents the Straven dialogue file.
 *
 * # Relations
 * - [Heroes Quest][content.region.asgarnia.burthorpe.quest.hero.HeroesQuest]
 */
class StravenDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when (getQuestStage(player!!, Quests.HEROES_QUEST)) {
                    1 -> {
                        player("How would I go about getting a Master Thief armband?")
                        stage = 1
                    }

                    2, 3, 4 -> {
                        player("What am I supposed to be doing again?")
                        stage = 10
                    }

                    5 -> {
                        if (inInventory(player!!, Items.PETES_CANDLESTICK_1577)) {
                            player("I have retrieved a candlestick!")
                            stage = 20
                        } else {
                            player("What am I supposed to be doing again?")
                            stage = 10
                        }
                    }

                    6 -> {
                        player("I'm afraid I've lost my master thief's armband.")
                        stage = 30
                    }
                }
            }

            // Quest stage 1.
            1 -> {
                npc("Ooh... tricky stuff. Took me YEARS to get that rank.")
                stage = 2
            }

            2 -> {
                npc(
                    "Well, what some of the more aspiring thieves in our gang are working on right now is to steal some very valuable candlesticks from Scarface Pete - the pirate leader on Karamja."
                )
                stage = 3
            }

            3 -> {
                npc(
                    "His security is excellent, and the target very valuable so that might be enough to get you the rank."
                )
                stage = 4
            }

            4 -> {
                npc("Go talk to our man Alfonse, the waiter in the Shrimp and Parrot.")
                stage = 5
            }

            5 -> {
                npc("Use the secret word 'gherkin' to show you're one of us.")
                stage = 6
            }

            6 -> {
                if (getQuestStage(player!!, Quests.HEROES_QUEST) == 1) {
                    setQuestStage(player!!, Quests.HEROES_QUEST, 2)
                }
                endFile()
            }

            // Quest stages 2, 3 and 4.
            10 -> {
                npc("You told me you wanted to get a Master thief's armband! Now pay attention.")
                stage = 11
            }

            11 -> {
                npc(
                    "Some of the more aspiring thieves in our gang are working on right now is to steal some very valuable candlesticks from Scarface Pete - the pirate leader on Karamja."
                )
                stage = 12
            }

            12 -> {
                npc(
                    "His security is excellent, and the target very valuable so that might be enough to get you the rank."
                )
                stage = 13
            }

            13 -> {
                npc("Go talk to our man Alfonse, the waiter in the Shrimp and Parrot.")
                stage = 14
            }

            14 -> {
                npc("Use the secret word 'gherkin' to show you're one of us.")
                stage = 100
            }

            // Quest stage 5, candlestick obtained.
            20 -> {
                npc("Hmmm. Not bad, not bad. Let's see it, make sure it's genuine.")
                stage = 21
            }

            21 -> {
                dialogue("You hand Straven the candlestick.")
                stage = 22
            }

            22 -> {
                player("So is this enough to get me a Master Thief armband?")
                stage = 23
            }

            23 -> {
                npc("Hmm... I dunno... Aww, go on then. I suppose I'm in a generous mood today.")
                stage = 24
            }

            24 -> {
                dialogue("Straven hands you a Master Thief armband.")
                stage = 25
            }

            25 -> {
                if (removeItem(player!!, Items.PETES_CANDLESTICK_1577)) {
                    addItemOrDrop(player!!, Items.THIEVES_ARMBAND_1579)

                    if (getQuestStage(player!!, Quests.HEROES_QUEST) == 5) {
                        setQuestStage(player!!, Quests.HEROES_QUEST, 6)
                    }
                }
                endFile()
            }

            // Quest stage 6.
            30 -> {
                npc("Lucky for you I have a spare. Don't lose it again!")
                stage = 31
            }

            31 -> {
                addItemOrDrop(player!!, Items.THIEVES_ARMBAND_1579)
                endFile()
            }

            100 -> endFile()
        }
    }
}
