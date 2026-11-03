
package content.region.morytania.canifis.quest.fenk.dialogue

import content.region.morytania.canifis.quest.fenk.npc.GardenerGhostNPC
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class GardenerGhostDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        val gardenerGhost = npc as GardenerGhostNPC

        if (gardenerGhost.target == player) {
            if (gardenerGhost.location.withinDistance(gardenerGhost.graveLocation, 5)) {
                sendChat(gardenerGhost, "Here is the place where I met me' maker.")
            } else {
                sendChat(
                    gardenerGhost,
                    "Go ${gardenerGhost.location.deriveDirection(gardenerGhost.graveLocation)!!.name.lowercase().replace('_', '-')}, mate",
                )
                gardenerGhost.continueFollowing(player)
            }
            return true
        }

        if (!player.equipment.containsAtLeastOneItem(Items.GHOSTSPEAK_AMULET_552)) {
            npc(FaceAnim.OLD_DEFAULT, "Wooo wooo wooooo.")
            stage = END_DIALOGUE
            return true
        }

        val questStage = getQuestStage(player, Quests.CREATURE_OF_FENKENSTRAIN)

        when {
            questStage == 4 -> {
                options("Tell me about Fenkenstrain.", "Do you know where the key to the shed is?", "Do you know where I can find a lightning conductor mould?", "What happened to your head?")
            }

            questStage < 4 -> {
                options("Tell me about Fenkenstrain.", "What happened to your head?", "What's your name?")
            }

            else -> {
                options("Tell me about Fenkenstrain.", "What happened to your head?")
            }
        }

        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val gardenerGhost = npc as GardenerGhostNPC

        when (stage) {
            0 -> {
                val questStage = getQuestStage(player, Quests.CREATURE_OF_FENKENSTRAIN)

                when {
                    questStage == 4 -> {
                        when (buttonId) {
                            1 -> {
                                playerl(FaceAnim.HALF_ASKING,"Can you tell me anything about Fenkenstrain?")
                                stage = 10
                            }

                            2 -> {
                                sendItemDialogue(player, Items.GHOSTSPEAK_AMULET_552, "You feel power emanate from the Amulet of Ghostspeak and the air around you vibrates with the ghostly voice of the headless gardener.")
                                stage = 20
                            }

                            3 -> {
                                sendItemDialogue(player, Items.GHOSTSPEAK_AMULET_552, "You feel power emanate from the Amulet of Ghostspeak and the air around you vibrates with the ghostly voice of the headless gardener.")
                                stage = 30
                            }

                            4 -> {
                                playerl(FaceAnim.HALF_ASKING,"What happened to your head?")
                                stage = 40
                            }
                        }
                    }

                    questStage < 4 -> {
                        when (buttonId) {
                            1 -> {
                                playerl(FaceAnim.HALF_ASKING,"Can you tell me anything about Fenkenstrain?")
                                stage = 10
                            }

                            2 -> {
                                playerl(FaceAnim.HALF_ASKING,"What happened to your head?")
                                stage = 40
                            }

                            3 -> {
                                playerl(FaceAnim.HALF_ASKING,"What's your name?")
                                stage = 50
                            }
                        }
                    }

                    else -> {
                        when (buttonId) {
                            1 -> {
                                playerl(FaceAnim.HALF_ASKING,"Can you tell me anything about Fenkenstrain?")
                                stage = 10
                            }

                            2 -> {
                                playerl(FaceAnim.HALF_ASKING,"What happened to your head?")
                                stage = 40
                            }
                        }
                    }
                }
            }

            // Fenkenstrain
            10 -> {
                npc(FaceAnim.OLD_DEFAULT, "Oi could tell you a few things about old Fenky, yeah.")
                stage++
            }

            11 -> {
                playerl(FaceAnim.NEUTRAL,"Go on.")
                stage++
            }

            12 -> {
                npc(FaceAnim.OLD_DEFAULT, "Once, this castle were full o' good folk - my friends. Fenky was just the castle doctor, you know, to the lord and the castle folk. I don't know what happened to them all, but one by one they all disappeared.")
                stage++
            }

            13 -> {
                npc(
                    FaceAnim.OLD_DEFAULT,
                    "When they were gone a while, I went an dug graves for 'em in the forest. After a while there weren't no one left, but the lord, Fenkenstrain and meself.",
                )
                stage++
            }

            14 -> {
                npc(
                    FaceAnim.OLD_DEFAULT,
                    "Old Fenky sent me into the forest to dig 'im a pit - never said what for - then would you believe it, someone chops me 'ead off.",
                )
                stage++
            }

            15 -> {
                playerl(FaceAnim.HALF_ASKING,"Did you see who did it...before...?")
                stage++
            }

            16 -> {
                npc(FaceAnim.OLD_DEFAULT, "Before oi kicked the bucket, you mean?")
                stage++
            }

            17 -> {
                playerl(FaceAnim.THINKING,"Umm...")
                stage++
            }

            18 -> {
                npc(
                    FaceAnim.OLD_DEFAULT,
                    "Don't worry yerself. I'm not worried about bein' dead. Worse things could happen, I suppose.",
                )
                stage++
            }

            19 -> {
                npc(
                    FaceAnim.OLD_DEFAULT,
                    "One thing I do know is, there ain't no lord of the castle anymore, 'cept for old Fenky. Makes ya think a bit, don't it?",
                )
                stage = END_DIALOGUE
            }

            // Shed key
            20 -> {
                npc(FaceAnim.OLD_DEFAULT, "Got it right 'ere in my pocket. Here you go.")
                stage++
            }

            21 -> {
                sendItemDialogue(player, Items.SHED_KEY_4186, "The headless gardener hands you a rusty key.")
                stage++
            }

            22 -> {
                end()
                addItemOrDrop(player, Items.SHED_KEY_4186)
                stage = END_DIALOGUE
            }

            // Lightning conductor mould
            30 -> {
                npc(FaceAnim.OLD_DEFAULT, "A conductor mould, you say? Let me see...")
                stage++
            }

            31 -> {
                npc(FaceAnim.OLD_DEFAULT, "There used to be a bloke 'ere, sort of an 'andyman 'e was. Did everything 'round the place - fixed what was broke, swept the chimneys and the like. He would 'ave had a mould, I imagine.")
                stage++
            }

            32 -> {
                playerl(FaceAnim.HALF_ASKING,"Where is he now?")
                stage++
            }

            33 -> {
                npc(FaceAnim.OLD_DEFAULT, "E's dead, just like everyone else round 'ere... 'cept for me.")
                stage = END_DIALOGUE
            }

            // What happened to your head?
            40 -> {
                sendItemDialogue(player, Items.GHOSTSPEAK_AMULET_552, "You feel power emanate from the Amulet of Ghostspeak and the air around you vibrates with the ghostly voice of the headless gardener.")
                stage++
            }

            41 -> {
                npc(FaceAnim.OLD_DEFAULT, "Oi was in the old 'aunted Forest to the south, diggin' a pit for moi old master, old Fenkenstrain, when would you believe it, someone chops me head off. Awful bad luck weren't it?")
                stage++
            }

            42 -> {
                playerl(FaceAnim.NEUTRAL,"Oh yes, dreadful bad luck.")
                stage++
            }

            43 -> {
                npc(FaceAnim.OLD_DEFAULT, "So oi thinks to meself, I don't needs any 'ead to be getting on with me gardenin', long as I got me hands and me spade.")
                stage++
            }

            44 -> {
                playerl(FaceAnim.HALF_ASKING,"Would you show me where the place was?")
                stage++
            }

            45 -> {
                npc(FaceAnim.OLD_DEFAULT, "Well, oi s'pose oi've got ten minutes to spare.")
                stage++
            }

            46 -> {
                end()
                gardenerGhost.startFollowing(player)
                stage = END_DIALOGUE
            }

            // What's your name?
            50 -> {
                sendItemDialogue(player, Items.GHOSTSPEAK_AMULET_552, "You feel power emanate from the Amulet of Ghostspeak and the air around you vibrates with the ghostly voice of the headless gardener.")
                stage++
            }

            51 -> {
                npc(FaceAnim.OLD_DEFAULT, "Me name? It's been a moivellous long while, mate, since I had any use for such a thing as a name.")
                stage++
            }

            52 -> {
                playerl(FaceAnim.FRIENDLY,"Don't worry, I was just trying to make conversation.")
                stage++
            }

            53 -> {
                npc(FaceAnim.OLD_DEFAULT, "No, no, I can't be havin' that. I'll remember in a minute...")
                stage++
            }

            54 -> {
                playerl(FaceAnim.NEUTRAL,"Please, don't worry.")
                stage++
            }

            55 -> {
                npc(FaceAnim.OLD_DEFAULT, "Lestwit, that's it! Ed Lestwit.")
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun newInstance(player: Player?): Dialogue = GardenerGhostDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.GARDENER_GHOST_1675)
}