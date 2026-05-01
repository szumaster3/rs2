package content.region.kandarin.gnome_stronghold.quest.makinghistory.cutscene

import content.region.kandarin.gnome_stronghold.quest.makinghistory.MHUtils
import core.api.*
import core.game.activity.Cutscene
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import core.tools.END_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

// TODO: Correct camera movement.
class OutpostHistoryCutscene(player: Player) : Cutscene(player) {

    override fun setup() {
        setExit(player.location.transform(0, 0, 0))
        if (player.settings.isRunToggled) {
            player.settings.toggleRun()
        }
        loadRegion(9780)
        addNPC(NPCs.JORRAL_2932, 5, 19, Direction.WEST)
        addNPC(NPCs.HOBGOBLIN_123, 12, 30, Direction.WEST)
        addNPC(NPCs.HOBGOBLIN_122, 13, 31, Direction.SOUTH)
        addNPC(NPCs.HOBGOBLIN_123, 15, 33, Direction.EAST)
    }

    override fun runStage(stage: Int) {
        when (stage) {
            0 -> {
                fadeToBlack()
                timedUpdate(6)
            }

            1 -> {
                teleport(player, 4, 19)
                face(player, getNPC(NPCs.JORRAL_2932)!!)
                sendDialogue(player, "With many occupants over the years,")
                moveCamera(0, 28, 300, 1)
                fadeFromBlack()
                timedUpdate(3)
            }

            2 -> {
                moveCamera(0, 23, 300, 1)
                timedUpdate(3)
            }

            3 -> {
                moveCamera(4, 19, 300, 1)
                rotateCamera(4, 19, 300, 1)
                timedUpdate(1)
            }
            4 -> dialogueUpdate(true, "the building has seen much action.")
            5 -> {
                moveCamera(9, 24, 300, 1)
                rotateCamera(5, 16, 300, 1)
                timedUpdate(3)
            }

            6 -> {
                moveCamera(9, 24, 300, 1)
                rotateCamera(4, 30, 500, 1)
                timedUpdate(3)
            }

            7 -> dialogueUpdate(true, "It started life as an outpost.")
            8 -> {
                moveCamera(0, 7, 400, 1)
                rotateCamera(4, 19, 400, 1)
                timedUpdate(8)
            }
            9 -> dialogueUpdate(true, "Its sole purpose to see incoming armies,")
            10 -> {
                moveCamera(13, 31, 400, 1)
                rotateCamera(13, 31, 400, 1)
                timedUpdate(4)
            }

            11 -> {
                move(getNPC(NPCs.HOBGOBLIN_122)!!, 12, 29)
                dialogueUpdate(true, "before they saw the city of Ardougne.")
            }

            12 -> {
                setQuestStage(player, Quests.MAKING_HISTORY, 1)
                setVarbit(player, MHUtils.PROGRESS, 1, true)
                end {
                    openDialogue(player, OutpostHistoryDialogue())
                }
            }
        }
    }
}

private class OutpostHistoryDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.JORRAL_2932)
        when (stage) {
            0 -> npc(FaceAnim.NEUTRAL, "If all goes well, I hope to be able to turn it into a", "museum as a monument to the area's history. What do", "you think?").also { stage++ }
            1 -> options("Ok, I'll make a stand for history!", "I don't care about some dusty building").also { stage++ }
            2 -> when (buttonID) {
                1 -> playerl(FaceAnim.HALF_GUILTY, "OK, I will make a stand for history!").also { stage++ }
                2 -> playerl(FaceAnim.HALF_GUILTY, "I don't care about some dusty building").also { stage = 100 }
            }
            3 -> npcl(FaceAnim.HAPPY, "Oh, thank you so much, you really are my saviour!").also { stage++ }
            4 -> player("But where should I start?").also { stage++ }
            5 -> player("Wht do I need to do now?").also { stage = 24 }

            24 -> npcl(FaceAnim.HALF_GUILTY, "There are three people that may be able to help:").also { stage++ }
            25 -> options("A trader in Ardougne", "A ghost in Port Phasmatys", "A warrior in Rellekka").also { stage++ }
            26 -> when (buttonID) {
                1 -> npc("There is a silver trader in East Ardougne called Erin,", "who I believe can help.").also { stage++ }
                2 -> npc("I've been told that there's a ghost far off in Port", "Phasmatys that moans of losing his life to this place.").also { stage = 30 }
                3 -> npc("Up near the mountains, in Rellekka there is a warrior called", "Dron whom I have spoken to in the past. He is always", "on the lookout for information that can improve his", "fighting and commanding skills.").also { stage = 35 }
            }

            27 -> playerl(FaceAnim.HALF_GUILTY, "In what way can he help?").also { stage++ }
            28 -> npc("His Great Grandfather lived in this outpost according to", "the records. He must know something!").also { stage++ }
            29 -> playerl(FaceAnim.HALF_GUILTY, "OK, I'll see what he has to say.").also { stage = 41 }
            30 -> playerl(FaceAnim.HALF_GUILTY, "Sounds ominous. Does he have a name?").also { stage++ }
            31 -> npcl(FaceAnim.HALF_GUILTY, "He does indeed. It's Droalak.").also { stage++ }
            32 -> playerl(FaceAnim.HALF_GUILTY, "I'll track him down.").also { stage++ }
            33 -> npc("It might not be so simple! You'll need an amulet of", "ghostspeak to talk to him!").also { stage++ }
            34 -> player(FaceAnim.HALF_GUILTY, "I've conversed with the undead before, that shouldn't be", "too much of a problem.").also { stage = 41 }
            35 -> player("And how's he related to this outpost?").also { stage++ }
            36 -> npc("He isn't directly, but he's studied many wars, and as", "this used to be an outpost it should have been involved", "in some war.").also { stage++ }
            37 -> player("That sounds simple enough.").also { stage++ }
            38 -> npc("He isn't the easiest person to talk tom so you may need", "to speak to his brother, Blanin, first.").also { stage = 41 }

            41 -> options("What's the story so far?", "What do I need to do now?", "Got to go, bye!").also { stage++ }
            42 -> when (buttonID) {
                1 -> player(FaceAnim.HALF_ASKING,"What's the story so far?").also { stage++ }
                2 -> player(FaceAnim.HALF_ASKING,"What do I need to do now?").also { stage = 24 }
                3 -> player(FaceAnim.HAPPY,"Got to go, bye!").also { stage = END_DIALOGUE }
            }
            43 -> npcl(FaceAnim.ANNOYED, "What do you mean? You've discovered nothing!").also { stage = END_DIALOGUE }
            100 -> npc("It's doomed. DOOMED!").also { stage = END_DIALOGUE }
        }
    }
}

