package content.global.random.event.evil_twin

import content.data.RandomEvent
import core.api.*
import core.api.utils.PlayerCamera
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.system.task.Pulse
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.build.DynamicRegion
import core.net.packet.PacketRepository
import core.net.packet.context.CameraContext
import core.net.packet.out.CameraViewPacket
import core.tools.RandomFunction
import shared.consts.*

/**
 * Utility object for handling the Evil Twin random event.
 */
object EvilTwinUtils {
    const val RANDOM_EVENT = "/save:evil_twin:random"
    const val CRANE_X_LOC = "/save:evil_twin:ccx"
    const val CRANE_Y_LOC = "/save:evil_twin:ccy"
    const val TRIES = "/save:evil_twin:tries"
    const val SUCCESS = "/save:evil_twin:success"
    val mollyNPCs = mutableMapOf<Player, NPC>()
    val craneNPCs = mutableMapOf<Player, NPC>()
    val cranes = mutableMapOf<Player, Scenery>()

    val rewards =
        arrayOf(
            Item(Items.UNCUT_DIAMOND_1618, 2),
            Item(Items.UNCUT_RUBY_1620, 3),
            Item(Items.UNCUT_EMERALD_1622, 3),
            Item(Items.UNCUT_SAPPHIRE_1624, 4)
        )

    val regions = mutableMapOf<Player, DynamicRegion>()

    fun getRegion(player: Player): DynamicRegion {
        return regions.getOrPut(player) {
            DynamicRegion.create(Regions.RE_EVIL_TWIN_7504)
        }
    }

    private fun removeRegion(player: Player) {
        regions.remove(player)?.clear()
    }

    /**
     * Starts the event.
     *
     * @param player The player.
     * @return `true` if the event was started, `false` otherwise.
     */
    fun start(player: Player): Boolean {
        val region = getRegion(player)
        region.add(player)
        region.setMusicId(Music.HEAD_TO_HEAD_612)
        cranes[player] = Scenery(shared.consts.Scenery.EVIL_CLAW_14976, region.baseLocation.transform(14, 12, 0), 10, 0)
        val color: EvilTwinColors = RandomFunction.getRandomElement(EvilTwinColors.values())
        val model = RandomFunction.random(5)
        val hash = color.ordinal or (model shl 16)
        val npcId = getMollyId(hash)
        setAttribute(player, RANDOM_EVENT, hash)
        val molly = NPC.create(
            npcId,
            Location.getRandomLocation(player.location, 1, true)
        )

        mollyNPCs[player] = molly
        molly?.apply {
            isWalks = false
            isNeverWalks = true
            isRespawn = false
            init()
        }

        mollyNPCs[player]?.let { npc ->
            sendChat(npc, "I need your help, ${player.username}.")
            npc.faceTemporary(player, 3)
        }
        setAttribute(player,SUCCESS,false)
        setAttribute(player,TRIES,3)
        setAttribute(player, RandomEvent.save(), player.location)
        queueScript(player, 4, QueueStrength.SOFT) {
            mollyNPCs[player]?.let { npc ->
                teleport(player, npc, hash)
                npc.locks.lockMovement(300000)
                openDialogue(player, MollyDialogue(3))
            }
            stopExecuting(player)
        }

        return true
    }

    /**
     * Teleports both player and npc to event region.
     *
     * @param player The player.
     * @param npc The npc.
     * @param hash The hash of this event.
     */
    fun teleport(player: Player, npc: NPC, hash: Int) {
        val region = getRegion(player)
        setMinimapState(player, 2)
        npc.properties.teleportLocation = region.baseLocation.transform(4, 15, 0)
        npc.direction = Direction.NORTH
        player.properties.teleportLocation = region.baseLocation.transform(4, 16, 0)
        registerLogoutListener(player, RandomEvent.logout()) { p ->
            p.location = getAttribute(p, RandomEvent.save(), player.location)
        }
        spawnSuspects(player,hash)
        showNPCs(player,true)
    }

    /**
     * Cleans up the event.
     *
     * @param player The player.
     */
    fun cleanup(player: Player) {
        PlayerCamera(player).reset()
        restoreTabs(player)
        player.properties.teleportLocation = getAttribute(player, RandomEvent.save(), null)
        setMinimapState(player, 0)
        removeAttributes(player, RANDOM_EVENT, RandomEvent.save(), CRANE_X_LOC, CRANE_Y_LOC, TRIES, SUCCESS)
        clearLogoutListener(player, RandomEvent.logout())
        removeRegion(player)
    }

    /**
     * Decreases the number of tries the player has left.
     *
     * @param player The player.
     */
    fun decreaseTries(player: Player) {
        val remainingTries = getAttribute(player, TRIES, 3) - 1

        setAttribute(player, TRIES, remainingTries)

        sendString(
            player,
            "Tries: $remainingTries",
            Components.CRANE_CONTROL_240,
            27
        )

        if (remainingTries < 1) {
            lock(player, 20)
            closeTabInterface(player)
            openDialogue(player, MollyDialogue(1))
        }
    }

    /**
     * Updates the location based logic for the player and entity.
     *
     * @param player The player.
     * @param entity The entity.
     * @param last The last known location of the entity.
     */
    fun locationUpdate(player: Player, entity: Entity, last: Location?) {
        when (entity) {
            craneNPCs[player] -> {
                if (entity.walkingQueue.queue.size > 1 && player.interfaceManager.singleTab != null) {
                    val l = entity.location
                    PacketRepository.send(
                        CameraViewPacket::class.java,
                        CameraContext(player, CameraContext.CameraType.POSITION, l.x + 2, l.y + 3, 520, 1, 5)
                    )
                    PacketRepository.send(
                        CameraViewPacket::class.java,
                        CameraContext(player, CameraContext.CameraType.ROTATION, l.x - 3, l.y - 3, 420, 1, 5)
                    )
                }
            }
            player -> {
                mollyNPCs[player]?.let { npc ->
                    if (npc.isHidden(player) && entity.location.localX < 9) {
                        showNPCs(player,true)
                    } else if (!npc.isHidden(player) && entity.location.localX > 8) {
                        showNPCs(player,false)
                    }
                }
            }
        }
    }

    /**
     * Updates the camera position and rotation based on crane coordinates.
     *
     * @param player The player.
     * @param x The x-coords of the crane.
     * @param y The y-coords of the crane.
     */
    fun updateCraneCam(player: Player, x: Int, y: Int) {
        val region = getRegion(player)
        if (player.interfaceManager.singleTab != null) {
            var loc = region.baseLocation.transform(14, 20, 0)
            PacketRepository.send(
                CameraViewPacket::class.java,
                CameraContext(player, CameraContext.CameraType.POSITION, loc.x, loc.y, 520, 1, 100)
            )
            loc =
                region.baseLocation.transform(x, 4 + y - (if (x < 14 || x > 14) (y / 4) else 0), 0)
            PacketRepository.send(
                CameraViewPacket::class.java,
                CameraContext(player, CameraContext.CameraType.ROTATION, loc.x, loc.y, 420, 1, 100)
            )
        }
        setAttribute(player, CRANE_X_LOC, x)
        setAttribute(player, CRANE_Y_LOC, y)
    }

    /**
     * Moves the crane in a specified direction.
     *
     * @param player The player.
     * @param direction The direction.
     */
    fun moveCrane(player: Player, direction: Direction) {
        val region = getRegion(player)
        submitWorldPulse(
            object : Pulse(1, player) {
                override fun pulse(): Boolean {
                    val crane = cranes[player] ?: return true

                    if (!direction.canMove(crane.location.transform(direction))) {
                        return true
                    }
                    val craneX: Int = player.getAttribute(CRANE_X_LOC, 14) + direction.stepX
                    val craneY: Int = player.getAttribute(CRANE_Y_LOC, 12) + direction.stepY
                    updateCraneCam(player, craneX, craneY)
                    removeScenery(crane)
                    addScenery(Scenery(66, crane.location, 22, 0))
                    val newCrane = crane.transform(
                        crane.id,
                        crane.rotation,
                        region.baseLocation.transform(craneX, craneY, 0)
                    )

                    cranes[player] = newCrane

                    addScenery(Scenery(14977, newCrane.location, 22, 0))
                    addScenery(newCrane)
                    return true
                }
            }
        )
    }

    /**
     * Shows or hides Molly.
     *
     * @param showMolly True to show, false to hide.
     */
    private fun showNPCs(p: Player, showMolly: Boolean) {
        val region = getRegion(p)
        mollyNPCs[p]?.isInvisible = !showMolly
        for (npc in region.planes[0].npcs) {
            if (npc.id in NPCs.SUSPECT_3852..NPCs.SUSPECT_3891) {
                npc.isInvisible = false
            }
        }
    }

    /**
     * Checks if npc is Evil Twin.
     *
     * @param npc The NPC to check.
     * @param hash The hash value of the npc.
     * @return `true` if the npc is the evil twin, `false` otherwise.
     */
    fun isEvilTwin(npc: NPC, hash: Int): Boolean {
        val npcId = npc.id - NPCs.SUSPECT_3852
        val type: Int = npcId / EvilTwinColors.values().size
        val color: Int = npcId - (type * EvilTwinColors.values().size)
        return hash == (color or (type shl 16))
    }

    /**
     * Removes all suspects npc.
     *
     * @param player The player.
     */
    fun removeSuspects(player: Player) {
        val region = getRegion(player)
        val hash: Int = player.getAttribute(RANDOM_EVENT, 0)
        for (npc in region.planes[0].npcs) {
            if (npc.id in NPCs.SUSPECT_3852..NPCs.SUSPECT_3891 && !isEvilTwin(npc, hash)) {
                sendGraphics(shared.consts.Graphics.RE_PUFF_86, npc.location)
                npc.clear()
            }
        }
    }

    /**
     * Spawns the Evil Twin in the region.
     *
     * @param hash The hash value of the npc.
     */
    private fun spawnSuspects(p:Player,hash: Int) {
        val region = getRegion(p)
        if (region.planes[0].npcs.size > 3) return

        val npcId = 3852 + (hash and 0xFF)
        for (i in 0..4) {
            val location =
                region.baseLocation.transform(
                    11 + RandomFunction.random(8),
                    6 + RandomFunction.random(6),
                    0
                )
            val suspect = NPC.create(npcId + (i * EvilTwinColors.values().size), location)
            suspect.isWalks = true
            suspect.isNeverWalks = false
            suspect.isRespawn = false
            suspect.walkRadius = 6
            suspect.init()
        }
    }

    /**
     * Calculates the npc id for Molly based on a hash.
     *
     * @param hash The hash value.
     * @return The npc id.
     */
    private fun getMollyId(hash: Int): Int {
        return 3892 + (hash and 0xFF) + (((hash shr 16) and 0xFF) * EvilTwinColors.values().size)
    }
}