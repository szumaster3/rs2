package content.global.skill.construction

import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueInterpreter
import core.game.node.entity.player.Player
import core.plugin.Initializable

/**
 * Removal dialogue for room & decorations in construction.
 *
 * @author Clayton Williams
 * @author Emperor
 */
@Initializable
class RemovalDialogue() : Dialogue() {

    /**
     * The room position.
     */
    private lateinit var pos: IntArray

    /**
     * The plane.
     */
    private var plane = 0

    /**
     * The room to remove.
     */
    private var room: Room? = null

    constructor(player: Player?) : this() {
        this.player = player
    }

    override fun newInstance(player: Player?): Dialogue = RemovalDialogue(player)

    override fun open(vararg args: Any): Boolean {
        pos = args[1] as IntArray

        plane = if (args.size > 2 && args[2] is Int) {
            args[2] as Int
        } else {
            if (HouseManager.isInDungeon(player)) {
                3
            } else {
                player.location.z
            }
        }

        room = player.houseManager.rooms[plane][pos[0]][pos[1]]

        if (room == null || room!!.properties.isRoof) {
            interpreter.sendPlainMessage(false, "There is no room there to remove.")
            return false
        }

        val room = room!!

        interpreter.sendOptions(
            "Remove the ${room.properties.name}?", "Yes", "No"
        )

        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        if (stage == 0) {
            if (buttonId == 1) {

                if (plane == 0 && player.houseManager.hasRoomAt(1, pos[0], pos[1])) {
                    interpreter.sendPlainMessage(
                        false, "You can't remove a room supporting another room."
                    )
                    stage = 1
                    return true
                }

                val room = room!!

                if (room.properties.isLand) {
                    val hotspot = room.hotspots[0]
                    if (hotspot != null && hotspot.decorationIndex == 0 && player.houseManager.portalAmount <= 1) {
                        interpreter.sendPlainMessage(
                            false, "You can't remove the garden with your portal in it."
                        )
                        stage = 1
                        return true
                    }
                }

                player.houseManager.rooms[plane][pos[0]][pos[1]] = null

                for (level in plane until player.houseManager.rooms.size) {
                    val roof = player.houseManager.rooms[level][pos[0]][pos[1]]
                    if (roof?.properties?.isRoof == true) {
                        player.houseManager.rooms[level][pos[0]][pos[1]] = null
                    }
                }

                player.houseManager.reload(player, true)
            }
        }

        end()
        return true
    }

    override fun getIds(): IntArray = intArrayOf(DialogueInterpreter.getDialogueKey("con:remove"))
}