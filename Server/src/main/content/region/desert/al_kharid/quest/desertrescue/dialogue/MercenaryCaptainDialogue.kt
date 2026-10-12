package content.region.desert.al_kharid.quest.desertrescue.dialogue

import core.api.getQuestStage
import core.game.dialogue.Dialogue
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import shared.consts.NPCs
import shared.consts.Quests

class MercenaryCaptainDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        when (getQuestStage(player, Quests.THE_TOURIST_TRAP)) {
            11 -> interpreter.sendDialogue("You approach the Mercenary Captain.")
            else -> npc("What are you doing here?")
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (getQuestStage(player, Quests.THE_TOURIST_TRAP)) {
            11 -> when (stage) {
                0 -> {
                    player("Wow! A real captain!")
                    stage++
                }

                1 -> {
                    npc("Be off effendi, you are not wanted around here.")
                    stage++
                }

                2 -> {
                    player("I'd love to work for a tough guy like you!")
                    stage++
                }

                3 -> {
                    npc("Hmmm, oh yes, what can you do?")
                    stage++
                }

                4 -> {
                    player("Can't I do something for a strong Captain like you?")
                    stage++
                }

                5 -> {
                    interpreter.sendDialogue("The Captain ponders a moment and then looks at you critically.")
                    stage++
                }

                6 -> {
                    npc("You could bring me the head of Al Zaba Bhasim.")
                    stage++
                }

                7 -> {
                    npc(
                        "He is the leader of the notorious desert bandits, they",
                        "plague us daily. You should find them west of here.",
                        "You should have no problem in finishing them all off.",
                        "Do this for me and maybe I will consider helping you.",
                    )
                    stage++
                }

                8 -> {
                    player("Sorry Sir, I don't think I can do that.")
                    stage++
                }

                9 -> {
                    npc(
                        "Hmm, well yes, I did consider that you might not be",
                        "right for the job. be off with you then before I turn",
                        "my men loose on you.",
                    )
                    stage++
                }

                10 -> {
                    player("It's a funny captain who can't fight his own battles!")
                    stage++
                }

                11 -> {
                    interpreter.sendDialogue(
                        "The men around you fall silent and the Captain silently fumes. All",
                        "eyes turn to the Captain...",
                    )
                    stage++
                }

                12 -> {
                    npc("Very well, if you're challenging me, let's get on with it!")
                    stage++
                }

                13 -> {
                    interpreter.sendDialogue("The guards gather around to watch the fight.")
                    stage++
                }

                14 -> {
                    end()
                    npc.properties.combatPulse.attack(player)
                }
            }

            else -> when (stage) {
                0 -> {
                    player("Nothing, just passing by.")
                    stage++
                }

                1 -> end()
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MERCENARY_CAPTAIN_830)
}
