package content.region.kandarin.gnome_stronghold.quest.itgronigen.npc

import core.api.sendChat
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.tools.RandomFunction
import shared.consts.NPCs

class LostGoblinNPC : NPCBehavior(NPCs.OBSERVATORY_GOBLIN_PATROL1_6125,NPCs.OBSERVATORY_GOBLIN_PATROL2_6126){

    private val randomDialogue = arrayOf(
    "Which way should I go?",
    "These dungeons are such a maze.",
    "Where's the exit?!?",
    "This is the fifth time this week. I'm lost!",
    "I've been wandering around down here for hours.",
    "How do you get back to the village?",
    "I hate being so lost!",
    "How could I be so disoriented?",
    "Where am I? I'm so lost.",
    "I know the exit's around here, somewhere."
    )

    override fun onCreation(self: NPC)
    {
        self.setAttribute("cooldown", 0)
    }

    override fun tick(self: NPC): Boolean
    {
        val cooldown = self.getAttribute("cooldown", 0)
        if (cooldown > 0)
        {
            self.setAttribute("cooldown", cooldown - 1)
            return true
        }
        if (RandomFunction.roll(25))
        {
            sendChat(self, randomDialogue.random())
            self.setAttribute("cooldown", RandomFunction.random(5, 15))
        }
        return true
    }

}