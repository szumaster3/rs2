package content.region.karamja.brimhaven.dialogue

import content.region.asgarnia.burthorpe.quest.hero.HeroesQuest
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Grip dialogue.
 */
@Initializable
class GripDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        openDialogue(player, GripDialogueFile(), npc)
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int, ): Boolean = true

    override fun newInstance(player: Player?): Dialogue = GripDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.GRIP_792)
}

class GripDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = if (
                    getAttribute(player!!, HeroesQuest.attributeGripTookPapers, false)
                ) {
                    100
                } else {
                    1
                }
            }

            // Initial conversation and ID papers.
            1 -> {
                playerl("Hi there. I am Hartigen, reporting for duty as your new deputy sir!")
                stage = 2
            }

            2 -> {
                npcl("Ah good, at last. You took your time getting here! Now let me see...")
                stage = 3
            }

            3 -> {
                npcl(
                    "I'll get your hours and duty roster sorted out in a while. Oh, and do you have your I.D. papers with you? Internal security is almost as important as external security for a guard."
                )
                stage = 4
            }

            4 -> {
                if (inInventory(player!!, Items.ID_PAPERS_1584)) {
                    playerl("Right here sir!")
                    stage = 11
                } else {
                    playerl("Oh, dear. I don't have that with me any more.")
                    stage = 21
                }
            }

            11 -> {
                dialogue("You hand the ID papers over to Grip.")
                stage = 12
            }

            12 -> {
                if (removeItem(player!!, Items.ID_PAPERS_1584)) {
                    setAttribute(player!!, HeroesQuest.attributeGripTookPapers, true)
                }
                stage = 100
            }

            21 -> {
                npcl("Well, that's no good! Go get them immediately, then report back for duty.")
                endFile()
            }

            // Grip's options.
            100 -> {
                val options = mutableListOf("So can I please guard the treasure room please?")

                if (!getAttribute(player!!, HeroesQuest.attributeGripSaidDuties, false)) {
                    options.add("So what do my duties involve?")
                }

                options.add("Well, I'd better sort my new room out.")

                if (getAttribute(player!!, HeroesQuest.attributeGripSaidDuties, false)) {
                    options.add("Anything I can do now?")
                }

                options(*options.toTypedArray())
                stage = 101
            }

            101 -> {
                val saidDuties = getAttribute(
                    player!!,
                    HeroesQuest.attributeGripSaidDuties,
                    false
                )

                when {
                    buttonID == 1 -> stage = 110
                    !saidDuties && buttonID == 2 -> stage = 120
                    !saidDuties && buttonID == 3 -> stage = 130
                    saidDuties && buttonID == 2 -> stage = 130
                    saidDuties && buttonID == 3 -> stage = 140
                }
            }

            // Guard the treasure room.
            110 -> {
                playerl("So can I please guard the treasure room please?")
                stage = 111
            }

            111 -> {
                npcl(
                    "Well, I might post you outside it sometimes. I prefer to be the only one allowed inside however."
                )
                stage = 112
            }

            112 -> {
                npcl(
                    "There's some pretty valuable artefacts in there! Those keys stay ONLY with the head guard and Scarface Pete."
                )
                endFile()
            }

            // Ask about duties.
            120 -> {
                setAttribute(player!!, HeroesQuest.attributeGripSaidDuties, true)
                stage = 121
            }

            121 -> {
                playerl("So what do my duties involve?")
                stage = 122
            }

            122 -> {
                npcl(
                    "You'll have various guard related duties on various shifts. I'll assign specific duties as they are required as and when they become necessary. Just so you know, if anything happens to me"
                )
                stage = 123
            }

            123 -> {
                npcl(
                    "you'll need to take over as head guard here. You'll find important keys to the treasure room and Pete's quarters inside my jacket - although I doubt anything bad's going to happen to"
                )
                stage = 124
            }

            124 -> {
                npcl("me anytime soon!")
                stage = 125
            }

            125 -> {
                dialogue("Grip laughs to himself at the thought.")
                endFile()
            }

            // Sort out the new room.
            130 -> {
                playerl("Well, I'd better sort my new room out.")
                stage = 131
            }

            131 -> {
                npcl(
                    "Yeah, I'll give you time to settle in. Better get a good night's sleep, I expect you to report for duty at oh five hundred hours tomorrow on the dot!"
                )
                endFile()
            }

            // Anything I can do now?
            140 -> {
                playerl("Anything I can do now?")
                stage = 141
            }

            141 -> {
                if (inInventory(player!!, Items.MISCELLANEOUS_KEY_1586)) {
                    npcl("Can't think of anything right now.")
                    endFile()
                } else {
                    npcl(
                        "Hmm. Well, you could find out what this key opens for me. Apparently it's for something in this building, but for the life of me I can't find what."
                    )
                    stage = 144
                }
            }

            144 -> {
                dialogue("Grip hands you a key.")
                stage = 145
            }

            145 -> {
                addItemOrDrop(player!!, Items.MISCELLANEOUS_KEY_1586)
                endFile()
            }
        }
    }
}