package core.game.worldevents.events.christmas.randoms

import core.api.*
import core.game.node.entity.npc.NPC
import core.game.worldevents.events.HolidayRandomEventNPC
import core.tools.minutesToTicks
import shared.consts.NPCs

class SnowmanFightHolidayRandom : HolidayRandomEventNPC(NPCs.XMAS07_SNOWMAN_BARBARIAN_6742) {
    private lateinit var otherSnowman: NPC
    private val snowmen = listOf(NPCs.XMAS07_SNOWMAN_DRAGON_6743, NPCs.XMAS07_SNOWMAN_PIRATE_6745, NPCs.XMAS07_SNOWMAN_DWARF_6744)
    private var snowmenFinalized = false

    override fun init() {
        otherSnowman = create(snowmen.random(), getPathableRandomLocalCoordinate(player, 3, player.location))
        ticksLeft = minutesToTicks(3)
        setAttribute(this, "holidayrandomplayer", player)
        setAttribute(otherSnowman, "holidayrandomplayer", player)
        setAttribute(otherSnowman, "holiday_random_extra_npc", true)
        this.behavior = SnowmanFightBehavior()
        otherSnowman.behavior = SnowmanFightBehavior()
        super.init()
        otherSnowman.init()

        this.attack(otherSnowman)
        otherSnowman.attack(this)
    }

    override fun tick() {
        if (!this.inCombat() && getWorldTicks() % 20 == 0) {
            this.teleport(otherSnowman.location)
            this.attack(otherSnowman)
            otherSnowman.attack(this)
        }
        super.tick()
    }

    override fun terminate() {
        if (!snowmenFinalized) {
            snowmenFinalized = true
            poofClear(otherSnowman)
        }
        super.terminate()
    }

    override fun follow() {
    }

    override fun talkTo(npc: NPC) {
        sendMessage(player, "They seem a little busy.")
    }
}
