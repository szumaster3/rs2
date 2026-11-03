package content.region.desert.quest.deserttreasure

import core.api.addItemOrDrop
import core.api.getQuestStage
import core.api.inInventory
import core.api.openDialogue
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Troll Child dialogue.
 */
@Initializable
class TrollChildDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, TrollChildDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return TrollChildDialogue(player)
    }

    override fun getIds(): IntArray {
        // NPCs.BANDIT_1932 is wrong, should be NPCs.TROLL_CHILD_1932
        // NPCs.TROLL_CHILD_1933, NPCs.TROLL_CHILD_1934 are varbit controlled 1932 instances.
        return intArrayOf(NPCs.BANDIT_1932)
    }
}

class TrollChildDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, DesertTreasure.questName)
        val iceStage = DesertTreasure.getSubStage(player!!, DesertTreasure.attributeIceStage)

        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    questStage in 0..8 -> 1
                    questStage == 9 && iceStage == 0 -> 10
                    questStage == 9 && iceStage == 1 -> 20
                    questStage == 9 && iceStage in 2..4 -> 50
                    questStage == 9 && iceStage == 100 -> 70
                    else -> {
                        endFile()
                        return
                    }
                }
            }

            // Before quest crying
            1 -> {
                playerl("Hello there.")
                stage = 2
            }

            2 -> {
                dialogue(
                    "The troll child is crying to itself.",
                    "It is ignoring you completely."
                )
                endFile()
            }

            // Still crying
            10 -> {
                playerl("Hello there.")
                stage = 11
            }

            11 -> {
                npcl(FaceAnim.OLD_NEARLY_CRYING, "Waaaaaaa!")
                stage = 12
            }

            12 -> {
                dialogue(
                    "This troll seems very upset about something.",
                    "Maybe some sweet food would take his mind off things?"
                )
                endFile()
            }

            // Stopped crying
            20 -> {
                playerl("Hello there.")
                stage = 21
            }

            21 -> {
                npc(FaceAnim.OLD_SAD, "-sniff-", "H-hello there.")
                stage = 22
            }

            22 -> {
                playerl("Why so sad, little troll?")
                stage = 23
            }

            23 -> {
                npc(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "It was the bad man!",
                    "He hurt my mommy and daddy!",
                    "He made them all freezey!"
                )
                stage = 24
            }

            24 -> {
                playerl("Bad man...?")
                stage = 25
            }

            25 -> {
                npcl(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "He said it was because they stole his diamond! But they never did! They found it, and didn't know who it belonged to!"
                )
                stage = 26
            }

            26 -> {
                npcl(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "My mommy always told me stealing is wrong, they would never steal from someone!"
                )
                stage = 27
            }

            27 -> {
                npcl(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "Then he did some wavey hand thing and my mommy and daddy got frozified!"
                )
                stage = 28
            }

            28 -> {
                playerl("A diamond you say? Listen, I think I might be able to help your parents, but I need that Diamond in return.")
                stage = 29
            }

            29 -> {
                npcl(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "-sniff- I don't think they really wanted it anyway, they would have given it back to the bad man if he'd asked before freezifying them..."
                )
                stage = 30
            }

            30 -> {
                npcl(
                    FaceAnim.OLD_NEARLY_CRYING,
                    "I give you my promise mister that if you unfreeze my mommy and daddy, you can have the stupid diamond."
                )
                stage = 31
            }

            31 -> {
                npcl(FaceAnim.OLD_NEARLY_CRYING, "Do we have a deal?")
                stage = 32
            }

            32 -> {
                options("Yes", "No")
                stage = 33
            }

            33 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Absolutely. Don't worry kid, I'll get your parents back to you safe and sound.")
                        stage = 34
                    }

                    2 -> {
                        playerl("Sorry, I can't make any promises about that, and I don't think I have the time to waste trying to defrost some stupid ice trolls.")
                        stage = 35
                    }
                }
            }

            // Yes to help
            34 -> {
                if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeIceStage) == 1) {
                    DesertTreasure.setSubStage(player!!, DesertTreasure.attributeIceStage, 2)
                }
                endFile()
            }

            // No to help
            35 -> {
                npcl(FaceAnim.OLD_NEARLY_CRYING, "Waaaaaaa!")
                endFile()
            }

            // Have you freed them?
            50 -> {
                npcl(FaceAnim.OLD_SAD, "You didn't free my mommy and daddy yet?")
                stage = 51
            }

            51 -> {
                player("Not yet...")
                stage = 52
            }

            52 -> {
                npc(FaceAnim.OLD_SAD, "Please try harder!", "I love my mommy and daddy!")
                endFile()
            }

            70 -> {
                if (!inInventory(player!!, Items.ICE_DIAMOND_4671)) {
                    playerl("I lost that diamond of Ice you gave me...")
                    stage = 72
                } else {
                    npc(
                        FaceAnim.OLD_CALM_TALK1,
                        "Thanks for all of your help!",
                        "I'm surprised you managed to survive the blizzard,",
                        "being a thin skinned fleshy and all!"
                    )
                    stage = 75
                }
            }

            72 -> {
                npcl(
                    FaceAnim.OLD_CALM_TALK1,
                    "That's okay, it blew back on an icy wind... It's almost like it wants to stay here! Here, take it back, you've earned it."
                )
                stage = 73
            }

            73 -> {
                addItemOrDrop(player!!, Items.ICE_DIAMOND_4671)
                endFile()
            }

            75 -> {
                player("What can I say?", "I'm a lot tougher than I look.")
                endFile()
            }
        }
    }
}