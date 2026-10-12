package content.region.kandarin.feldip.quest.chompybird.npc

import core.api.*
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Handles the RantzNPC.
 *
 * # Relations
 * - [Big Chompy Bird Hunting][content.region.kandarin.feldip.quest.chompybird.BigChompyBirdHunting]
 */
@Initializable
class RantzNPC : AbstractNPC {
    constructor() : super(NPCs.RANTZ_1010, null, true)
    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any?): AbstractNPC = RantzNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.RANTZ_1010)

    override fun handleTickActions() {
        super.handleTickActions()
        val chompy = findLocalNPC(this, NPCs.CHOMPY_BIRD_1550) as? ChompyBirdNPC ?: return

        val owner = getAttribute<Player?>(chompy, "owner", null) ?: return
        if (getQuestStage(owner, Quests.BIG_CHOMPY_BIRD_HUNTING) !in 40..50 || chompy.getAttribute("attacked", false)) return

        sendChat("Hey, dere's da chompy, I's gonna shoot it.")
        sendMessage(owner, "Rantz: Hey, dere's da chompy, I's gonna shoot it.")
        sendMessage(owner, "Rantz keeps missing the chompy bird.")
        sendMessage(owner, "Rantz: Grrr...de'ese arrows are rubbish.")

        attack(chompy)
        chompy.setAttribute("attacked", true)
        setQuestStage(owner, Quests.BIG_CHOMPY_BIRD_HUNTING, 50)
    }
}
