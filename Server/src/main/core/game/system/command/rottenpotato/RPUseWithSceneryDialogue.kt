package core.game.system.command.rottenpotato

import core.api.*
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.plugin.Initializable
import core.tools.colorize

@Initializable
class RPUseWithSceneryDialogue(player: Player? = null) : Dialogue(player) {

    val ID = 38575797

    private lateinit var scenery: Scenery
    private var rotation: Int = 0
    private var type: Int = 10

    override fun newInstance(player: Player?): Dialogue = RPUseWithSceneryDialogue(player)

    override fun open(vararg args: Any?): Boolean {
        scenery = args[0] as Scenery

        options(
            "Remove Scenery",
            "Transform Scenery (+1 id)",
            "Teleport to Scenery"
        )
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (buttonId) {

            // Remove
            1 -> {
                removeScenery(scenery.asScenery())
                sendMessage(player, colorize("%RScenery removed."))
                end()
            }

            // Transform
            2 -> {
                val loc = scenery.location
                val newId = scenery.id + 1

                removeScenery(scenery.asScenery())
                addScenery(newId, loc, rotation, type)

                sendMessage(player, colorize("%BScenery transformed to $newId."))
                end()
            }

            // Teleport
            3 -> {
                player.teleport(scenery.location)
                end()
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(ID)
}