package content.region.morytania.mort_myre.quest.druidspirit.plugin

import content.region.morytania.mort_myre.npc.GhastNPC
import core.api.*
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.scenery.SceneryBuilder
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Graphics
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.Sounds

object NSUtils {
    fun flagFungusPlaced(player: Player) {
        setAttribute(player, "/save:ns:fungus_placed", true)
    }

    fun flagCardPlaced(player: Player) {
        setAttribute(player, "/save:ns:card_placed", true)
    }

    fun hasPlacedFungus(player: Player): Boolean = getAttribute(player, "ns:fungus_placed", false)

    fun hasPlacedCard(player: Player): Boolean = getAttribute(player, "ns:card_placed", false)

    fun onStone(player: Player): Boolean = player.location.equals(3440, 3335, 0)

    fun getGhastKC(player: Player): Int = getAttribute(player, "ns:ghasts_killed", 0)

    fun incrementGhastKC(player: Player) {
        setAttribute(player, "/save:ns:ghasts_killed", getGhastKC(player) + 1)
        if (getGhastKC(player) == 3) {
            sendMessage(player, "That's all three Ghasts!")
        }
    }

    fun activatePouch(
        player: Player,
        attacker: GhastNPC,
    ): Boolean {
        var shouldAddEmptyPouch = false
        val pouchAmt = amountInInventory(player, Items.DRUID_POUCH_2958)
        if (pouchAmt == 1) shouldAddEmptyPouch = true
        if (pouchAmt > 0 && removeItem(player, Items.DRUID_POUCH_2958, Container.INVENTORY)) {
            if (shouldAddEmptyPouch) {
                addItem(player, Items.DRUID_POUCH_2957)
            }
            spawnProjectile(player, attacker, 268)
            queueScript(player, 3, QueueStrength.NORMAL) { stage ->
                when (stage) {
                    0 -> {
                        playAudio(player, 1495)
                        visualize(attacker, -1, Graphics(shared.consts.Graphics.FIRST_CONTACT_GOES_WITH_ABOVE_269, 125))
                        return@queueScript delayScript(player, 1)
                    }
                    1 -> {
                        attacker.transform(attacker.id + 1)
                        playAudio(player, 1490)
                        attacker.attack(player)
                        attacker.setAttribute("woke", getWorldTicks())
                        return@queueScript stopExecuting(player)
                    }
                    else -> return@queueScript stopExecuting(player)
                }
            }
            return true
        }
        return false
    }

    fun cleanupAttributes(player: Player) {
        removeAttribute(player, "ns:fungus_placed")
        removeAttribute(player, "ns:card_placed")
    }

    @JvmStatic
    fun castBloom(player: Player): Boolean {
        if (player.skills.prayerPoints < 1) {
            sendMessage(player, "You need to recharge your Prayer at an altar.")
            return false
        }

        lock(player, 1)
        handleVisuals(player)

        val locs = player.location.getSurroundingTiles()
        var success = false

        for (o in locs) {
            val obj = RegionManager.getObject(o) ?: continue

            if (!player.location.withinDistance(obj.location, 2)) continue

            when (obj.name.lowercase()) {
                "rotting log" -> {
                    SceneryBuilder.replace(obj, obj.transform(3509))
                    playAudio(player, Sounds.BLOOM_MUSHROOM_1491)
                    success = true
                }
                "rotting branch" -> {
                    SceneryBuilder.replace(obj, obj.transform(3511))
                    playAudio(player, Sounds.BLOOM_BRANCH_1489)
                    success = true
                }
                "a small bush" -> {
                    SceneryBuilder.replace(obj, obj.transform(3513))
                    playAudio(player, Sounds.BLOOM_PEARS_1492)
                    success = true
                }
            }
        }

        if (!success) {
            sendMessage(player, "There is no suitable material to be affected in this area.")
        }

        return success
    }

    private fun handleVisuals(player: Player) {
        player.skills.decrementPrayerPoints(RandomFunction.random(1, 5).toDouble())
        playAudio(player, Sounds.CAST_BLOOM_1493)
        val aroundPlayer = player.location.getSurroundingTiles()
        for (location in aroundPlayer) {
            player.packetDispatch.sendGlobalPositionGraphic(
                shared.consts.Graphics.SMALLS_STARS_SILVER_SICKLEB_263,
                location,
            )
        }
    }
}
