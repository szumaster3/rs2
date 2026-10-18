package content.global.skill.thieving.blackjack

import content.global.skill.thieving.blackjack.timer.BlackjackUnconsciousTimer
import content.global.skill.thieving.blackjack.timer.BlackjackUnconsciousTimer.Companion.BLACKJACK_UNCONSCIOUS
import core.api.*
import core.game.container.impl.EquipmentContainer
import core.game.interaction.MovementPulse
import core.game.node.entity.impl.PulseType
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import core.tools.secondsToTicks
import shared.consts.Animations

object BlackjackUtils {

    private const val LURE_SECONDS = 10
    private const val FAIL_STUN_TICKS = 3
    private const val KNOCK_OUT_ANIMATION = 393

    /**
     * Definition of the NPC the player actually sees (resolves wrapper NPCs through their varbit).
     */
    private fun shownDefinition(player: Player, npc: NPC) = npc.definition.getChildNPC(player)

    private fun shownName(player: Player, npc: NPC) = shownDefinition(player, npc).name.lowercase()

    /**
     * Whether the NPC is currently knocked out (timer running).
     */
    fun isUnconscious(npc: NPC): Boolean = hasTimerActive<BlackjackUnconsciousTimer>(npc)

    /**
     * Attempt to lure the NPC.
     */
    fun lure(player: Player, npc: NPC) {
        if (player.inCombat() || isUnconscious(npc)) return
        sendMessage(player, "You attempt to lure the ${shownName(player, npc)}.")
        npc.walkingQueue.reset()
        npc.pulseManager.run(getFollowPulse(npc, player), PulseType.STANDARD)
    }

    /**
     * Makes the NPC follow [target] for [durationSeconds].
     */
    private fun getFollowPulse(npc: NPC, target: Player, durationSeconds: Int = LURE_SECONDS): MovementPulse {
        val maxTicks = secondsToTicks(durationSeconds)
        return object : MovementPulse(npc, target) {
            var ticks = 0

            override fun pulse(): Boolean {
                ticks++
                return ticks >= maxTicks
            }
        }
    }

    /**
     * Attempt to knock out the NPC with a blackjack.
     */
    fun knockOut(player: Player, npc: NPC) {
        if (player.inCombat() || isUnconscious(npc)) return

        if (getEquippedBlackjack(player) == null) {
            sendMessage(player, "You need a blackjack to do this.")
            return
        }

        val npcData = BlackjackNPC.forId(shownDefinition(player, npc).id) ?: run {
            sendMessage(player, "You can't do that.")
            return
        }

        if (isStunned(player)) {
            sendMessage(player, "You're stunned!")
            return
        }

        val level = getDynLevel(player, Skills.THIEVING)
        if (level < npcData.thievingLevel) {
            sendMessage(player, "You need a Thieving level of ${npcData.thievingLevel} to do this.")
            return
        }

        player.animate(Animation(KNOCK_OUT_ANIMATION))

        if (RandomFunction.random(100) >= knockOutChance(level, npcData)) {
            fail(player, npc, npcData)
            return
        }

        // The stun keeps the NPC in place for as long as the timer keeps it "asleep".
        stun(npc, BlackjackUnconsciousTimer.DEFAULT_TICKS, false)
        registerTimer(
            npc,
            spawnTimer<BlackjackUnconsciousTimer>(
                BlackjackUnconsciousTimer.DEFAULT_TICKS,
                BlackjackUnconsciousTimer.DEFAULT_PICKPOCKETS
            )
        )

        rewardXP(player, Skills.THIEVING, npcData.xp)
        sendMessage(player, "You smack the ${shownName(player, npc)} over the head and render them unconscious.")
    }

    /**
     * Notify when another player interacts with an NPC that is currently knocked out.
     */
    fun notify(player: Player, npc: NPC) {
        if (!canPickpocket(npc)) return

        RegionManager.getLocalPlayers(npc, 1)
            .filter { it != player }
            .forEach {
                sendMessage(it, "I'm teaching coshing here, not pickpocketing.")
                it.lock(1)
            }
    }

    /**
     * Determines knock-out success chance (percent).
     */
    private fun knockOutChance(level: Int, npcData: BlackjackNPC): Int =
        if (npcData == BlackjackNPC.MENAPHITE_THUG) {
            31 + (level.coerceIn(1, 99) - 1) * 63 / 98
        } else {
            (50 + (level - npcData.thievingLevel) * 2).coerceIn(5, 95)
        }

    /**
     * Handle a failed knockout attempt.
     */
    private fun fail(player: Player, npc: NPC, npcData: BlackjackNPC) {
        npc.faceLocation(player.location)
        npc.sendChat("I'll kill you for that!")
        npc.animator.animate(Animation(Animations.ATTACK_395))
        impact(player, npcData.damage)
        stun(player, FAIL_STUN_TICKS, false)
        sendMessage(player, "You blow only glances off the ${shownName(player, npc)}'s head.")
    }

    /**
     * Returns the blackjack equipped by the player, if any.
     */
    private fun getEquippedBlackjack(player: Player): BlackjackType? =
        BlackjackType.fromItem(player.equipment.getId(EquipmentContainer.SLOT_WEAPON))

    /**
     * Returns whether the NPC can currently be pickpocketed.
     */
    fun canPickpocket(npc: NPC): Boolean {
        val timer = getTimer<BlackjackUnconsciousTimer>(npc)
        return timer != null && timer.remainingTicks > 0 && timer.pickpocketsLeft > 0
    }

    /**
     * Called when a player successfully pickpockets the NPC.
     */
    fun onPickpocket(npc: NPC) {
        val timer = getTimer<BlackjackUnconsciousTimer>(npc) ?: return
        timer.pickpocketsLeft--
        if (timer.pickpocketsLeft <= 0) {
            removeTimer(npc, BLACKJACK_UNCONSCIOUS)
        }
    }
}
