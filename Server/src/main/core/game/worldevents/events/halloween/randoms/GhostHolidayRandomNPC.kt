package core.game.worldevents.events.halloween.randoms

import core.api.animate
import core.api.playGlobalAudio
import core.api.sendChat
import core.api.sendMessage
import core.game.node.entity.npc.NPC
import core.game.worldevents.events.HolidayRandomEventNPC
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.NPCs
import shared.consts.Sounds

class GhostHolidayRandomNPC : HolidayRandomEventNPC(NPCs.UNATTACKABLE_GHOST_2716) {
    override fun init() {
        super.init()
        this.isAggressive = false
        playGlobalAudio(this.location, Sounds.BIGGHOST_APPEAR_1595)
        animate(player, Animations.TERRIFIED_EMOTE_2836)
        sendChat(this, "WooooOOOooOOo")
        sendMessage(player, "The air suddenly gets colder...")
    }

    override fun tick() {
        super.tick()
        if (RandomFunction.roll(10)) {
            playGlobalAudio(this.location, Sounds.BIGGHOST_LAUGH_1600)
        }
    }

    override fun talkTo(npc: NPC) {
    }
}
