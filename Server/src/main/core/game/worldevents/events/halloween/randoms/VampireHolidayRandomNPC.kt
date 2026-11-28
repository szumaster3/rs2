package core.game.worldevents.events.halloween.randoms

import core.api.*
import core.game.interaction.QueueStrength
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.npc.NPC
import core.game.worldevents.events.HolidayRandomEventNPC
import core.game.worldevents.events.HolidayRandoms
import core.game.worldevents.events.ResetHolidayAppearance
import core.tools.RandomFunction
import core.tools.colorize
import shared.consts.Animations
import shared.consts.Graphics
import shared.consts.NPCs
import shared.consts.Sounds

class VampireHolidayRandomNPC : HolidayRandomEventNPC(NPCs.VAMPIRE_FLYER_BOOT_1023) {
    override fun init() {
        super.init()
        playGlobalAudio(this.location, Sounds.REGULAR_VAMPIRE_APPEAR_1897)
        visualize(this, -1, Graphics.SPELLS_UPDATE_PLAYER_WATER_LAUNCH_1863)
        queueScript(this, 3, QueueStrength.SOFT) { stage: Int ->
            when (stage) {
                0 -> {
                    this.face(player)
                    visualize(this, Animations.PIRATE_UPDATE_ATTACK_UNARMED_7183, -1)
                    playGlobalAudio(this.location, Sounds.VAMPIRE_ATTACK_879)
                    if (RandomFunction.roll(2)) {
                        player.timers.registerTimer(ResetHolidayAppearance())
                        sendMessage(player, colorize("%RThe vampire bites your neck!"))
                        val hit = if (player.skills.lifepoints < 5) 0 else 2
                        impact(player, hit, ImpactHandler.HitsplatType.NORMAL)
                        player.appearance.transformNPC(NPCs.VAMPIRE_FLYER_BOOT_1023)
                    } else {
                        sendMessage(player, "The vampire tries to bite your neck and misses!")
                        impact(player, 0, ImpactHandler.HitsplatType.NORMAL)
                    }
                    return@queueScript delayScript(this, 8)
                }

                1 -> {
                    visualize(this, Animations.SWEPT_PLAYER_SCARE_EMOTE_10530, Graphics.SPELLS_UPDATE_PLAYER_WATER_LAUNCH_1863)
                    HolidayRandoms.terminateEventNpc(player)
                    return@queueScript stopExecuting(this)
                }

                else -> return@queueScript stopExecuting(this)
            }
        }
    }

    override fun talkTo(npc: NPC) {
    }
}
