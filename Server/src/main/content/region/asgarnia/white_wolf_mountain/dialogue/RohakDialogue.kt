package content.region.asgarnia.white_wolf_mountain.dialogue

import content.data.Quests

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.item.Item
import core.game.world.map.Location
import core.tools.END_DIALOGUE
import shared.consts.Items

/**
 * Represents the Rohak dialogue.
 */
class RohakDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> if(!hasRequirement(player!!, Quests.RECIPE_FOR_DISASTER.questName, false)) {
                npcl(FaceAnim.OLD_DEFAULT, "Hello there, youngster.").also { stage++ }
            } else {
                npcl(FaceAnim.OLD_DEFAULT, "Hello again, youngster.").also { stage = 5 }
            }
            1 -> player(FaceAnim.ASKING, "Who are you calling youngster??").also { stage++ }
            2 -> npcl(FaceAnim.OLD_DEFAULT, "Why you. Unless you have seen the small village of Falador grow into a heaving city like I have of course!").also { stage++ }
            3 -> player(FaceAnim.HALF_THINKING, "Err... no...").also { stage++ }
            4 -> npcl(FaceAnim.OLD_HAPPY, "Well then! Enjoy your stay in our mountain home.").also { stage = END_DIALOGUE }

            5 -> showTopics(
                Topic("About your son...", 6, true),
                Topic("Can I have another rock cake?", 8)
            )

            6 -> player("How's your son getting on, now that I've rescued him from the time bubble and the culinaromancer?").also { stage++ }
            7 -> npcl(FaceAnim.OLD_DEFAULT, "My son's fine. I'm not so sure about you, though. Maybe you should lay down in a dark room until the babbling stops.").also { stage = END_DIALOGUE }
            8 -> npcl(FaceAnim.OLD_HAPPY, "Aye, I've still got enough ingredients from before, but I'll want 100 coins.").also { stage++ }
            9 -> showTopics(
                Topic("Okay, 100 coins will be fine.", 10, true),
                Topic("No thanks.", 11),
            )
            10 -> {
                end()
                if(!removeItem(player!!, Item(Items.COINS_995, 100))) {
                    sendMessage(player!!, "You can not afford that.")
                    stage = END_DIALOGUE
                } else {
                    sendItemDialogue(player!!, Items.DWARVEN_ROCK_CAKE_7510, "You hand over 100 gold and Rohak bakes you a rock cake.")
                    produceGroundItem(player, Items.DWARVEN_ROCK_CAKE_7510, 1, Location(2867, 9878, 0))
                    stage = END_DIALOGUE
                }
            }
            11 -> npcl(FaceAnim.OLD_DEFAULT, "I'm not doing it for less, even for a mate.").also { stage = END_DIALOGUE }
        }
    }
}
