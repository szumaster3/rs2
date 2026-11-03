
package content.region.morytania.canifis.quest.fenk.dialogue

import core.api.getQuestStage
import core.api.setQuestStage
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class LordRologarthDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        when (getQuestStage(player, Quests.CREATURE_OF_FENKENSTRAIN)) {
            6 -> {
                playerl(FaceAnim.ANGRY,"I am commanded to destroy you, creature!")
                stage = 0
            }
            7 -> {
                playerl(FaceAnim.THINKING, "Do you know how I can stop Fenkenstrain's experiments?")
                stage = 30
            }
            8, 100 -> {
                npc(FaceAnim.OLD_DEFAULT, "How goes it, friend?")
                stage = 50
            }
            else -> return false
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            // Confronting Lord Rologarth
            0 -> {
                npc(FaceAnim.OLD_DEFAULT, "Oh that's *hic* not very *hic* nice ...")
                stage++
            }

            1 -> {
                playerl(FaceAnim.HALF_ASKING,"Are you feeling ok?")
                stage++
            }

            2 -> {
                npc(FaceAnim.OLD_DEFAULT, "Abso *hic* lutely. Never *buuurrp* better.")
                stage++
            }

            3 -> {
                playerl(FaceAnim.HALF_THINKING,"You don't look very dangerous.")
                stage++
            }

            4 -> {
                npc(FaceAnim.OLD_DEFAULT, "How *hic* do I look?")
                stage++
            }

            5 -> {
                playerl(FaceAnim.HALF_ASKING,"You really don't know, do you? Have a look for yourself.")
                stage++
            }

            6 -> {
                sendDialogue("The creature stumbles over towards the mirror, focuses upon his", "reflection and...")
                stage++
            }

            7 -> {
                npcl(FaceAnim.OLD_DEFAULT, "AAAAARRGGGGHHHH!")
                stage++
            }

            8 -> {
                sendDialogue("The creature becomes instantly sober, horror all too evident in his", "undead eyes.")
                stage++
            }

            9 -> {
                playerl(FaceAnim.SAD,"I'm sorry. I suppose I'm partly to blame for this.")
                stage++
            }

            10 -> {
                npcl(FaceAnim.OLD_DEFAULT, "No - it was him I wager - Fenkenstrain - wasn't it? He's brought me back to life!")
                stage++
            }

            11 -> {
                playerl(FaceAnim.HALF_ASKING,"Who are - were - you?")
                stage++
            }

            12 -> {
                npcl(FaceAnim.OLD_DEFAULT, "I was Rologarth, Lord of the North Coast - this castle was once mine. Fenkenstrain was the castle doctor.")
                stage++
            }

            13 -> {
                playerl(FaceAnim.HALF_ASKING,"So the castle wasn't really abandoned when he found it?")
                stage++
            }

            14 -> {
                npcl(FaceAnim.OLD_DEFAULT, "Is that what he told you? No, no, this castle was once full of people and life. Fenkenstrain advised me to sell them to the vampyres, which - I am sad to say - I did.")
                stage++
            }

            15 -> {
                playerl(FaceAnim.NEUTRAL,"I found your brain in a jar in Canifis, so he must have sold you too.")
                stage++
            }

            16 -> {
                npcl(FaceAnim.OLD_DEFAULT, "Of that I will not speak. There lie memories that should rest with the dead, the living unable to bear them.")
                stage++
            }

            17 -> {
                playerl(FaceAnim.NEUTRAL, "That's it - I'm leaving this dreadful place, whether I get paid or not. Is there anything I can do for you before I leave?")
                stage++
            }

            18 -> {
                npcl(FaceAnim.OLD_DEFAULT, "Only one - please stop Fenkenstrain from carrying on his experiments, once and for all, so that no other poor soul has to endure suffering such as that of my people and I.")
                stage++
            }

            19 -> {
                end()
                if (getQuestStage(player, Quests.CREATURE_OF_FENKENSTRAIN) == 6) {
                    setQuestStage(player, Quests.CREATURE_OF_FENKENSTRAIN, 7)
                }
                stage = END_DIALOGUE
            }

            // The Ring of Charos
            30 -> {
                npcl(FaceAnim.OLD_DEFAULT, "Take the Ring of Charos from him.")
                stage++
            }

            31 -> {
                playerl(FaceAnim.THINKING, "What is this ring?")
                stage++
            }

            32 -> {
                npcl(FaceAnim.OLD_DEFAULT, "It was my birthright, passed down to me through the ages, its origin forgotten.")
                stage++
            }

            33 -> {
                npcl(FaceAnim.OLD_DEFAULT, "The Ring of Charos has many powers, but Fenkenstrain has bent them to his down evil purposes. Without the power of the ring, he will not be able to raise the dead from their sleep.")
                stage++
            }

            34 -> {
                npcl(FaceAnim.OLD_DEFAULT, "It has one other, extremely important use - it confuses the werewolves' senses, making them believe that they smell one of their own kind. Without the ring, Fenkenstrain will be at their mercy.")
                stage = END_DIALOGUE
            }

            // After stealing the Ring of Charos
            50 -> {
                playerl(FaceAnim.FRIENDLY,"I stole the Ring of Charos from Fenkenstrain.")
                stage++
            }

            51 -> {
                npcl(FaceAnim.OLD_DEFAULT, "I saw him climb up into the Tower to hide. It doesn't matter - soon the werewolves will come for him, and his experiments will be forever ceased.")
                stage++
            }

            52 -> {
                playerl(FaceAnim.THINKING, "Do you want the ring back? It is yours after all.")
                stage++
            }

            53 -> {
                npcl(FaceAnim.OLD_DEFAULT, "No, you keep it, my friend. Werewolves hunger for the scent of live flesh - I have no need for the ring. I have my castle back, if not my soul.")
                stage = END_DIALOGUE
            }
        }

        return true
    }

    override fun newInstance(player: Player?): Dialogue = LordRologarthDialogue(player)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.DR_FENKENSTRAIN_1671,
        NPCs.DR_FENKENSTRAIN_1672,
        NPCs.FENKENSTRAINS_MONSTER_1673,
    )
}