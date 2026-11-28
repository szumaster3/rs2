package content.global.skill.hunter.pitfall

import content.global.skill.hunter.HunterManager
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.Entity
import core.game.node.entity.impl.Animator
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.scenery.Scenery
import core.game.system.task.Pulse
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds
import java.util.concurrent.TimeUnit

class PitfallPlugin : InteractionListener {
    override fun defineListeners() {

        setDest(IntType.SCENERY, PIT_SCENERY_IDS.toIntArray(), "trap", "jump", "dismantle") { player, node ->
            getBestJumpSpot(player.location, node as Scenery)
        }

        on(shared.consts.Scenery.PIT_19227, IntType.SCENERY, "trap") { player, node ->
            val pit = node as Scenery
            val hunterLevel = getStatLevel(player, Skills.HUNTER)

            if (hunterLevel < 31) {
                sendMessage(player, "You need a Hunter level of 31 to set a pitfall trap.")
                return@on true
            }

            val manager = HunterManager.getInstance(player)
            val maxTraps = manager.maximumTraps
            if (getAttribute(player, "pitfall:count", 0) >= maxTraps) {
                sendMessage(player, "You can't set up more than $maxTraps pitfall traps at your hunter level.")
                return@on true
            }

            if (!inInventory(player, KNIFE_ITEM_ID) || !removeItem(player, LOG_ITEM_ID)) {
                sendMessage(player, "You need some logs and a knife to set a pitfall trap.")
                return@on true
            }

            val timestamp = System.currentTimeMillis()
            synchronized(pitTimestamps) { pitTimestamps[pit.location] = timestamp }
            player.incrementAttribute("pitfall:count", 1)
            setPitState(player, pit.location, 1)
            playAudio(player, Sounds.HUNTING_PLACEBRANCHES_2639)

            submitWorldPulse(object : Pulse(201, player) {
                override fun pulse(): Boolean {
                    val lastTimestamp = pitTimestamps[pit.location] ?: return true
                    if (System.currentTimeMillis() - lastTimestamp >= TimeUnit.MINUTES.toMillis(2)) {
                        synchronized(pitTimestamps) { pitTimestamps.remove(pit.location) }
                        synchronized(pitNpcList) { pitNpcList.remove(pit.location) }
                        setPitState(player, pit.location, 0)
                        player.incrementAttribute("pitfall:count", -1)
                        sendMessage(player, "A pitfall trap has collapsed.")
                    }
                    return true
                }
            })

            return@on true
        }

        on(shared.consts.Scenery.SPIKED_PIT_19228, IntType.SCENERY, "jump") { player, node ->
            val pit = node as Scenery
            val src = player.location
            val dir = PitfallDefinition.pitJumpSpots(pit.location)?.get(src) ?: return@on true
            val dst = src.transform(dir, 3)

            ForceMovement.run(player, src, dst, ForceMovement.WALK_ANIMATION, Animation(Animations.HUMAN_JUMP_HURDLE_1603), dir, 16)
            playAudio(player, Sounds.HUNTING_JUMP_2635)

            synchronized(pitNpcList) {
                pitNpcList[pit.location]?.toList()?.forEach { npc ->
                    handlePitfallNpcJump(player, npc, pit, src, dst, dir)
                }
            }
            return@on true
        }

        on(shared.consts.Scenery.SPIKED_PIT_19228, IntType.SCENERY, "dismantle") { player, node ->
            dismantlePit(player, node as Scenery)
            return@on true
        }

        handleDismantlePit(shared.consts.Scenery.COLLAPSED_TRAP_19232, Items.LARUPIA_FUR_10095, Items.TATTY_LARUPIA_FUR_10093, "larupia", 180.0)
        handleDismantlePit(shared.consts.Scenery.COLLAPSED_TRAP_19231, Items.GRAAHK_FUR_10099, Items.TATTY_GRAAHK_FUR_10097, "graahk", 240.0)
        handleDismantlePit(shared.consts.Scenery.COLLAPSED_TRAP_19233, Items.KYATT_FUR_10103, Items.TATTY_KYATT_FUR_10101, "kyatt", 300.0)

        on(PitfallDefinition.BEAST_IDS, IntType.NPC, "tease") { player, node ->
            val npc = node as Entity
            val req = PitfallDefinition.HUNTER_REQS[npc.name] ?: return@on true

            if (getStatLevel(player, Skills.HUNTER) < req) {
                sendMessage(player, "You need a Hunter level of $req to hunt ${npc.name.lowercase()}s.")
                return@on true
            }
            if (!inInventory(player, TEASING_STICK_ITEM_ID)) {
                sendMessage(player, "You need a teasing stick to hunt ${npc.name.lowercase()}s.")
                return@on true
            }

            npc.attack(player)
            playAudio(player, Sounds.HUNTING_TEASE_FELINE_2651)

            synchronized(pitNpcList) {
                pitNpcList.getOrPut(npc.location) { mutableListOf() }.add(npc)
            }

            setAttribute(player, "pitfall_npc", npc)
            return@on true
        }
    }

    private fun getBestJumpSpot(src: Location, pit: Scenery): Location {
        val locs = PitfallDefinition.pitJumpSpots(pit.location)
        return locs?.keys?.minByOrNull { src.getDistance(it) } ?: pit.location
    }

    private fun handlePitfallNpcJump(player: Player, npc: Entity, pit: Scenery, src: Location, dst: Location, dir: Direction) {
        if (npc.location.getDistance(src) >= 3.0) return

        val lastPit = npc.getAttribute("last_pit_loc", null) as? Location
        if (lastPit == pit.location) {
            sendMessage(player, "The ${npc.name.lowercase()} won't jump the same pit twice in a row.")
            return
        }

        val chance = RandomFunction.getSkillSuccessChance(50.0, 100.0, player.skills.getLevel(Skills.HUNTER))
        if (RandomFunction.random(0.0, 100.0) < chance) {
            teleport(npc, pit.location)
            removeAttribute(npc, "last_pit_loc")
            playAudio(player, Sounds.HUNTING_PITFALL_COLLAPSE_2638, 0, 1, pit.location, 10)
            playAudio(player, Sounds.PANTHER_DEATH_667, 50, 1, pit.location, 10)
            npc.startDeath(null)
            synchronized(pitNpcList) { pitNpcList[pit.location]?.remove(npc) }
            synchronized(pitTimestamps) { pitTimestamps.remove(pit.location) }
            player.incrementAttribute("pitfall:count", -1)
            setPitState(player, pit.location, 3)
        } else {
            val npcDest = dst.transform(dir, if (dir == Direction.SOUTH || dir == Direction.WEST) 1 else 0)
            teleport(npc, npcDest)
            animate(npc, Animation(5232, Animator.Priority.HIGH))
            playAudio(player, Sounds.HUNTING_BIGCAT_JUMP_2619, 0, 1, pit.location, 10)
            npc.attack(player)
            setAttribute(npc, "last_pit_loc", pit.location)
        }
    }

    private fun dismantlePit(player: Player, pit: Scenery) {
        playAudio(player, Sounds.HUNTING_TAKEBRANCHES_2649)
        synchronized(pitTimestamps) { pitTimestamps.remove(pit.location) }
        synchronized(pitNpcList) { pitNpcList.remove(pit.location) }
        player.incrementAttribute("pitfall:count", -1)
        setPitState(player, pit.location, 0)
        sendMessage(player, "You dismantled the pitfall trap.")
    }

    private fun handleDismantlePit(pitId: Int, goodFur: Int, badFur: Int, name: String, xp: Double) {
        on(pitId, IntType.SCENERY, "dismantle") { player, node ->
            lootCorpse(player, node as Scenery, xp, goodFur, badFur)
            sendMessage(player, "You've caught a $name!")
            if (pitId == shared.consts.Scenery.COLLAPSED_TRAP_19231)
                finishDiaryTask(player, DiaryType.KARAMJA, 1, 13)
                setVarbit(player, 3592,1,true)

            return@on true
        }
    }

    private fun lootCorpse(player: Player, pit: Scenery, xp: Double, goodFur: Int, badFur: Int) {
        if (freeSlots(player) < 2) {
            sendMessage(player, "You don't have enough inventory space. You need 2 more free slots.")
            return
        }

        setPitState(player, pit.location, 0)
        rewardXP(player, Skills.HUNTER, xp)
        addItemOrDrop(player, Items.BIG_BONES_532)
        playAudio(player, Sounds.HUNTING_TAKEBRANCHES_2649)

        val chance = RandomFunction.getSkillSuccessChance(50.0, 100.0, getStatLevel(player, Skills.HUNTER))
        val furItem = if (RandomFunction.random(0.0, 100.0) < chance) goodFur else badFur
        addItemOrDrop(player, furItem)
    }

    private fun setPitState(player: Player, loc: Location, state: Int) {
        val pit = PitfallDefinition.pitVarps[loc] ?: return
        setVarbit(player, pit.varbitId, state)
    }

    companion object {
        private const val KNIFE_ITEM_ID = Items.KNIFE_946
        private const val TEASING_STICK_ITEM_ID = Items.TEASING_STICK_10029
        private const val LOG_ITEM_ID = Items.LOGS_1511
        private val PIT_SCENERY_IDS = listOf(
            shared.consts.Scenery.PIT_19227,
            shared.consts.Scenery.SPIKED_PIT_19228,
            shared.consts.Scenery.COLLAPSED_TRAP_19231,
            shared.consts.Scenery.COLLAPSED_TRAP_19232,
            shared.consts.Scenery.COLLAPSED_TRAP_19233,
        )

        private val pitTimestamps = mutableMapOf<Location, Long>()
        private val pitNpcList = mutableMapOf<Location, MutableList<Entity>>()
    }
}
