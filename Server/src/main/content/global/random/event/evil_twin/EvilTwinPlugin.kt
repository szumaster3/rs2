package content.global.random.event.evil_twin

import content.data.GameAttributes
import core.api.*
import core.game.component.Component
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.net.packet.PacketRepository
import core.net.packet.context.CameraContext
import core.net.packet.out.CameraViewPacket
import shared.consts.Components
import shared.consts.NPCs
import shared.consts.Regions

/**
 * Handles the evil twin random event.
 * @author Emperor, szu
 */
class EvilTwinPlugin : InteractionListener, MapArea {

    companion object {
        private val mollyId = (NPCs.MOLLY_3892..NPCs.MOLLY_3911).toIntArray()
        private const val DOOR_ID = shared.consts.Scenery.DOOR_14982
        private const val CONTROL_PANEL_SCENERY_ID = shared.consts.Scenery.CONTROL_PANEL_14978
    }

    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(ZoneBorders.forRegion(Regions.RE_EVIL_TWIN_7504))
    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(ZoneRestriction.CANNON, ZoneRestriction.FOLLOWERS)
    override fun areaLeave(entity: Entity, logout: Boolean)
    {
        super.areaLeave(entity, logout)
        if(entity is Player)
        {
            val player = entity.asPlayer()
            EvilTwinUtils.mollyNPCs.remove(player)
            EvilTwinUtils.craneNPCs.remove(player)
            EvilTwinUtils.cranes.remove(player)
            EvilTwinUtils.regions.remove(player)
        }
    }

    override fun defineListeners() {

        /*
         * Handles talk to molly at Random event.
         */

        on(mollyId, IntType.NPC, "talk-to") { player, node ->
            if ((getAttribute(player, EvilTwinUtils.TRIES, 3) < 1 ||
                        getAttribute(player, EvilTwinUtils.SUCCESS, false)) &&
                node.id in NPCs.MOLLY_3892..NPCs.MOLLY_3911
            ){
                openDialogue(player, MollyDialogue(if (getAttribute(player,EvilTwinUtils.SUCCESS,false)) 2 else 1), node.id)
            }
            return@on true
        }

        /*
         * Handles operating the crane.
         */

        on(CONTROL_PANEL_SCENERY_ID, IntType.SCENERY, "use") { player, _ ->
            if (getAttribute(player, EvilTwinUtils.SUCCESS, false)) {
                sendMessage(player, "You already caught the evil twin.")
                return@on true
            }

            val crane = EvilTwinUtils.cranes[player] ?: return@on true

            player.interfaceManager.openSingleTab(
                Component(Components.CRANE_CONTROL_240).setUncloseEvent { _, _ ->
                    SceneryBuilder.remove(crane)
                    SceneryBuilder.add(Scenery(shared.consts.Scenery.CRATE_WALL_66, crane.location, 22, 0))
                    val newCrane = crane.transform(crane.id, crane.rotation, EvilTwinUtils.getRegion(player).baseLocation.transform(14, 12, 0))
                    EvilTwinUtils.cranes[player] = newCrane
                    SceneryBuilder.add(Scenery(14977, newCrane.location, 22, 0))
                    SceneryBuilder.add(newCrane)
                    PacketRepository.send(CameraViewPacket::class.java, CameraContext(player, CameraContext.CameraType.RESET, 0, 0, 0, 0, 0))
                    true
                }
            )
            player.packetDispatch.sendString("Tries: ${getAttribute(player, EvilTwinUtils.TRIES, 3)}", 240, 27)
            EvilTwinUtils.updateCraneCam(player, 14, 12)
            return@on false
        }

        /*
         * Handles doors between molly and twins if player wants to rush.
         */

        on(DOOR_ID, IntType.SCENERY, "open") { player, node ->
            val end = DoorActionHandler.getEndLocation(player, node.asScenery())
            if (player.location.localX < 9 && !player.getAttribute(GameAttributes.RE_TWIN_DIAL, false)) {
                openDialogue(player, MollyDialogue(3), EvilTwinUtils.mollyNPCs[player]!!)
                return@on true
            }
            DoorActionHandler.open(node.asScenery(), node.asScenery(), node.id, node.id + 1, true, 3, false)
            forceWalk(player, end, "")
            return@on true
        }
    }
}
